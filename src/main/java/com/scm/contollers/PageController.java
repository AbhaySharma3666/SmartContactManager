package com.scm.contollers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PageController {

    @RequestMapping("/home")
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

    @RequestMapping("/about")
    public String aboutPage() {
        System.out.println("About page loading");
        return "about";
    }
    
    // services route

    @RequestMapping("/services")
    public String servicesPage() {
        System.out.println("Services page loading");
        return "services";
    }
}
