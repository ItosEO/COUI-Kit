import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { SaxesParser } from 'saxes';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const ROOT_DIR = path.resolve(__dirname, '..');
const COUI_COLOR_PATH = path.join(ROOT_DIR, 'coui/src/main/java/com/itos/coui/theme/CouiColor.kt');
const COUI_ICONS_PATH = path.join(ROOT_DIR, 'coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt');
const RESOURCE_DIR = path.join(ROOT_DIR, 'coui-icons/src/main/res');
const DRAWABLE_DIR = path.join(RESOURCE_DIR, 'drawable');
const DRAWABLE_NIGHT_DIR = path.join(RESOURCE_DIR, 'drawable-night');
const DAY_COLORS_PATH = path.join(RESOURCE_DIR, 'values/coui_icon_colors.xml');
const NIGHT_COLORS_PATH = path.join(RESOURCE_DIR, 'values-night/coui_icon_colors.xml');
const OUTPUT_DATA_PATH = path.join(__dirname, 'data.js');

function formatNum(value) {
  return Number(Number(value).toFixed(4)).toString();
}

function parseNumber(raw, context) {
  const value = Number(raw);
  if (!Number.isFinite(value)) {
    throw new Error(`Invalid number in ${context}: ${raw}`);
  }
  return value;
}

function parseDimension(raw, context) {
  const match = raw?.trim().match(/^(-?(?:\d+(?:\.\d*)?|\.\d+))dp$/);
  if (!match) {
    throw new Error(`Expected a dp dimension in ${context}: ${raw}`);
  }
  return parseNumber(match[1], context);
}

function parseCouiColors(source) {
  const colors = { Day: {}, Night: {} };
  const clean = source.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
  const objectRegex = /object\s+(Day|Night)\s*\{([\s\S]*?)\n\s*\}/g;
  let match;
  const matchedThemes = new Set();
  while ((match = objectRegex.exec(clean)) !== null) {
    const theme = match[1];
    matchedThemes.add(theme);
    for (const rawLine of match[2].split('\n')) {
      const line = rawLine.trim();
      if (!line) continue;
      const valueMatch = line.match(/^val\s+([A-Z0-9_]+)\s*=\s*(.+)$/);
      if (!valueMatch) throw new Error(`Unsupported CouiColor.${theme} declaration: ${line}`);
      const [, name, expression] = valueMatch;
      const colorMatch = expression.trim().match(/^Color\(0x([0-9A-Fa-f]{8})\)$/);
      if (colorMatch) {
        const argb = colorMatch[1].toUpperCase();
        if (!argb.startsWith('FF')) throw new Error(`Expected opaque CouiColor.${theme}.${name}`);
        colors[theme][name] = `#${argb.slice(2)}`;
      } else if (/^[A-Z0-9_]+$/.test(expression.trim())) {
        const reference = expression.trim();
        if (!(reference in colors[theme])) throw new Error(`Unresolved CouiColor.${theme}.${name}: ${reference}`);
        colors[theme][name] = colors[theme][reference];
      } else {
        const referenceMatch = expression.trim().match(/^(Day|Night)\.([A-Z0-9_]+)$/);
        if (!referenceMatch) throw new Error(`Unsupported CouiColor.${theme}.${name}: ${expression}`);
        const [, refTheme, refName] = referenceMatch;
        if (!(refName in colors[refTheme])) throw new Error(`Unresolved CouiColor.${theme}.${name}: ${expression}`);
        colors[theme][name] = colors[refTheme][refName];
      }
    }
  }
  if (!matchedThemes.has('Day') || !matchedThemes.has('Night')) {
    throw new Error('Could not read both Day and Night CouiColor palettes');
  }
  return colors;
}

function parseIconOrder(source) {
  const clean = source.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/.*$/gm, '');
  const order = {};
  const aliases = {};
  const sharedResources = new Map();
  const bitmapResources = new Map();
  for (const style of ['Filled', 'Outlined', 'Monochrome', 'Bitmap']) {
    const declaration = new RegExp(`\\bobject\\s+${style}\\s*\\{`).exec(clean);
    if (!declaration) throw new Error(`Could not find CouiIcons.${style}`);
    const openBrace = declaration.index + declaration[0].lastIndexOf('{');
    let depth = 0;
    let inString = false;
    let endBrace = -1;
    for (let index = openBrace; index < clean.length; index++) {
      const character = clean[index];
      if (character === '"' && clean[index - 1] !== '\\') inString = !inString;
      if (!inString && character === '{') depth++;
      if (!inString && character === '}' && --depth === 0) {
        endBrace = index;
        break;
      }
    }
    if (endBrace === -1) throw new Error(`Unclosed CouiIcons.${style} declaration`);
    const body = clean.slice(openBrace + 1, endBrace);
    const type = style === 'Bitmap' ? 'Painter' : 'ImageVector';
    const propertyPattern = new RegExp(`^\\s*val\\s+([A-Z][A-Za-z0-9_]*)\\s*:\\s*${type}\\s+@Composable\\s+get\\(\\)\\s*=\\s*([^\\n]+)`, 'gm');
    const properties = [...body.matchAll(propertyPattern)];
    if (properties.length === 0) throw new Error(`CouiIcons.${style} has no icon properties`);
    if (style === 'Bitmap') {
      for (const [, name, getter] of properties) {
        const resource = getter.trim().match(/^couiBitmapResource\(R\.drawable\.(coui_[a-z0-9_]+)\)$/);
        if (!resource || bitmapResources.has(name)) throw new Error(`Invalid Bitmap icon ${name}`);
        bitmapResources.set(name, resource[1]);
      }
    } else if (style === 'Monochrome') {
      for (const [, name, getter] of properties) {
        const resource = getter.trim().match(/^couiIconResource\(R\.drawable\.(coui_[a-z0-9_]+)\)$/);
        if (!resource || sharedResources.has(name)) throw new Error(`Invalid Monochrome icon ${name}`);
        sharedResources.set(name, resource[1]);
      }
    } else {
      order[style] = properties.map((match) => match[1]);
      aliases[style] = new Set(properties.filter(([, name, getter]) => getter.trim() === `Monochrome.${name}`).map((match) => match[1]));
    }
  }
  return { order, aliases, sharedResources, bitmapResources };
}

