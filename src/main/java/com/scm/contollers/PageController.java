package com.scm.contollers;

import com.scm.SmartContactManagerApplication;
import com.scm.entities.User;
import com.scm.forms.UserForm;
import com.scm.helpers.Message;
import com.scm.helpers.MessageType;
import com.scm.services.UserServices;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PageController {

    @Autowired
    private UserServices userServices;

    private final SmartContactManagerApplication smartContactManagerApplication;

    PageController(SmartContactManagerApplication smartContactManagerApplication) {
        this.smartContactManagerApplication = smartContactManagerApplication;
    }

    @GetMapping("/home")
    public String home(Model model) {
        System.out.println("========================");
        System.out.println("Home Page handler");
        System.out.println("========================");

        // sending data to view
        model.addAttribute("name", "Substring Technologies");
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
    public String register(Model model) {
        UserForm userForm = new UserForm();
        // default value bhi daal sakte ha
        // userForm.setName("Abhay");
        // userForm.setAbout("This is about : write something about yourself");
        model.addAttribute("userForm", userForm);

        return "register";
    }

    // processing register
    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String processRegister(@Valid @ModelAttribute UserForm userForm, BindingResult rBindingResult,
            HttpSession session) {
        System.out.println("Processing registration");
        // fetch form data
        // UserForm
        System.out.println(userForm);

        // validate form data
        if (rBindingResult.hasErrors()) {
            return "register";
        }

        // TODO::Validate userForm[Next Video]

        // save to database

        // userservice

        // UserForm--> User
        // User user = User.builder()
        // .name(userForm.getName())
        // .email(userForm.getEmail())
        // .password(userForm.getPassword())
        // .about(userForm.getAbout())
        // .phoneNumber(userForm.getPhoneNumber())
        // .profilePic(
        // "https://www.learncodewithdurgesh.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fdurgesh_sir.35c6cb78.webp&w=1920&q=75")
        // .build();

        User user = new User();
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setAbout(userForm.getAbout());
        user.setPhoneNumber(userForm.getPhoneNumber());
        user.setEnabled(false);
        user.setProfilePic(
                "https://www.learncodewithdurgesh.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Fdurgesh_sir.35c6cb78.webp&w=1920&q=75");

        User savedUser = userServices.saveUser(user);

        System.out.println("user saved :");

        // message = "Registration Successful"

        // add the message:

        Message message = Message.builder().content("Registration Successful").type(MessageType.green).build();

        session.setAttribute("message", message);

        // redirectto login page
        return "redirect:/register";
    }

}
