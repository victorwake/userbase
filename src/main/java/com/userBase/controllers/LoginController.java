package com.userBase.controllers;

import com.userBase.entities.Customer;
import com.userBase.repositories.PhotoRepository;
import com.userBase.services.CustomerService;
import com.userBase.services.PhotoService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class LoginController {

    private final CustomerService customerService;

    private PhotoService photoService;

    private PhotoRepository photoRepository;

    @Autowired
    public LoginController(CustomerService customerService, PhotoService photoService, PhotoRepository photoRepository) {
        this.customerService = customerService;
        this.photoService = photoService;
        this.photoRepository = photoRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, ModelMap model) {
        if (error != null) {
            model.put("error", "Email o password incorrectos");
        }
        return "/login_and_register/login";
    }

    ////////////////////////////////////////////////////////////////////////////
    //Muestra la plantilla de registro y registra el usuario el PostMapping
    ////////////////////////////////////////////////////////////////////////////
    @GetMapping("/form")
    public String createUser(ModelMap model) {
        model.addAttribute("customer", new Customer());
        return "login_and_register/register";
    }

    @PostMapping("/register")
    public String saveCustomer(@ModelAttribute Customer customer, ModelMap model,
            @RequestParam String clave2, @RequestParam MultipartFile file) {
        try {
            //validar
            customerService.validarClaveX2(customer.getClave(), clave2);
            customerService.save(customer, Optional.ofNullable(file));
//            model.put("titulo", "Bienvenido.");
//            model.put("descripcion", "Usuario registrado con exito.");
            return "redirect:/form";
        } catch (Exception e) {
            model.put("error", e.getMessage());
            return "/login_and_register/register";
        }
    }
    
}