function parseXml(source, context) {
  let root = null;
  let parseError = null;
  const stack = [];
  const parser = new SaxesParser({ xmlns: false });
  parser.on('opentag', (tag) => {
    const node = { name: tag.name, attrs: { ...tag.attributes }, children: [], text: '' };
    if (stack.length > 0) {
      stack[stack.length - 1].children.push(node);
    } else if (root === null) {
      root = node;
    } else {
      parseError = new Error('Multiple root elements');
    }
    stack.push(node);
  });
  parser.on('text', (text) => {
    if (stack.length > 0) stack[stack.length - 1].text += text;
  });
  parser.on('closetag', () => stack.pop());
  parser.on('error', (error) => {
    parseError = error;
  });

  try {
    parser.write(source).close();
  } catch (error) {
    parseError = parseError || error;
  }
  if (parseError) {
    throw new Error(`Invalid XML in ${context}: ${parseError.message}`, { cause: parseError });
  }
  if (!root || stack.length !== 0) {
    throw new Error(`Invalid XML in ${context}: missing root element`);
  }
  return root;
}

function readXml(filePath, required = true) {
  if (!fs.existsSync(filePath)) {
    if (required) {
      throw new Error(`Required XML resource is missing: ${path.relative(ROOT_DIR, filePath)}`);
    }
    return null;
  }
  return parseXml(fs.readFileSync(filePath, 'utf8'), path.relative(ROOT_DIR, filePath));
}

function childElements(node, name) {
  return node.children.filter((child) => child.name === name);
}

function getAndroidAttr(node, name) {
  return node.attrs[`android:${name}`];
}

function assertSupportedAttrs(node, supported, context) {
  for (const name of Object.keys(node.attrs)) {
    if (name.startsWith('android:') && !supported.has(name.slice('android:'.length))) {
      throw new Error(`Unsupported android:${name.slice('android:'.length)} attribute in ${context}`);
    }
  }
}

