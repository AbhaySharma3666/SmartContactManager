console.log("Dashboard loaded");

// Reinitialize theme button for dashboard page
const themeButton = document.querySelector("#theme_change_button");
if (themeButton && !themeButton.hasAttribute('data-initialized')) {
  themeButton.setAttribute('data-initialized', 'true');
  themeButton.addEventListener("click", (event) => {
    let currentTheme = localStorage.getItem("theme") || "light";
    let oldTheme = currentTheme;
    currentTheme = currentTheme === "dark" ? "light" : "dark";
    
    localStorage.setItem("theme", currentTheme);
    document.querySelector("html").classList.remove(oldTheme);
    document.querySelector("html").classList.add(currentTheme);
    themeButton.querySelector("span").textContent = currentTheme === "light" ? "Dark" : "Light";
  });
}
