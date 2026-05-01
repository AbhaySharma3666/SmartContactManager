package com.scm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scm.entities.User;
import com.scm.forms.UserForm;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class PageController {

    private static final Logger logger = LoggerFactory.getLogger(PageController.class);

    private final UserService userService;

    public PageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String home(Model model) {
        logger.info("Home page handler");
        model.addAttribute("name", "Substring Technologies");
        model.addAttribute("youtubeChannel", "Learn Code With Abhay");
        model.addAttribute("githubRepo", "https://github.com/abhaysharma3666/");
        return "home";
    }

    @RequestMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("isLogin", true);
        logger.info("About page loading");
        return "about";
    }

    @RequestMapping("/services")
    public String servicesPage() {
        logger.info("Services page loading");
        return "services";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        UserForm userForm = new UserForm();
        model.addAttribute("userForm", userForm);
        return "register";
    }

    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String processRegister(@Valid @ModelAttribute UserForm userForm, BindingResult rBindingResult,
            HttpSession session) {
        logger.info("Processing registration");
        logger.info("UserForm: {}", userForm);

        if (rBindingResult.hasErrors()) {
            return "register";
        }

        // check if user already exists
        if (userService.isUserExistByEmail(userForm.getEmail())) {
            Message message = Message.builder().content("Email already registered").type(MessageType.red).build();
            session.setAttribute("message", message);
            return "redirect:/register";
        }

        User user = new User();
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setAbout(userForm.getAbout());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setEnabled(false);
        user.setProfilePic("https://cdn-icons-png.flaticon.com/512/3135/3135715.png");

        userService.saveUser(user);
        logger.info("User saved successfully");

        Message message = Message.builder()
                .content("Registration Successful! Verification email sent to " + userForm.getEmail())
                .type(MessageType.green).build();
        session.setAttribute("message", message);

        return "redirect:/login";
    }
}
