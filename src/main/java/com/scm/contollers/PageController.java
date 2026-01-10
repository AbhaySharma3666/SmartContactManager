package com.scm.contollers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class PageController {

    @GetMapping("/home")
    public String home(Model model) {
        System.out.println("========================");
        System.out.println("Home Page handler");
        System.out.println("========================");

        // sending data to view 
        model.addAttribute("name","Substring Technologies");
        model.addAttribute("githubRepo", "https://github.com/AbhaySharma3666");
        return "home";
    }

    // about route 
    @GetMapping("/about")
    public String aboutPage() {
        System.out.println("About page loading");
        return "about";
    }
    
    // services route
    @GetMapping("/services")
    public String servicesPage() {
        System.out.println("Services page loading");
        return "services";
    }

    // contact route
    @GetMapping("/contact")
    public String contact() {
        System.out.println("contact page loading");
        return new String("contact");
    }

    // login route
    @GetMapping("/login")
    public String login() {
        System.out.println("login page loading");
        return new String("login");
    }

    // register route
    @GetMapping("/register")
    public String register() {
        System.out.println("register page loading");
        return new String("register");
    }
    


}
