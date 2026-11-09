(function () {
  "use strict";

  const root = document.documentElement;
  const storageKey = "coui-pages-theme";

  function readTheme() {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved === "day" || saved === "night") return saved;
    } catch (error) {
      console.warn("无法读取 COUI 主题偏好。", error);
    }
    return root.dataset.theme;
  }

  root.dataset.theme = readTheme();

  document.addEventListener("DOMContentLoaded", () => {
    const button = document.getElementById("theme-toggle-btn");
    // 使用库中 DarkLight 的路径，首页无需加载整份图标数据。
    button.innerHTML = '<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" focusable="false"><path d="M12,22C17.52,22 22,17.52 22,12 22,6.48 17.52,2 12,2 6.48,2 2,6.48 2,12 2,17.52 6.48,22 12,22ZM12,3.915c3.889,0 8,4.005 8,8.085 0,4.08 -3.927,7.992 -7.928,7.992z"/></svg>';
    button.hidden = false;

    function applyTheme(theme) {
      root.dataset.theme = theme;
      const label = theme === "day" ? "切换到夜间模式" : "切换到日间模式";
      button.setAttribute("aria-label", label);
      button.setAttribute("title", label);
      button.setAttribute("aria-pressed", String(theme === "night"));
      document.dispatchEvent(new CustomEvent("coui-theme-change", { detail: theme }));
    }

    button.addEventListener("click", () => {
      const theme = root.dataset.theme === "day" ? "night" : "day";
      applyTheme(theme);
      try {
        localStorage.setItem(storageKey, theme);
      } catch (error) {
        console.warn("无法保存 COUI 主题偏好，当前页面仍可切换主题。", error);
      }
    });

    applyTheme(root.dataset.theme);
    // 浏览器后退可恢复旧页面，重新读取另一个页面保存的主题。
    window.addEventListener("pageshow", () => applyTheme(readTheme()));
  });
})();
