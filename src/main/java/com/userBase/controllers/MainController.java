package com.userBase.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {
    
 @GetMapping("/main")
    public String home(ModelMap model) {
        
        return "/main/main";
    }

    

}