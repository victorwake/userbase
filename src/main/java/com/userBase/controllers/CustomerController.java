package com.userBase.controllers;

import com.userBase.services.CustomerService;
import com.userBase.services.PhotoService;
import com.userBase.entities.Customer;
import com.userBase.entities.Photo;
import com.userBase.repositories.PhotoRepository;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    private PhotoService photoService;

    private PhotoRepository photoRepository;

    @Autowired
    public CustomerController(CustomerService customerService, PhotoService photoService, PhotoRepository photoRepository) {
        this.customerService = customerService;
        this.photoService = photoService;
        this.photoRepository = photoRepository;
    }

    @PostMapping("/update/{id}")
    public String saveupdate(@PathVariable String id, @RequestParam String nombre, @RequestParam String apellido, @RequestParam String dni, ModelMap model,
            Optional<MultipartFile> file, HttpSession session) {
        try {
            Customer customer = (Customer) session.getAttribute("customersession");
            customerService.modificar(nombre, apellido, dni, file, customer);
            model.put("descripcion", "Usuario registrado con exito.");

        } catch (Exception e) {
            model.put("error", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            System.err.println(e);
            return "customer/customeredit";
        }
        return "customer/profile";
    }
//    ////////////////////////////////////////////////////////////////////////////

    @GetMapping("/alta/{id}")
    public String activate(@PathVariable String id, ModelMap model,HttpSession session) {
     
        try {
            Customer customer = customerService.findById(id);
            customerService.alta(customer);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
       
        return "redirect:/customer/list";
        
    }
    
 
    @GetMapping("/profile")
    public String profile(ModelMap model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customersession");

        return "customer/profile";
    }

    @GetMapping("/deletephotouser/")
    public String deletephotouser(ModelMap model, HttpSession session) {

        try {
            Customer customer = (Customer) session.getAttribute("customersession");

            customer.setPhoto(null);

            customerService.savefotocustomer(customer);
        } catch (Exception ex) {
            model.put("error", ex.getMessage());
        }
        return "customer/profile";
    }
    @GetMapping("/deletephotoadmin/{id}")
    public String deletephotoadmin(@PathVariable String id ,ModelMap model) {

        try {
            Customer customer = customerService.findById(id);

            customer.setPhoto(null);

            customerService.savefotocustomer(customer);
        } catch (Exception ex) {
            model.put("error", ex.getMessage());
        }
        return "redirect:/customer/list";
    }

    @GetMapping("/password")
    public String password(ModelMap model) {

        return "customer/password";
    }

    @PostMapping("/password")
    public String profile2(ModelMap model, @ModelAttribute Customer customer, @RequestParam String clave1, @RequestParam String clave2) {
        try {
            //valida que las 2 nuevas claves sean iguales
            customerService.validarClaveX2(clave1, clave2);
            customer.setClave(clave2);

            return "redirect:/customer/profile";
        } catch (Exception e) {
            model.put("error", e.getMessage());

            return "/customer/profile";
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @GetMapping("/list")
    public String listCustomer(ModelMap model, HttpSession session) {

        Customer customer = (Customer) session.getAttribute("customersession");

        List<Customer> customers = customerService.listCustomers();//Traigo la lista de editoriales
        model.addAttribute("customers", customers);//Agrego la lista a ModelMap(model)
        return "customer/list-customer"; //ruta del archivo donde busca para mostrar
    }

    @GetMapping("/load/{id}")
    public ResponseEntity<byte[]> photo(@PathVariable String id) {
        Photo photo = photoRepository.getOne(id);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(photo.getContenido(), headers, HttpStatus.OK);
    }

}