function parseXmlColor(raw, context) {
  const value = raw.trim();
  if (value === '@android:color/transparent' || value === '@null') {
    return { hex: '#000000', alpha: 0, rawAlpha: 0 };
  }
  const match = value.match(/^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{4}|[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$/);
  if (!match) {
    throw new Error(`Unsupported color value in ${context}: ${raw}`);
  }
  const digits = match[1].toUpperCase();
  const expanded = digits.length <= 4
    ? [...digits].map((digit) => digit + digit).join('')
    : digits;
  const hasAlpha = expanded.length === 8;
  const rawAlpha = hasAlpha ? parseInt(expanded.slice(0, 2), 16) : 0xFF;
  const rgb = hasAlpha ? expanded.slice(2) : expanded;
  return { hex: `#${rgb}`, alpha: rawAlpha / 255, rawAlpha };
}

function readColorResourceStrings(filePath, required) {
  const root = readXml(filePath, required);
  if (!root) return new Map();
  if (root.name !== 'resources') {
    throw new Error(`Expected <resources> in ${path.relative(ROOT_DIR, filePath)}`);
  }
  const resources = new Map();
  for (const colorNode of childElements(root, 'color')) {
    const name = colorNode.attrs.name;
    const value = colorNode.text.trim();
    if (!name || !value || resources.has(name)) {
      throw new Error(`Invalid or duplicate color resource in ${path.relative(ROOT_DIR, filePath)}`);
    }
    resources.set(name, value);
  }
  return resources;
}

function resolveColor(raw, resources, context, seen = new Set()) {
  const value = raw?.trim();
  const reference = value?.match(/^@color\/([A-Za-z0-9_]+)$/);
  if (!reference) {
    return parseXmlColor(value, context);
  }
  const name = reference[1];
  if (!resources.has(name)) {
    throw new Error(`Unknown color resource @color/${name} in ${context}`);
  }
  if (seen.has(name)) {
    throw new Error(`Circular color resource reference @color/${name} in ${context}`);
  }
  const nextSeen = new Set(seen);
  nextSeen.add(name);
  return resolveColor(resources.get(name), resources, context, nextSeen);
}

function parseColorValue(raw, resources, context) {
  const color = resolveColor(raw, resources, context);
  return {
    type: 'solid',
    hex: color.hex,
    alpha: color.alpha,
    rawAlpha: color.rawAlpha,
    isBackgroundParam: false,
  };
}

function parseGradient(node, resources, context) {
  assertSupportedAttrs(node, new Set([
    'type', 'startX', 'startY', 'endX', 'endY', 'centerX', 'centerY', 'gradientRadius', 'tileMode',
  ]), context);
  const type = getAndroidAttr(node, 'type');
  const tileModes = { clamp: 'pad', mirror: 'reflect', repeat: 'repeat' };
  const tileMode = getAndroidAttr(node, 'tileMode') || 'clamp';
  const spreadMethod = tileModes[tileMode];
  if (!spreadMethod) {
    throw new Error(`Unsupported gradient tileMode in ${context}: ${tileMode}`);
  }
  const stops = childElements(node, 'item').map((item, index) => {
    assertSupportedAttrs(item, new Set(['offset', 'color']), `${context} stop ${index + 1}`);
    const offset = getAndroidAttr(item, 'offset');
    const color = getAndroidAttr(item, 'color');
    if (offset === undefined || color === undefined) {
      throw new Error(`Gradient stop is missing offset or color in ${context}`);
    }
    const parsedColor = resolveColor(color, resources, context);
    return { offset: parseNumber(offset, `${context} stop offset`), hex: parsedColor.hex, alpha: parsedColor.alpha };
  });
  if (stops.length === 0) {
    throw new Error(`Gradient has no color stops in ${context}`);
  }

  if (type === 'linear') {
    return {
      type: 'linearGradient',
      start: {
        x: parseNumber(getAndroidAttr(node, 'startX'), `${context} startX`),
        y: parseNumber(getAndroidAttr(node, 'startY'), `${context} startY`),
      },
      end: {
        x: parseNumber(getAndroidAttr(node, 'endX'), `${context} endX`),
        y: parseNumber(getAndroidAttr(node, 'endY'), `${context} endY`),
      },
      spreadMethod,
      stops,
    };
  }
  if (type === 'radial') {
    return {
      type: 'radialGradient',
      center: {
        x: parseNumber(getAndroidAttr(node, 'centerX'), `${context} centerX`),
        y: parseNumber(getAndroidAttr(node, 'centerY'), `${context} centerY`),
      },
      radius: parseNumber(getAndroidAttr(node, 'gradientRadius'), `${context} gradientRadius`),
      spreadMethod,
      stops,
    };
  }
  throw new Error(`Unsupported gradient type in ${context}: ${type}`);
}

function parseBrush(node, colorAttr, resources, context) {
  const directColor = getAndroidAttr(node, colorAttr);
  const allCustomAttrs = childElements(node, 'aapt:attr');
  if (allCustomAttrs.some((attrNode) => !['android:fillColor', 'android:strokeColor'].includes(attrNode.attrs.name))) {
    throw new Error(`Unexpected aapt attribute in ${context}`);
  }
  const customAttrs = allCustomAttrs.filter((attrNode) => attrNode.attrs.name === `android:${colorAttr}`);
  if (customAttrs.length > 1) {
    throw new Error(`Multiple aapt attributes in ${context}`);
  }
  if (customAttrs.length === 0) {
    return directColor === undefined ? null : parseColorValue(directColor, resources, context);
  }
  const attrNode = customAttrs[0];
  if (directColor !== undefined) {
    throw new Error(`Both android:${colorAttr} and aapt:${colorAttr} are set in ${context}`);
  }
  if (attrNode.children.length !== 1 || !['gradient'].includes(attrNode.children[0].name)) {
    throw new Error(`Expected one gradient child for ${colorAttr} in ${context}`);
  }
  return parseGradient(attrNode.children[0], resources, context);
}

const ROOT_ATTRS = new Set(['name', 'width', 'height', 'viewportWidth', 'viewportHeight', 'autoMirrored', 'tint', 'tintMode', 'alpha']);
const GROUP_ATTRS = new Set(['name', 'rotation', 'pivotX', 'pivotY', 'scaleX', 'scaleY', 'translateX', 'translateY', 'alpha']);
const PATH_ATTRS = new Set([
  'name', 'pathData', 'fillColor', 'fillAlpha', 'strokeColor', 'strokeAlpha', 'strokeWidth',
  'strokeLineCap', 'strokeLineJoin', 'strokeMiterLimit', 'fillType', 'trimPathStart', 'trimPathEnd', 'trimPathOffset',
]);

function parsePathNode(node, resources, context) {
  assertSupportedAttrs(node, PATH_ATTRS, context);
  const d = getAndroidAttr(node, 'pathData');
  if (!d) {
    throw new Error(`Path is missing android:pathData in ${context}`);
  }
  const fill = parseBrush(node, 'fillColor', resources, context);
  const stroke = parseBrush(node, 'strokeColor', resources, context);
  const fillAlpha = getAndroidAttr(node, 'fillAlpha') === undefined ? 1 : parseNumber(getAndroidAttr(node, 'fillAlpha'), `${context} fillAlpha`);
  const strokeAlpha = getAndroidAttr(node, 'strokeAlpha') === undefined ? 1 : parseNumber(getAndroidAttr(node, 'strokeAlpha'), `${context} strokeAlpha`);
  const strokeLineWidth = getAndroidAttr(node, 'strokeWidth') === undefined ? 0 : parseNumber(getAndroidAttr(node, 'strokeWidth'), `${context} strokeWidth`);
  const trimValues = ['trimPathStart', 'trimPathEnd', 'trimPathOffset'].map((key, index) => {
    const defaults = [0, 1, 0];
    const raw = getAndroidAttr(node, key);
    return raw === undefined ? defaults[index] : parseNumber(raw, `${context} ${key}`);
  });
  if (trimValues[0] !== 0 || trimValues[1] !== 1 || trimValues[2] !== 0) {
    throw new Error(`Unsupported non-default trimPath values in ${context}`);
  }
  if (fillAlpha < 0 || fillAlpha > 1 || strokeAlpha < 0 || strokeAlpha > 1 || strokeLineWidth < 0) {
    throw new Error(`Invalid path alpha or stroke width in ${context}`);
  }
  const fillType = getAndroidAttr(node, 'fillType') || 'nonZero';
  if (fillType !== 'nonZero' && fillType !== 'evenOdd') {
    throw new Error(`Unsupported fillType in ${context}: ${fillType}`);
  }
  const pathName = getAndroidAttr(node, 'name');
  const isBackgroundParam = pathName === 'coui_background';
  if (isBackgroundParam && (!fill || fill.type !== 'solid')) {
    throw new Error(`Background path must use a solid fill in ${context}`);
  }
  if (isBackgroundParam) {
    fill.isBackgroundParam = true;
  }
  return {
    type: 'path',
    d,
    fill,
    fillAlpha,
    stroke,
    strokeAlpha,
    strokeLineWidth,
    strokeLineCap: getAndroidAttr(node, 'strokeLineCap') || 'butt',
    strokeLineJoin: getAndroidAttr(node, 'strokeLineJoin') || 'miter',
    strokeLineMiter: getAndroidAttr(node, 'strokeMiterLimit') === undefined
      ? 4
      : parseNumber(getAndroidAttr(node, 'strokeMiterLimit'), `${context} strokeMiterLimit`),
    fillRule: fillType === 'evenOdd' ? 'evenodd' : 'nonzero',
  };
}

function parseClipPath(node, context) {
  assertSupportedAttrs(node, new Set(['name', 'pathData', 'fillType']), context);
  const d = getAndroidAttr(node, 'pathData');
  if (!d) {
    throw new Error(`Clip path is missing android:pathData in ${context}`);
  }
  const fillType = getAndroidAttr(node, 'fillType') || 'nonZero';
  if (fillType !== 'nonZero' && fillType !== 'evenOdd') {
    throw new Error(`Unsupported clip path fillType in ${context}: ${fillType}`);
  }
  return { d, fillRule: fillType === 'evenOdd' ? 'evenodd' : 'nonzero' };
}

function numberAttr(node, name, fallback, context) {
  const raw = getAndroidAttr(node, name);
  return raw === undefined ? fallback : parseNumber(raw, `${context} ${name}`);
}

function parseGroupNode(node, resources, commands, context) {
  assertSupportedAttrs(node, GROUP_ATTRS, context);
  const alpha = numberAttr(node, 'alpha', 1, context);
  if (alpha !== 1) {
    throw new Error(`Unsupported group alpha in ${context}: ${alpha}`);
  }
  const clipNodes = childElements(node, 'clip-path');
  if (clipNodes.length > 1 || (clipNodes.length === 1 && node.children[0] !== clipNodes[0])) {
    throw new Error(`Clip paths must be the first and only clip child in ${context}`);
  }
  const clip = clipNodes.length === 1 ? parseClipPath(clipNodes[0], `${context} clip path`) : null;
  const command = {
    type: 'addGroup',
    rotate: numberAttr(node, 'rotation', 0, context),
    pivotX: numberAttr(node, 'pivotX', 0, context),
    pivotY: numberAttr(node, 'pivotY', 0, context),
    scaleX: numberAttr(node, 'scaleX', 1, context),
    scaleY: numberAttr(node, 'scaleY', 1, context),
    translationX: numberAttr(node, 'translateX', 0, context),
    translationY: numberAttr(node, 'translateY', 0, context),
    clipPathD: clip?.d || null,
    clipPathFillRule: clip?.fillRule || 'nonzero',
  };
  commands.push(command);
  for (const child of node.children) {
    if (child.name === 'clip-path') continue;
    if (child.name === 'group') {
      parseGroupNode(child, resources, commands, `${context} group`);
    } else if (child.name === 'path') {
      commands.push(parsePathNode(child, resources, `${context} path`));
    } else {
      throw new Error(`Unsupported vector element <${child.name}> in ${context}`);
    }
  }
  commands.push({ type: 'clearGroup' });
}

function parseVectorXml(filePath, resources) {
  const root = readXml(filePath);
  const context = path.relative(ROOT_DIR, filePath);
  if (root.name !== 'vector') {
    throw new Error(`Expected <vector> in ${context}`);
  }
  assertSupportedAttrs(root, ROOT_ATTRS, context);
  const name = getAndroidAttr(root, 'name');
  if (!name || !/^[A-Z][A-Za-z0-9]*$/.test(name)) {
    throw new Error(`Vector is missing a PascalCase android:name in ${context}`);
  }
  const tintMode = getAndroidAttr(root, 'tintMode');
  if (tintMode !== undefined && tintMode !== 'src_in' && tintMode !== 'srcIn') {
    throw new Error(`Unsupported vector tintMode in ${context}: ${tintMode}`);
  }
  if (numberAttr(root, 'alpha', 1, context) !== 1) {
    throw new Error(`Unsupported vector alpha in ${context}`);
  }
  const commands = [];
  for (const child of root.children) {
    if (child.name === 'path') {
      commands.push(parsePathNode(child, resources, `${context} path`));
    } else if (child.name === 'group') {
      parseGroupNode(child, resources, commands, `${context} group`);
    } else {
      throw new Error(`Unsupported vector element <${child.name}> in ${context}`);
    }
  }
  const tintValue = getAndroidAttr(root, 'tint');
  const tintColor = tintValue === undefined || tintValue === '@null'
    ? { type: 'unspecified' }
    : { type: 'color', ...resolveColor(tintValue, resources, `${context} tint`) };
  return {
    name,
    width: parseDimension(getAndroidAttr(root, 'width'), `${context} width`),
    height: parseDimension(getAndroidAttr(root, 'height'), `${context} height`),
    viewportWidth: parseNumber(getAndroidAttr(root, 'viewportWidth'), `${context} viewportWidth`),
    viewportHeight: parseNumber(getAndroidAttr(root, 'viewportHeight'), `${context} viewportHeight`),
    autoMirror: getAndroidAttr(root, 'autoMirrored') === 'true',
    tintColor,
    commands,
  };
}

function buildGroupTransform(cmd) {
  const parts = [];
  if (cmd.translationX !== 0 || cmd.translationY !== 0) {
    parts.push(`translate(${formatNum(cmd.translationX)} ${formatNum(cmd.translationY)})`);
  }
  if (cmd.rotate !== 0) {
    if (cmd.pivotX !== 0 || cmd.pivotY !== 0) {
      parts.push(`rotate(${formatNum(cmd.rotate)} ${formatNum(cmd.pivotX)} ${formatNum(cmd.pivotY)})`);
    } else {
      parts.push(`rotate(${formatNum(cmd.rotate)})`);
    }
  }
  if (cmd.scaleX !== 1 || cmd.scaleY !== 1) {
    if (cmd.pivotX !== 0 || cmd.pivotY !== 0) {
      parts.push(`translate(${formatNum(cmd.pivotX)} ${formatNum(cmd.pivotY)})`);
      parts.push(`scale(${formatNum(cmd.scaleX)} ${formatNum(cmd.scaleY)})`);
      parts.push(`translate(${formatNum(-cmd.pivotX)} ${formatNum(-cmd.pivotY)})`);
    } else {
      parts.push(`scale(${formatNum(cmd.scaleX)} ${formatNum(cmd.scaleY)})`);
    }
  }
  return parts.join(' ');
}

function renderSvg(vectorSpec, iconName, style, themeKey, mode) {
  const defs = [];
  const bodyElements = [];
  let gradCounter = 0;
  let clipCounter = 0;
  const idPrefix = `coui-${iconName}-${style.toLowerCase()}-${themeKey.toLowerCase()}`;
  const hasTintColor = vectorSpec.tintColor && vectorSpec.tintColor.type === 'color';
  const tintHex = hasTintColor ? vectorSpec.tintColor.hex : null;

  function registerBrushDef(brush) {
    gradCounter++;
    const gradId = `${idPrefix}-grad-${gradCounter}`;
    const stopsXml = brush.stops.map((stop) => {
      const attrs = [`offset="${formatNum(stop.offset)}"`, `stop-color="${stop.hex}"`];
      if (stop.alpha < 1) attrs.push(`stop-opacity="${formatNum(stop.alpha)}"`);
      return `<stop ${attrs.join(' ')}/>`;
    }).join('');
    if (brush.type === 'linearGradient') {
      const attrs = [
        `id="${gradId}"`, `x1="${formatNum(brush.start.x)}"`, `y1="${formatNum(brush.start.y)}"`,
        `x2="${formatNum(brush.end.x)}"`, `y2="${formatNum(brush.end.y)}"`, 'gradientUnits="userSpaceOnUse"',
      ];
      if (brush.spreadMethod !== 'pad') attrs.push(`spreadMethod="${brush.spreadMethod}"`);
      defs.push(`<linearGradient ${attrs.join(' ')}>${stopsXml}</linearGradient>`);
      return `url(#${gradId})`;
    }
    if (brush.type === 'radialGradient') {
      const attrs = [
        `id="${gradId}"`, `cx="${formatNum(brush.center.x)}"`, `cy="${formatNum(brush.center.y)}"`,
        `r="${formatNum(brush.radius)}"`, 'gradientUnits="userSpaceOnUse"',
      ];
      if (brush.spreadMethod !== 'pad') attrs.push(`spreadMethod="${brush.spreadMethod}"`);
      defs.push(`<radialGradient ${attrs.join(' ')}>${stopsXml}</radialGradient>`);
      return `url(#${gradId})`;
    }
    throw new Error(`Unknown gradient brush type: ${brush.type}`);
  }

  for (const command of vectorSpec.commands) {
    if (command.type === 'addGroup') {
      const attrs = [];
      const transform = buildGroupTransform(command);
      if (transform) attrs.push(`transform="${transform}"`);
      if (command.clipPathD) {
        clipCounter++;
        const clipId = `${idPrefix}-clip-${clipCounter}`;
        const clipRule = command.clipPathFillRule === 'evenodd' ? ' fill-rule="evenodd" clip-rule="evenodd"' : '';
        defs.push(`<clipPath id="${clipId}"><path d="${command.clipPathD}"${clipRule}/></clipPath>`);
        attrs.push(`clip-path="url(#${clipId})"`);
      }
      bodyElements.push(attrs.length > 0 ? `<g ${attrs.join(' ')}>` : '<g>');
    } else if (command.type === 'clearGroup') {
      bodyElements.push('</g>');
    } else if (command.type === 'path') {
      const attrs = [`d="${command.d}"`];
      if (!command.fill || (command.fill.type === 'solid' && command.fill.rawAlpha === 0)) {
        attrs.push('fill="none"');
      } else if (command.fill.type === 'solid') {
        const fillColor = mode === 'tint' && tintHex ? tintHex : command.fill.hex;
        attrs.push(`fill="${fillColor}"`);
        if (mode === 'tint' || (mode === 'background' && command.fill.isBackgroundParam)) {
          attrs.push('data-coui-fill="true"');
        }
        const alpha = command.fillAlpha * command.fill.alpha;
        if (alpha < 1) attrs.push(`fill-opacity="${formatNum(alpha)}"`);
      } else {
        if (mode === 'tint') throw new Error(`Unexpected gradient fill in tint-mode icon ${iconName}`);
        attrs.push(`fill="${registerBrushDef(command.fill)}"`);
        if (command.fillAlpha < 1) attrs.push(`fill-opacity="${formatNum(command.fillAlpha)}"`);
      }

      if (command.stroke && command.strokeLineWidth > 0 && !(command.stroke.type === 'solid' && command.stroke.rawAlpha === 0)) {
        if (command.stroke.type === 'solid') {
          const strokeColor = mode === 'tint' && tintHex ? tintHex : command.stroke.hex;
          attrs.push(`stroke="${strokeColor}"`);
          if (mode === 'tint' || (mode === 'background' && command.stroke.isBackgroundParam)) {
            attrs.push('data-coui-stroke="true"');
          }
          const alpha = command.strokeAlpha * command.stroke.alpha;
          if (alpha < 1) attrs.push(`stroke-opacity="${formatNum(alpha)}"`);
        } else {
          attrs.push(`stroke="${registerBrushDef(command.stroke)}"`);
          if (command.strokeAlpha < 1) attrs.push(`stroke-opacity="${formatNum(command.strokeAlpha)}"`);
        }
        attrs.push(`stroke-width="${formatNum(command.strokeLineWidth)}"`);
        attrs.push(`stroke-linecap="${command.strokeLineCap}"`);
        attrs.push(`stroke-linejoin="${command.strokeLineJoin}"`);
        if (command.strokeLineJoin === 'miter' && command.strokeLineMiter !== 4) {
          attrs.push(`stroke-miterlimit="${formatNum(command.strokeLineMiter)}"`);
        }
      }
      if (command.fillRule === 'evenodd') attrs.push('fill-rule="evenodd" clip-rule="evenodd"');
      bodyElements.push(`<path ${attrs.join(' ')}/>`);
    }
  }

  const attrs = [
    'xmlns="http://www.w3.org/2000/svg"',
    `width="${formatNum(vectorSpec.width)}"`,
    `height="${formatNum(vectorSpec.height)}"`,
    `viewBox="0 0 ${formatNum(vectorSpec.viewportWidth)} ${formatNum(vectorSpec.viewportHeight)}"`,
    'fill="none"',
  ];
  const defsXml = defs.length > 0 ? `<defs>${defs.join('')}</defs>` : '';
  return `<svg ${attrs.join(' ')}>${defsXml}${bodyElements.join('')}</svg>`;
}

function snakeCase(name) {
  return name
    .replace(/([A-Z]+)([A-Z][a-z])/g, '$1_$2')
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .toLowerCase();
}

function readIconFiles(directory, required) {
  if (!fs.existsSync(directory)) {
    if (required) throw new Error(`Required icon directory is missing: ${path.relative(ROOT_DIR, directory)}`);
    return new Map();
  }
  const files = new Map();
  for (const filename of fs.readdirSync(directory).sort()) {
    if (!filename.endsWith('.xml')) continue;
    const match = filename.match(/^coui_(.+)_(filled|outlined)\.xml$/);
    if (!match) continue;
    const [, resourceName, styleName] = match;
    const style = styleName === 'filled' ? 'Filled' : 'Outlined';
    const key = `${resourceName}:${style}`;
    if (files.has(key)) throw new Error(`Duplicate icon resource: ${filename}`);
    files.set(key, { resourceName, style, filePath: path.join(directory, filename) });
  }
  return files;
}

function verifyTintPaletteResources(couiColors, dayResources, nightResources) {
  const resourceNames = {
    BLUE: 'coui_icon_tint_blue',
    GREEN: 'coui_icon_tint_green',
    YELLOW: 'coui_icon_tint_yellow',
    RED: 'coui_icon_tint_red',
    ORANGE: 'coui_icon_tint_orange',
  };
  for (const [theme, resources] of [['Day', dayResources], ['Night', nightResources]]) {
    for (const [key, name] of Object.entries(resourceNames)) {
      if (!(key in couiColors[theme]) || !resources.has(name)) {
        throw new Error(`Missing ${theme} color for ${name} or CouiColor.${theme}.${key}`);
      }
      const resourceColor = resolveColor(resources.get(name), resources, `${theme} ${name}`);
      if (resourceColor.alpha !== 1 || resourceColor.hex !== couiColors[theme][key]) {
        throw new Error(`${theme} ${name} does not match CouiColor.${theme}.${key}`);
      }
    }
  }
}

function parseResourceColorFile(filePath, required) {
  return readColorResourceStrings(filePath, required);
}

function getShapeSignature(spec) {
  return JSON.stringify(spec.commands.map((command) => {
    if (command.type === 'addGroup') {
      return [command.type, command.rotate, command.pivotX, command.pivotY, command.scaleX, command.scaleY,
        command.translationX, command.translationY, command.clipPathD, command.clipPathFillRule];
    }
    if (command.type === 'clearGroup') return [command.type];
    return [command.type, command.d, command.fillRule, command.fill !== null, command.stroke !== null,
      command.fillAlpha, command.strokeAlpha, command.strokeLineWidth, command.strokeLineCap,
      command.strokeLineJoin, command.strokeLineMiter];
  }));
}

function readBitmapIcon(name, resource) {
  const fileName = `${resource}.png`;
  const dayPath = path.join(RESOURCE_DIR, 'drawable-nodpi', fileName);
  const nightPath = path.join(RESOURCE_DIR, 'drawable-night-nodpi', fileName);
  const encode = (file) => `data:image/png;base64,${fs.readFileSync(file).toString('base64')}`;
  const day = encode(dayPath);
  return { name, style: 'Bitmap', mode: 'original', day, night: fs.existsSync(nightPath) ? encode(nightPath) : day };
}

function validateGeneratedPayload({ icons, colors }) {
  if (icons.length === 0) throw new Error('Self-check failed: generated icon list is empty');
  if (!Object.keys(colors.Day).length || !Object.keys(colors.Night).length) {
    throw new Error('Self-check failed: Day or Night color resources are empty');
  }
  const seen = new Set();
  for (const icon of icons) {
    const key = `${icon.style}:${icon.name}`;
    if (seen.has(key)) throw new Error(`Self-check failed: duplicate icon ${key}`);
    seen.add(key);
    if (icon.style === 'Bitmap') continue;
    if (!icon.day.startsWith('<svg') || !icon.day.includes('<path') || !icon.night.startsWith('<svg') || !icon.night.includes('<path')) {
      throw new Error(`Self-check failed: missing day/night SVG for ${key}`);
    }
    if (!icon.vector.day.commands.length || !icon.vector.night.commands.length) {
      throw new Error(`Self-check failed: missing ImageVector commands for ${key}`);
    }
  }
  const appUpdate = icons.find((icon) => icon.name === 'AppUpdate');
  if (appUpdate) {
    if (appUpdate.mode !== 'background') throw new Error('Self-check failed: AppUpdate must use background mode');
    for (const theme of ['day', 'night']) {
      const commands = appUpdate.vector[theme].commands.filter((command) => command.type === 'path');
      const backgroundPaths = commands.filter((command) => command.fill?.isBackgroundParam);
      const whiteForegrounds = commands.filter((command) =>
        (command.fill?.type === 'solid' && command.fill.hex === '#FFFFFF' && command.fill.alpha === 1) ||
        (command.stroke?.type === 'solid' && command.stroke.hex === '#FFFFFF' && command.stroke.alpha === 1));
      if (backgroundPaths.length !== 1 || whiteForegrounds.length === 0) {
        throw new Error(`Self-check failed: AppUpdate ${theme} needs one adjustable background and white foreground`);
      }
    }
  }
}

function main() {
  const couiColors = parseCouiColors(fs.readFileSync(COUI_COLOR_PATH, 'utf8'));
  const { order: iconOrder, aliases, sharedResources, bitmapResources } = parseIconOrder(fs.readFileSync(COUI_ICONS_PATH, 'utf8'));
  const rawDayColors = parseResourceColorFile(DAY_COLORS_PATH, true);
  const rawNightColors = new Map(rawDayColors);
  for (const [name, value] of parseResourceColorFile(NIGHT_COLORS_PATH, false)) {
    rawNightColors.set(name, value);
  }
  verifyTintPaletteResources(couiColors, rawDayColors, rawNightColors);

  const dayFiles = readIconFiles(DRAWABLE_DIR, true);
  const nightFiles = readIconFiles(DRAWABLE_NIGHT_DIR, false);
  const specs = new Map();
  for (const [key, dayFile] of dayFiles) {
    const nightFile = nightFiles.get(key);
    const daySpec = parseVectorXml(dayFile.filePath, rawDayColors);
    if (snakeCase(daySpec.name) !== dayFile.resourceName) {
      throw new Error(`Resource ${path.basename(dayFile.filePath)} does not match vector name ${daySpec.name}`);
    }
    const nightSpec = nightFile ? parseVectorXml(nightFile.filePath, rawNightColors) : parseVectorXml(dayFile.filePath, rawNightColors);
    if (nightSpec.name !== daySpec.name) {
      throw new Error(`Day/night vector names differ for ${daySpec.name}: ${nightSpec.name}`);
    }
    specs.set(key, { name: daySpec.name, style: dayFile.style, daySpec, nightSpec });
  }
  for (const key of nightFiles.keys()) {
    if (!dayFiles.has(key)) throw new Error(`Night icon resource has no day resource: ${key}`);
  }

  for (const [name, resource] of sharedResources) {
    const match = resource.match(/^coui_(.+)_(filled|outlined)$/);
    if (!match || match[1] !== snakeCase(name)) throw new Error(`Invalid Monochrome resource for ${name}: ${resource}`);
    const sourceStyle = match[2] === 'filled' ? 'Filled' : 'Outlined';
    const source = specs.get(`${match[1]}:${sourceStyle}`);
    if (!source) throw new Error(`Missing Monochrome resource for ${name}: ${resource}`);
    for (const style of ['Filled', 'Outlined']) {
      if (!aliases[style].has(name)) throw new Error(`CouiIcons.${style}.${name} must refer to Monochrome.${name}`);
      specs.set(`${match[1]}:${style}`, { ...source, style, source: name });
    }
  }
  for (const style of ['Filled', 'Outlined']) {
    for (const name of aliases[style]) {
      if (!sharedResources.has(name)) throw new Error(`Missing Monochrome icon ${name}`);
    }
  }

  const entries = [...specs.values()];
  const orderIndices = {};
  for (const style of ['Filled', 'Outlined']) {
    orderIndices[style] = new Map(iconOrder[style].map((name, index) => [name, index]));
    const styleEntries = entries.filter((entry) => entry.style === style);
    if (styleEntries.length !== iconOrder[style].length || styleEntries.some((entry) => !orderIndices[style].has(entry.name))) {
      throw new Error(`CouiIcons.${style} declarations do not match XML icon resources`);
    }
  }
  const styleOrder = { Filled: 0, Outlined: 1 };
  entries.sort((a, b) => styleOrder[a.style] - styleOrder[b.style] || orderIndices[a.style].get(a.name) - orderIndices[b.style].get(b.name));
  for (const entry of entries) {
    if (entry.style !== 'Filled' && entry.style !== 'Outlined') {
      throw new Error(`Unsupported icon style: ${entry.style}`);
    }
    if (!entry.daySpec.commands.some((command) => command.type === 'path')) {
      throw new Error(`Icon has no paths: ${entry.name} (${entry.style})`);
    }
  }

  const originals = new Set(['AI', 'Google', 'SettingDetail', 'Phone']);
  const byName = new Map();
  for (const entry of entries) {
    if (!byName.has(entry.name)) byName.set(entry.name, []);
    byName.get(entry.name).push(entry);
  }
  for (const [name, variants] of byName) {
    if (variants.length === 2) {
      const [first, second] = variants;
      if (getShapeSignature(first.daySpec) === getShapeSignature(second.daySpec)) {
        first.source = name;
        second.source = name;
      }
    }
  }

  const icons = entries.map((entry) => {
    const backgroundPath = entry.daySpec.commands.some((command) => command.type === 'path' && command.fill?.isBackgroundParam);
    const mode = entry.name === 'AppUpdate' && backgroundPath
      ? 'background'
      : (originals.has(entry.name) || entry.daySpec.tintColor.type === 'unspecified' ? 'original' : 'tint');
    if (entry.name === 'AppUpdate' && !backgroundPath) {
      throw new Error('AppUpdate is missing the coui_background path');
    }
    return {
      name: entry.name,
      style: entry.style,
      source: entry.source,
      day: renderSvg(entry.daySpec, entry.name, entry.style, 'Day', mode),
      night: renderSvg(entry.nightSpec, entry.name, entry.style, 'Night', mode),
      mode,
      vector: { day: entry.daySpec, night: entry.nightSpec },
    };
  });
  for (const [name, resource] of bitmapResources) icons.push(readBitmapIcon(name, resource));
  const payload = { icons, colors: couiColors };
  validateGeneratedPayload(payload);
  fs.writeFileSync(OUTPUT_DATA_PATH, `window.COUI_ICON_DATA = ${JSON.stringify(payload, null, 2)};\n`, 'utf8');
  console.log(`Generated and verified ${icons.length} icons to ${OUTPUT_DATA_PATH}`);
}

main();
