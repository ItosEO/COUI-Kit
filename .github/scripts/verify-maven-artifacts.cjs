const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { execFileSync } = require('node:child_process');

const properties = Object.fromEntries(fs.readFileSync('gradle.properties', 'utf8').split('\n')
  .filter(line => /^[A-Z_]+=/.test(line)).map(line => [line.slice(0, line.indexOf('=')), line.slice(line.indexOf('=') + 1).trim()]));
const group = process.env.PACKAGE_GROUP || properties.GROUP;
const version = process.env.PACKAGE_VERSION || properties.VERSION_NAME;

for (const artifact of ['coui', 'coui-material3', 'coui-icons']) {
  const dir = path.join(artifact, 'build/repository', group.replaceAll('.', '/'), artifact, version);
  const modules = fs.readdirSync(dir).filter(name => name.endsWith('.module'));
  assert.equal(modules.length, 1, `${artifact} 应只生成一个库 publication`);
  const stem = modules[0].slice(0, -'.module'.length);
  const file = suffix => path.join(dir, stem + suffix);
  for (const suffix of ['.aar', '.pom', '.module', '-sources.jar', '-javadoc.jar']) {
    assert(fs.statSync(file(suffix)).size > 0, `${artifact} 缺少有效发行文件: ${suffix}`);
  }
  const metadata = JSON.parse(fs.readFileSync(file('.module'), 'utf8'));
  assert.equal(metadata.component.group, group);
  assert.equal(metadata.component.module, artifact);
  assert.equal(metadata.component.version, version);
  const dependencies = metadata.variants.flatMap(variant => variant.dependencies ?? []);
  const composeBom = dependencies.find(dep => dep.group === 'androidx.compose' && dep.module === 'compose-bom-alpha');
  assert(composeBom?.version?.requires, `${artifact} 缺少带版本的 Compose BOM`);
  assert(!dependencies.some(dep => ['demo', 'capsule'].includes(dep.module)), `${artifact} 发行依赖包含 Demo 装配`);
  assert(!dependencies.some(dep => dep.group === 'androidx.compose.material'), `${artifact} 不应引入 Material Icons`);

  if (artifact === 'coui') {
    const isDynamicAnimation = dep => dep.group === 'androidx.dynamicanimation' && dep.module === 'dynamicanimation';
    const animationDependencies = dependencies.filter(isDynamicAnimation);
    const animationConstraints = metadata.variants
      .flatMap(variant => variant.dependencyConstraints ?? []).filter(isDynamicAnimation);
    assert(animationDependencies.some(dep => dep.version?.requires === '1.1.0'), '核心动画依赖应请求 1.1.0');
    assert([...animationDependencies, ...animationConstraints].every(dep => dep.version?.strictly === undefined),
      '核心动画依赖不应使用严格版本约束');
    assert(!dependencies.some(dep => dep.group === 'androidx.compose.material3'), '核心不应依赖 Material 3');
    assert(!dependencies.some(dep => dep.module === 'coui-icons'), '核心模块元数据不应引入 coui-icons');
  } else if (artifact === 'coui-material3') {
    assert(dependencies.some(dep => dep.group === group && dep.module === 'coui' && dep.version?.requires === version), '适配模块必须依赖同版本核心');
    assert(dependencies.some(dep => dep.group === 'androidx.compose.material3' && dep.module === 'material3'), '适配模块缺少 Material 3');
    assert(!dependencies.some(dep => dep.module === 'coui-icons'), '适配模块元数据不应引入 coui-icons');
  } else if (artifact === 'coui-icons') {
    assert(dependencies.some(dep => dep.group === group && dep.module === 'coui' && dep.version?.requires === version), '图标模块必须依赖同版本核心');
    assert(!dependencies.some(dep => dep.group === 'androidx.compose.material3'), '图标模块不应依赖 Material 3');
  } else {
    assert.fail(`未知的 artifact: ${artifact}`);
  }

  const pom = fs.readFileSync(file('.pom'), 'utf8');
  assert(pom.includes(`<artifactId>${artifact}</artifactId>`), `${artifact} POM 坐标不正确`);
  assert(pom.includes(`<version>${version}</version>`), `${artifact} POM 版本不正确`);

  const pomDependencies = [...pom.matchAll(/<dependency>([\s\S]*?)<\/dependency>/g)].map(([, dep]) => ({
    groupId: dep.match(/<groupId>([\s\S]*?)<\/groupId>/)?.[1]?.trim(),
    artifactId: dep.match(/<artifactId>([\s\S]*?)<\/artifactId>/)?.[1]?.trim(),
    version: dep.match(/<version>([\s\S]*?)<\/version>/)?.[1]?.trim(),
  }));

  if (artifact === 'coui' || artifact === 'coui-material3') {
    assert(!pomDependencies.some(dep => dep.groupId === group && dep.artifactId === 'coui-icons'), `${artifact} POM 依赖不应引入 coui-icons`);
  } else if (artifact === 'coui-icons') {
    assert(pomDependencies.some(dep => dep.groupId === group && dep.artifactId === 'coui' && dep.version === version), '图标模块 POM 必须依赖同 group 同版本核心');
  }
  const managedDependencies = pom.match(/<dependencyManagement>([\s\S]*?)<\/dependencyManagement>/)?.[1] ?? '';
  assert([...managedDependencies.matchAll(/<dependency>([\s\S]*?)<\/dependency>/g)].some(([, dependency]) => [
    '<groupId>androidx.compose</groupId>', '<artifactId>compose-bom-alpha</artifactId>',
    `<version>${composeBom.version.requires}</version>`, '<type>pom</type>', '<scope>import</scope>',
  ].every(field => dependency.includes(field))), `${artifact} POM 缺少 Compose BOM 版本导入`);

  const entries = suffix => execFileSync('unzip', ['-Z1', file(suffix)], { encoding: 'utf8' }).split('\n');
  assert(entries('.aar').includes('classes.jar'), `${artifact} AAR 缺少 classes.jar`);

  let expectedSource;
  if (artifact === 'coui') {
    expectedSource = '/Slider.kt';
  } else if (artifact === 'coui-material3') {
    expectedSource = '/CouiMaterial3Style.kt';
  } else if (artifact === 'coui-icons') {
    expectedSource = '/CouiIcons.kt';
  } else {
    assert.fail(`未知的 artifact: ${artifact}`);
  }
  assert(entries('-sources.jar').some(name => name.endsWith(expectedSource)), `${artifact} 源码包缺少库源码`);
  assert(entries('-javadoc.jar').includes('index.html'), `${artifact} API 文档包没有生成 HTML`);

  const notices = ['LICENSE', 'NOTICE', 'TRADEMARKS.md'];
  if (artifact === 'coui') {
    notices.push('ICON_SOURCES.md', 'coui-skia-LICENSE', 'coui-androidx-LICENSE');
  } else if (artifact === 'coui-icons') {
    notices.push('ICON_SOURCES.md');
  }

  for (const suffix of ['-sources.jar', '-javadoc.jar']) {
    for (const notice of notices) {
      assert(entries(suffix).includes(`META-INF/coui/${notice}`), `${artifact} ${suffix} 缺少声明: ${notice}`);
    }
  }
  console.log(`发行产物检查通过: ${group}:${artifact}:${version}`);
}
