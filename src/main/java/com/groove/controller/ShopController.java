package com.groove.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// @Controller
// @RequestMapping("/shop")
public class ShopController {
    // @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        // String email = principal.getName();
        // ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        // model.addAttribute("title", "dashboard");
        // model.addAttribute("user", user);
        return "dashboard";
    }
}
