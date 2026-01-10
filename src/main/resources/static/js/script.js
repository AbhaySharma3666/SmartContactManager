console.log("Script loaded");

// change theme work
let currentTheme = getTheme();
//initial -->

document.addEventListener("DOMContentLoaded", () => {
  console.log("DOM loaded, current theme:", currentTheme);
  changeTheme();
});

//TODO:
function changeTheme() {
  //set to web page

  changePageTheme(currentTheme, "");
  //set the listener to change theme button
  const changeThemeButton = document.querySelector("#theme_change_button");

  if (!changeThemeButton) {
    console.error("Theme button not found");
    return;
  }

  changeThemeButton.addEventListener("click", (event) => {
    let oldTheme = currentTheme;
    console.log("change theme button clicked");
    if (currentTheme === "dark") {
      //theme ko light
      currentTheme = "light";
    } else {
      //theme ko dark
      currentTheme = "dark";
    }
    console.log(currentTheme);
    changePageTheme(currentTheme, oldTheme);
  });
}

//set theme to localstorage
function setTheme(theme) {
  localStorage.setItem("theme", theme);
}

//get theme from localstorage
function getTheme() {
  let theme = localStorage.getItem("theme");
  return theme ? theme : "light";
}

//change current page theme
function changePageTheme(theme, oldTheme) {
  console.log("Changing theme from", oldTheme, "to", theme);
  setTheme(theme);
  
  if (oldTheme) {
    document.querySelector("html").classList.remove(oldTheme);
  }
  
  document.querySelector("html").classList.add(theme);
  console.log("HTML classes:", document.querySelector("html").className);
  
  const button = document.querySelector("#theme_change_button");
  const icon = document.querySelector("#theme_icon");
  
  if (theme === "light") {
    button.querySelector("span").textContent = "Dark";
    icon.className = "fa-solid fa-moon";
  } else {
    button.querySelector("span").textContent = "Light";
    icon.className = "fa-regular fa-sun";
  }
}

//change page change theme 