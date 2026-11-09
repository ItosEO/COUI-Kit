(function () {
  "use strict";

  const rawData = window.COUI_ICON_DATA || {};
  const iconVariants = Array.isArray(rawData.icons) ? rawData.icons : [];
  const variantsByName = new Map();
  iconVariants.forEach((icon) => {
    if (!variantsByName.has(icon.name)) variantsByName.set(icon.name, []);
    variantsByName.get(icon.name).push(icon);
  });
  const icons = Array.from(variantsByName.values(), (variants) =>
    variants.find((icon) => icon.style === "Filled") || variants[0]
  ).sort((a, b) => a.name.localeCompare(b.name, "en"));
  const colors = rawData.colors && typeof rawData.colors === "object"
    ? rawData.colors
    : { Day: {}, Night: {} };

  const state = {
    theme: document.documentElement.dataset.theme, // "day" | "night"
    query: "",
    styleFilter: "all",
    selectedIconName: null,
    selectedIconStyle: null,
    // colorSelection: null | { type: "preset", palette: "Day" | "Night", key: string, hex: string } | { type: "custom", hex: string }
    colorSelection: null
  };

  let copyFeedbackTimer = null;

  const dom = {
    styleFilterButtons: document.querySelectorAll("[data-style-filter]"),
    searchInput: document.getElementById("search-input"),
    searchClearBtn: document.getElementById("search-clear-btn"),
    iconCount: document.getElementById("icon-count"),
    iconGrid: document.getElementById("icon-grid"),
    emptyState: document.getElementById("empty-state"),
    emptyStateText: document.getElementById("empty-state-text"),
    detailPanel: document.querySelector(".detail-panel"),
    detailContent: document.querySelector(".detail-content"),
    detailCloseBtn: document.getElementById("detail-close-btn"),
    detailSvgContainer: document.getElementById("detail-svg-container"),
    detailName: document.getElementById("detail-name"),
    detailSubtitle: document.getElementById("detail-subtitle"),
    variantSwitch: document.getElementById("variant-switch"),
    variantFilledBtn: document.getElementById("variant-filled-btn"),
    variantOutlinedBtn: document.getElementById("variant-outlined-btn"),
    colorSection: document.querySelector(".color-section"),
    codeSection: document.querySelector(".code-section"),
    resetColorBtn: document.getElementById("reset-color-btn"),
    colorModeNotice: document.getElementById("color-mode-notice"),
    colorControls: document.getElementById("color-controls"),
    paletteDay: document.getElementById("palette-day"),
    paletteNight: document.getElementById("palette-night"),
    customColorInput: document.getElementById("custom-color-input"),
    customColorHex: document.getElementById("custom-color-hex"),
    customColorWrap: document.querySelector(".custom-color-picker-wrap"),
    copySvgBtn: document.getElementById("copy-svg-btn"),
    copyVectorBtn: document.getElementById("copy-vector-btn"),
    copyFeedback: document.getElementById("copy-feedback")
  };

  function getSelectedIcon() {
    if (!state.selectedIconName) {
      return null;
    }
    const variants = variantsByName.get(state.selectedIconName) || [];
    return variants.find((icon) => icon.style === state.selectedIconStyle)
      || variants.find((icon) => icon.style === "Filled")
      || variants[0]
      || null;
  }

  function getFilteredIcons() {
    const q = state.query.trim().toLowerCase();
    return icons.map((icon) => state.styleFilter === "all"
      ? icon
      : variantsByName.get(icon.name).find((variant) => variant.style === state.styleFilter)
    ).filter((icon) => icon && String(icon.name || "").toLowerCase().includes(q));
  }

  function getIconArtwork(icon) {
    if (!icon) {
      return "";
    }
    if (state.theme === "night") {
      return icon.night || icon.day || "";
    }
    return icon.day || icon.night || "";
  }

  function canRecolorIcon(icon) {
    if (!icon) {
      return false;
    }
    return icon.mode === "tint" || icon.mode === "background";
  }

  /**
   * 仅对带 data-coui-fill="true" 或 data-coui-stroke="true" 的 SVG 元素应用颜色，
   * 其余路径、渐变和透明度原样保留；original 模式禁止改色。
   */
  function buildSvgElement(icon, applySelectedColor) {
    const rawSvg = getIconArtwork(icon);
    const wrapper = document.createElement("div");
    wrapper.innerHTML = rawSvg.trim();
    const svg = wrapper.querySelector("svg");
    if (!svg) {
      return null;
    }

    svg.setAttribute("focusable", "false");
    svg.setAttribute("aria-hidden", "true");

    const activeHex = applySelectedColor && state.colorSelection && canRecolorIcon(icon)
      ? state.colorSelection.hex
      : null;

    if (activeHex) {
      const fillTargets = svg.querySelectorAll('[data-coui-fill="true"]');
      fillTargets.forEach((node) => {
        node.setAttribute("fill", activeHex);
      });

      const strokeTargets = svg.querySelectorAll('[data-coui-stroke="true"]');
      strokeTargets.forEach((node) => {
        node.setAttribute("stroke", activeHex);
      });
    }

    return svg;
  }

  function buildPreviewElement(icon, applySelectedColor) {
    if (icon?.style !== "Bitmap") return buildSvgElement(icon, applySelectedColor);
    const img = document.createElement("img");
    img.alt = "";
    img.src = getIconArtwork(icon);
    return img;
  }

  function kotlinFloat(value) {
    return `${value}f`;
  }

  function kotlinColor(hex, alpha) {
    const alphaHex = Math.round(alpha * 255).toString(16).padStart(2, "0").toUpperCase();
    return `Color(0x${alphaHex}${hex.slice(1).toUpperCase()})`;
  }

  function kotlinBrush(brush, overrideColor) {
    if (!brush) return "null";
    if (brush.type === "solid") {
      return `SolidColor(${kotlinColor(overrideColor || brush.hex, brush.alpha)})`;
    }
    const stops = brush.stops
      .map((stop) => `${kotlinFloat(stop.offset)} to ${kotlinColor(stop.hex, stop.alpha)}`)
      .join(", ");
    const tileModes = { pad: "Clamp", reflect: "Mirror", repeat: "Repeated" };
    const tileMode = tileModes[brush.spreadMethod];
    if (!tileMode) throw new Error("不支持的渐变平铺模式");
    if (brush.type === "linearGradient") {
      return `Brush.linearGradient(colorStops = arrayOf(${stops}), start = Offset(${kotlinFloat(brush.start.x)}, ${kotlinFloat(brush.start.y)}), end = Offset(${kotlinFloat(brush.end.x)}, ${kotlinFloat(brush.end.y)}), tileMode = TileMode.${tileMode})`;
    }
    if (brush.type === "radialGradient") {
      return `Brush.radialGradient(colorStops = arrayOf(${stops}), center = Offset(${kotlinFloat(brush.center.x)}, ${kotlinFloat(brush.center.y)}), radius = ${kotlinFloat(brush.radius)}, tileMode = TileMode.${tileMode})`;
    }
    throw new Error("不支持的图标画刷");
  }

  function buildImageVectorCode(icon) {
    const spec = icon.vector[state.theme];
    const tint = state.colorSelection && canRecolorIcon(icon)
      ? state.colorSelection.hex
      : spec.tintColor?.hex;
    const lines = [
      "import androidx.compose.ui.geometry.Offset",
      "import androidx.compose.ui.graphics.Brush",
      "import androidx.compose.ui.graphics.Color",
      "import androidx.compose.ui.graphics.PathFillType",
      "import androidx.compose.ui.graphics.SolidColor",
      "import androidx.compose.ui.graphics.StrokeCap",
      "import androidx.compose.ui.graphics.StrokeJoin",
      "import androidx.compose.ui.graphics.TileMode",
      "import androidx.compose.ui.graphics.vector.ImageVector",
      "import androidx.compose.ui.graphics.vector.PathParser",
      "import androidx.compose.ui.unit.dp",
      "",
      `val ${icon.name}: ImageVector = ImageVector.Builder(`,
      `    name = "${icon.name}",`,
      `    defaultWidth = ${kotlinFloat(spec.width)}.dp,`,
      `    defaultHeight = ${kotlinFloat(spec.height)}.dp,`,
      `    viewportWidth = ${kotlinFloat(spec.viewportWidth)},`,
      `    viewportHeight = ${kotlinFloat(spec.viewportHeight)},`,
      `    autoMirror = ${Boolean(spec.autoMirror)},`,
      ").apply {"
    ];
    const caps = { butt: "Butt", round: "Round", square: "Square" };
    const joins = { miter: "Miter", round: "Round", bevel: "Bevel" };

    for (const command of spec.commands) {
      if (command.type === "addGroup") {
        lines.push(
          "    addGroup(",
          `        rotate = ${kotlinFloat(command.rotate)},`,
          `        pivotX = ${kotlinFloat(command.pivotX)},`,
          `        pivotY = ${kotlinFloat(command.pivotY)},`,
          `        scaleX = ${kotlinFloat(command.scaleX)},`,
          `        scaleY = ${kotlinFloat(command.scaleY)},`,
          `        translationX = ${kotlinFloat(command.translationX)},`,
          `        translationY = ${kotlinFloat(command.translationY)},`
        );
        if (command.clipPathD) {
          lines.push(`        clipPathData = PathParser().parsePathString(${JSON.stringify(command.clipPathD)}).toNodes(),`);
        }
        lines.push("    )");
      } else if (command.type === "clearGroup") {
        lines.push("    clearGroup()");
      } else if (command.type === "path") {
        const fillColor = icon.mode === "tint" || command.fill?.isBackgroundParam ? tint : null;
        const strokeColor = icon.mode === "tint" || command.stroke?.isBackgroundParam ? tint : null;
        lines.push(
          "    addPath(",
          `        pathData = PathParser().parsePathString(${JSON.stringify(command.d)}).toNodes(),`,
          `        pathFillType = PathFillType.${command.fillRule === "evenodd" ? "EvenOdd" : "NonZero"},`,
          `        fill = ${kotlinBrush(command.fill, fillColor)},`,
          `        fillAlpha = ${kotlinFloat(command.fillAlpha)},`,
          `        stroke = ${kotlinBrush(command.stroke, strokeColor)},`,
          `        strokeAlpha = ${kotlinFloat(command.strokeAlpha)},`,
          `        strokeLineWidth = ${kotlinFloat(command.strokeLineWidth)},`,
          `        strokeLineCap = StrokeCap.${caps[command.strokeLineCap]},`,
          `        strokeLineJoin = StrokeJoin.${joins[command.strokeLineJoin]},`,
          `        strokeLineMiter = ${kotlinFloat(command.strokeLineMiter)},`,
          "    )"
        );
      } else {
        throw new Error("不支持的图标路径");
      }
    }
    lines.push("}.build()");
    return lines.join("\n");
  }

  const PALETTE_KEYS = ["BLUE", "GREEN", "YELLOW", "RED", "ORANGE", "CYAN_BLUE", "GRAY", "DARK_GRAY"];

  /**
   * 在驼峰单词边界插入零宽空格，使长名称仅在词边界自然换行，避免在单词中间断行。
   */
  function formatWrappableName(name) {
    return String(name || "")
      .replace(/([a-z0-9])([A-Z])/g, "$1\u200B$2")
      .replace(/([A-Z]+)([A-Z][a-z])/g, "$1\u200B$2");
  }

  function renderPalettes() {
    renderPaletteGroup(dom.paletteDay, "Day", colors.Day || {});
    renderPaletteGroup(dom.paletteNight, "Night", colors.Night || {});
  }

  function renderPaletteGroup(container, paletteName, paletteObj) {
    container.innerHTML = "";
    const entries = PALETTE_KEYS
      .filter((key) => Object.prototype.hasOwnProperty.call(paletteObj, key))
      .map((key) => [key, paletteObj[key]]);

    if (entries.length === 0) {
      const empty = document.createElement("span");
      empty.className = "palette-title";
      empty.textContent = "暂无色板数据";
      container.appendChild(empty);
      return;
    }

    entries.forEach(([key, hexValue]) => {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = "swatch-btn";
      btn.style.backgroundColor = hexValue;
      btn.dataset.palette = paletteName;
      btn.dataset.key = key;
      btn.dataset.hex = hexValue;
      const label = `CouiColor.${paletteName}.${key} (${hexValue})`;
      btn.setAttribute("aria-label", label);
      btn.setAttribute("title", label);
      btn.setAttribute("aria-pressed", "false");

      btn.addEventListener("click", () => {
        const icon = getSelectedIcon();
        if (!canRecolorIcon(icon)) {
          return;
        }
        state.colorSelection = {
          type: "preset",
          palette: paletteName,
          key: key,
          hex: hexValue
        };
        updateDetailPanel();
      });

      container.appendChild(btn);
    });
  }

  function renderGrid() {
    const filtered = getFilteredIcons();
    dom.iconCount.textContent = `${filtered.length} 个图标`;
    dom.iconGrid.innerHTML = "";

    if (icons.length === 0) {
      dom.emptyState.hidden = false;
      dom.emptyStateText.textContent = "未加载到图标数据（请先生成 Pages/data.js）";
      return;
    }

    if (filtered.length === 0) {
      dom.emptyState.hidden = false;
      dom.emptyStateText.textContent = state.query.trim()
        ? `未找到包含 “${state.query.trim()}” 的图标`
        : "没有符合当前样式的图标";
      return;
    }

    dom.emptyState.hidden = true;

    const fragment = document.createDocumentFragment();
    filtered.forEach((icon) => {
      const isSelected = icon.name === state.selectedIconName;
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = `icon-card${isSelected ? " is-selected" : ""}`;
      btn.setAttribute("aria-label", `${icon.name} 图标`);
      btn.setAttribute("aria-haspopup", "dialog");
      btn.setAttribute("aria-controls", "detail-panel");
      btn.setAttribute("aria-expanded", isSelected ? "true" : "false");
      btn.dataset.iconName = icon.name;

      const svgBox = document.createElement("div");
      svgBox.className = "icon-card-svg";
      const preview = buildPreviewElement(isSelected && state.styleFilter === "all" ? getSelectedIcon() : icon, false);
      if (preview) {
        svgBox.appendChild(preview);
      }

      const nameSpan = document.createElement("span");
      nameSpan.className = "icon-card-name";
      nameSpan.textContent = formatWrappableName(icon.name);

      btn.appendChild(svgBox);
      btn.appendChild(nameSpan);

      btn.addEventListener("click", () => {
        const resetScroll = dom.detailPanel.hidden || state.selectedIconName !== icon.name;
        if (state.selectedIconName !== icon.name || state.styleFilter !== "all") {
          state.selectedIconName = icon.name;
          state.selectedIconStyle = state.styleFilter === "all" ? null : icon.style;
        }
        dom.detailPanel.hidden = false;
        if (resetScroll) dom.detailContent.scrollTop = 0;
        syncGridSelection();
        updateDetailPanel();
        dom.detailCloseBtn.focus();
      });

      fragment.appendChild(btn);
    });

    dom.iconGrid.appendChild(fragment);
  }

  function syncGridSelection() {
    const filtered = getFilteredIcons();
    dom.iconGrid.querySelectorAll(".icon-card").forEach((card) => {
      const matched = card.dataset.iconName === state.selectedIconName;
      if (matched || card.classList.contains("is-selected")) {
        const icon = matched && state.styleFilter === "all"
          ? getSelectedIcon()
          : filtered.find((item) => item.name === card.dataset.iconName);
        const preview = buildPreviewElement(icon, false);
        card.querySelector(".icon-card-svg").replaceChildren(...(preview ? [preview] : []));
      }
      card.classList.toggle("is-selected", matched);
      card.setAttribute("aria-expanded", matched ? "true" : "false");
    });
  }

  function closeDetailPanel() {
    const selectedName = state.selectedIconName;
    const focusWasInside = dom.detailPanel.contains(document.activeElement);
    dom.detailPanel.hidden = true;
    state.selectedIconName = null;
    state.selectedIconStyle = null;
    syncGridSelection();
    if (focusWasInside) {
      const trigger = Array.from(dom.iconGrid.querySelectorAll(".icon-card"))
        .find((card) => card.dataset.iconName === selectedName);
      (trigger || dom.searchInput).focus({ preventScroll: true });
    }
  }

  function updateColorControlsUI(icon) {
    const recolorable = canRecolorIcon(icon);
    const mode = icon ? icon.mode : "original";

    if (!icon) {
      dom.colorModeNotice.textContent = "请选择一个图标以查看配色与代码。";
    } else if (mode === "original") {
      dom.colorModeNotice.textContent =
        "多色图标固定使用原厂配色，不支持改色。";
    } else if (mode === "background") {
      dom.colorModeNotice.textContent =
        "可更改圆形背景色，白色箭头保持不变。";
    } else {
      dom.colorModeNotice.textContent =
        "可自由选择预设或自定义颜色；点击「默认原图」恢复当前主题默认配色。";
    }

    dom.colorControls.classList.toggle("is-disabled", !recolorable);

    const isDefaultColor = !state.colorSelection || !recolorable;
    dom.resetColorBtn.classList.toggle("is-active", isDefaultColor);
    dom.resetColorBtn.setAttribute("aria-pressed", isDefaultColor ? "true" : "false");

    const allSwatches = dom.colorControls.querySelectorAll(".swatch-btn");
    allSwatches.forEach((btn) => {
      btn.disabled = !recolorable;
      const isSelected = Boolean(
        recolorable &&
        state.colorSelection &&
        state.colorSelection.type === "preset" &&
        state.colorSelection.palette === btn.dataset.palette &&
        state.colorSelection.key === btn.dataset.key
      );
      btn.classList.toggle("is-selected", isSelected);
      btn.setAttribute("aria-pressed", isSelected ? "true" : "false");
    });

    dom.customColorInput.disabled = !recolorable;
    const isCustomSelected = Boolean(
      recolorable && state.colorSelection && state.colorSelection.type === "custom"
    );
    dom.customColorWrap.classList.toggle("is-selected", isCustomSelected);
  }

  function updateDetailPanel() {
    const icon = getSelectedIcon();
    const isBitmap = icon?.style === "Bitmap";
    dom.colorSection.hidden = isBitmap;
    dom.codeSection.hidden = isBitmap;
    dom.detailSvgContainer.innerHTML = "";

    if (!icon) {
      dom.detailName.textContent = "未选择图标";
      dom.detailSubtitle.textContent = "COUI 图标";
      dom.variantSwitch.hidden = true;
      updateColorControlsUI(null);
      dom.copySvgBtn.disabled = true;
      dom.copyVectorBtn.disabled = true;
      return;
    }

    dom.copySvgBtn.disabled = isBitmap;
    dom.copyVectorBtn.disabled = isBitmap;
    const preview = buildPreviewElement(icon, true);
    if (preview) {
      dom.detailSvgContainer.appendChild(preview);
    }

    dom.detailName.textContent = formatWrappableName(icon.name);
    const modeLabelMap = {
      tint: "单色图标",
      background: "背景可调色图标",
      original: "多色原色图标"
    };
    const variants = variantsByName.get(icon.name) || [];
    const filled = variants.find((item) => item.style === "Filled");
    const outlined = variants.find((item) => item.style === "Outlined");
    const sharedGraphic = filled && outlined && filled.source && filled.source === outlined.source;
    const modeLabel = isBitmap ? "PNG 原图" : modeLabelMap[icon.mode] || "COUI 图标";
    dom.detailSubtitle.textContent = `${icon.style} · ${modeLabel}${sharedGraphic ? " · 共用图形" : ""}`;
    dom.variantSwitch.hidden = !(filled && outlined);
    [[dom.variantFilledBtn, "Filled"], [dom.variantOutlinedBtn, "Outlined"]].forEach(([button, style]) => {
      const active = icon.style === style;
      button.classList.toggle("is-active", active);
      button.setAttribute("aria-pressed", active ? "true" : "false");
    });

    updateColorControlsUI(icon);
  }

  function setTheme(nextTheme) {
    if (nextTheme !== "day" && nextTheme !== "night") {
      return;
    }
    state.theme = nextTheme;

    // 主题切换更新所有网格图标的默认原图以及右侧详情预览
    renderGrid();
    updateDetailPanel();
  }

  function showCopyFeedback(message, isSuccess) {
    if (copyFeedbackTimer) {
      clearTimeout(copyFeedbackTimer);
    }
    dom.copyFeedback.hidden = false;
    dom.copyFeedback.textContent = message;
    dom.copyFeedback.classList.toggle("is-success", Boolean(isSuccess));
    dom.copyFeedback.classList.toggle("is-error", !isSuccess);

    copyFeedbackTimer = setTimeout(() => {
      dom.copyFeedback.hidden = true;
    }, 3500);
  }

  function buildSvgCode(icon) {
    const svg = buildSvgElement(icon, true);
    if (!svg) throw new Error("无法生成 SVG");
    svg.removeAttribute("focusable");
    svg.removeAttribute("aria-hidden");
    svg.querySelectorAll("[data-coui-fill], [data-coui-stroke]").forEach((node) => {
      node.removeAttribute("data-coui-fill");
      node.removeAttribute("data-coui-stroke");
    });
    return new XMLSerializer().serializeToString(svg);
  }

  async function copyIcon(format) {
    try {
      const icon = getSelectedIcon();
      if (!icon) throw new Error("请先选择图标");
      const code = format === "SVG" ? buildSvgCode(icon) : buildImageVectorCode(icon);
      if (navigator.clipboard && typeof navigator.clipboard.writeText === "function" && window.isSecureContext) {
        try {
          await navigator.clipboard.writeText(code);
          showCopyFeedback(`已复制 ${format}。`, true);
          return;
        } catch (error) {
          // 剪贴板接口不可用时，尝试浏览器的复制命令。
        }
      }

      const textarea = document.createElement("textarea");
      textarea.value = code;
      textarea.setAttribute("readonly", "");
      textarea.style.position = "fixed";
      textarea.style.top = "-9999px";
      textarea.style.opacity = "0";
      document.body.appendChild(textarea);
      let succeeded;
      try {
        textarea.select();
        succeeded = document.execCommand("copy");
      } finally {
        textarea.remove();
      }

      if (succeeded) {
        showCopyFeedback(`已复制 ${format}。`, true);
      } else {
        showCopyFeedback("复制失败：浏览器拒绝访问剪贴板，请检查浏览器权限。", false);
      }
    } catch (error) {
      const reason = error && error.message ? `（${error.message}）` : "";
      showCopyFeedback(`复制失败${reason}。`, false);
    }
  }

  function bindEvents() {
    const topNav = document.querySelector(".top-nav");
    let previousScrollY = Math.max(0, window.scrollY);
    window.addEventListener("scroll", () => {
      const currentScrollY = Math.max(0, window.scrollY);
      // 累积小幅滚动，避免触控板微小反向输入让顶栏反复进出。
      if (currentScrollY <= topNav.offsetHeight || Math.abs(currentScrollY - previousScrollY) >= 8) {
        document.body.classList.toggle("is-nav-hidden",
          currentScrollY > topNav.offsetHeight && currentScrollY > previousScrollY);
        previousScrollY = currentScrollY;
      }
    }, { passive: true });

    document.addEventListener("coui-theme-change", (event) => {
      if (state.theme !== event.detail) setTheme(event.detail);
    });
    dom.styleFilterButtons.forEach((button) => {
      button.addEventListener("click", () => {
        state.styleFilter = button.dataset.styleFilter;
        dom.styleFilterButtons.forEach((item) => {
          const active = item === button;
          item.classList.toggle("is-active", active);
          item.setAttribute("aria-pressed", String(active));
        });
        if (state.selectedIconName) {
          if (!getFilteredIcons().some((icon) => icon.name === state.selectedIconName)) {
            closeDetailPanel();
          } else if (state.styleFilter !== "all") {
            state.selectedIconStyle = state.styleFilter;
          }
        }
        renderGrid();
        updateDetailPanel();
      });
    });
    dom.detailCloseBtn.addEventListener("click", closeDetailPanel);
    dom.detailPanel.addEventListener("keydown", (event) => {
      if (event.key === "Escape") closeDetailPanel();
    });
    [[dom.variantFilledBtn, "Filled"], [dom.variantOutlinedBtn, "Outlined"]].forEach(([button, style]) => {
      button.addEventListener("click", () => {
        state.selectedIconStyle = style;
        syncGridSelection();
        updateDetailPanel();
      });
    });

    dom.searchInput.addEventListener("input", (event) => {
      state.query = event.target.value || "";
      dom.searchClearBtn.hidden = state.query.length === 0;
      renderGrid();
      updateDetailPanel();
    });

    dom.searchClearBtn.addEventListener("click", () => {
      state.query = "";
      dom.searchInput.value = "";
      dom.searchClearBtn.hidden = true;
      dom.searchInput.focus();
      renderGrid();
      updateDetailPanel();
    });

    dom.resetColorBtn.addEventListener("click", () => {
      state.colorSelection = null;
      updateDetailPanel();
    });

    dom.customColorInput.addEventListener("input", (event) => {
      const icon = getSelectedIcon();
      const hex = String(event.target.value || "#007aff").toUpperCase();
      dom.customColorHex.textContent = hex;
      if (!canRecolorIcon(icon)) {
        return;
      }
      state.colorSelection = {
        type: "custom",
        hex: hex
      };
      updateDetailPanel();
    });

    dom.copySvgBtn.addEventListener("click", () => copyIcon("SVG"));
    dom.copyVectorBtn.addEventListener("click", () => copyIcon("ImageVector"));
  }

  function init() {
    renderPalettes();
    bindEvents();
    setTheme(state.theme);
  }

  init();
})();
