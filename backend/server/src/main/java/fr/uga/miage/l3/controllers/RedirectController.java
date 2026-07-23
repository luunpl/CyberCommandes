package fr.uga.miage.l3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {

    // The UI is served by the frontend container (nginx); the backend root
    // points to the API documentation instead.
    @RequestMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui.html";
    }
}
