package com.userBase.services;

import com.userBase.entities.Customer;
import com.userBase.entities.Photo;
import com.userBase.enums.Role;
import com.userBase.mistakes.ErrorService;
import com.userBase.repositories.CustomerRepository;
import com.userBase.repositories.PhotoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerService implements UserDetailsService {

    private PhotoService photoService;
    public final CustomerRepository customerRepository;

    public final PhotoRepository photoRepository;

    @Autowired
    public CustomerService(PhotoService photoService, CustomerRepository customerRepository, PhotoRepository photoRepository) {
        this.photoService = photoService;
        this.customerRepository = customerRepository;
        this.photoRepository = photoRepository;
    }

    ////////////////////////////////////////////////////////////////////////////
    //Guardar 
    ////////////////////////////////////////////////////////////////////////////
    @Transactional(rollbackOn = Exception.class)
    public void save(Customer customer, Optional<MultipartFile> file) throws Exception {
        validar(customer);
        customer.setClave(new BCryptPasswordEncoder().encode(customer.getClave()));
        //activateIfNew activa y da rol de user
        activateIfNew(customer);
        if (file.isPresent() && !file.get().isEmpty()) {
            Photo photo = photoService.guardarFoto(file.get());
            customer.setPhoto(photo);
        }
        customerRepository.save(customer);  
    }
    
    public void savefotocustomer(Customer customer){
        
        customerRepository.save(customer); 
        
    }
    ////////////////////////////////////////////////////////////////////////////
    //Modificar
    ////////////////////////////////////////////////////////////////////////////
    @Transactional(rollbackOn = Exception.class)
    public void modificar(String nombre, String apellido, String dni, Optional<MultipartFile> file, Customer customer) throws Exception {   
        customer.setNombre(nombre);
        customer.setDni(dni);
        customer.setApellido(apellido);
        customer.setActive(true);
        activateIfNew(customer);
        String idPhoto = null;

        if (file.isPresent() && !file.get().isEmpty()) {
            Photo photo = photoService.guardarFoto(file.get());
            customer.setPhoto(photo);
        }

        if (!file.isPresent() && file.get().isEmpty()) {
            idPhoto = customer.getPhoto().getId();
            Photo photo = photoService.buscar(idPhoto);
            customer.setPhoto(photo);
        }
        customerRepository.save(customer);
    }

    @Transactional(rollbackOn = Exception.class)
    public void modificarprofileadmin(String nombre, String apellido, String dni, String role, Optional<MultipartFile> file, Customer customer) throws Exception {

        //Ingresar validaciones en un futuro
        if (role.equalsIgnoreCase("ADMIN")) {
            customer.setRole(Role.ADMIN);
        } else if (role.equalsIgnoreCase("USER")) {
            customer.setRole(Role.USER);
        }

        customer.setNombre(nombre);
        customer.setDni(dni);
        customer.setApellido(apellido);
        customer.setActive(Boolean.TRUE);

//        validaNombre(customer);
//        validaApellido(customer);
//        validaDni(customer);
        activateIfNew(customer);
        String idPhoto = null;

        if (file.isPresent() && !file.get().isEmpty()) {
            Photo photo = photoService.guardarFoto(file.get());
            customer.setPhoto(photo);
        }

        if (!file.isPresent() && file.get().isEmpty()) {
            idPhoto = customer.getPhoto().getId();
            Photo photo = photoService.buscar(idPhoto);
            customer.setPhoto(photo);
        }
        customerRepository.save(customer);
    }

    ////////////////////////////////////////////////////////////////////////////
    //Verifica los permisos del logeado
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<GrantedAuthority> permisos = new ArrayList<>();
        GrantedAuthority rolPermiso = new SimpleGrantedAuthority("ROLE_" + customer.getRole().toString());
        permisos.add(rolPermiso);

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        HttpSession session = attr.getRequest().getSession(true);
        session.setAttribute("customersession", customer);

        return new User(customer.getEmail(), customer.getClave(), permisos);
    }

    ////////////////////////////////////////////////////////////////////////////
    @Transactional
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer findById(String id) throws Exception {
        Optional<Customer> option = customerRepository.findById(id);
        if (option.isPresent()) {
            Customer customer = option.get();
            return customer;
        } else {
            throw new Exception("usuario no encontrado");
        }
    }

   
    private void activateIfNew(Customer customer) {
        if (customer.getActive() == null) {
            customer.setActive(true);
            customer.setRole(Role.USER);
        }
    }
    
    public void alta(Customer customer){
        
      customer.setActive(Boolean.TRUE);
      customer.setRole(Role.USER);
      customerRepository.save(customer);
    }

    @Transactional
    public void activate(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(rollbackOn = {Exception.class})
    public void desactivate(String id) throws Exception {
        Customer customer = findById(id);
        customer.setActive(false);
        customer.setRole(null);
        customerRepository.save(customer);
    }

    ////////////////////////////////////////////////////////////////////////////
    //Validacion general
    ////////////////////////////////////////////////////////////////////////////
    public void validar(Customer customer) throws ErrorService {
//        validaSiExiste(customer);
        validaNombre(customer);
        validaApellido(customer);
        validaDni(customer);
        validaClave(customer);
        validaEmail(customer);
    }

    ////////////////////////////////////////////////////////////////////////////
    //Valida si ya existe un customer con ese mail o dni
    ////////////////////////////////////////////////////////////////////////////
    private void validaSiExiste(Customer customer) throws ErrorService {

        Optional<Customer> optionalCustomer = null;
        optionalCustomer = customerRepository.findByEmail(customer.getEmail());

        if (optionalCustomer.isPresent()) {
            throw new ErrorService("Ya existe un usuario con ese email");
        }

        optionalCustomer = customerRepository.findByDni(customer.getDni());

        if (optionalCustomer.isPresent()) {
            throw new ErrorService("Ya existe un usuario con ese DNI");
        }

    }

    ////////////////////////////////////////////////////////////////////////
    //Nombre
    ////////////////////////////////////////////////////////////////////////
    public void validaNombre(Customer customer) throws ErrorService {

        if (customer.getNombre().isEmpty()) {
            throw new ErrorService("Tiene que ingresar un nombre");
        }

        Pattern pattern = Pattern
                .compile("^[a-zA-Z\\s]+{3,25}");
        Matcher mather = pattern.matcher(customer.getNombre());
        if (mather.find() != true) {
            throw new ErrorService("caracteres invalido nombre.");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //Apellido
    ////////////////////////////////////////////////////////////////////////
    public void validaApellido(Customer customer) throws ErrorService {

        if (customer.getApellido().isEmpty()) {
            throw new ErrorService("Tiene que ingresar un apellido");
        }

//        Pattern pattern = Pattern
//                .compile("^[a-zA-Z\\s]+{3,25}");
//        Matcher mather = pattern.matcher(customer.getApellido());
//        if (mather.find() == true) {
//            throw new ErrorService("caracteres invalido apellido.");
//        }
    }

    ////////////////////////////////////////////////////////////////////////
    //DNI
    ////////////////////////////////////////////////////////////////////////
    public void validaDni(Customer customer) throws ErrorService {
        Pattern pattern = Pattern
                .compile("^\\d{8}(?:[-\\s]\\d{4})?$");
        Matcher mather = pattern.matcher(customer.getDni());
        if (mather.find() != true) {
            throw new ErrorService("El DNI debe contener de 8 digitos");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //Clave
    ////////////////////////////////////////////////////////////////////////
    public void validaClave(Customer customer) throws ErrorService {
        //Debe contener entre 8 y 16 caracteres, 
        //minimo 1 minuscula, 1 mayuscula y 1 numero.
        Pattern pattern = Pattern
                .compile("^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{8,16}$");
        Matcher mather = pattern.matcher(customer.getClave());
        if (mather.find() != true) {
            throw new ErrorService("clave 8 a 16 digitos 1 May 1 Min minimo");
        }

        if (customer.getClave().isEmpty()) {
            throw new ErrorService("Tiene que ingresar una clave");
        }
    }

    public void validarClaveX2(String clave1, String clave2) throws ErrorService {
        if (!clave1.equals(clave2)) {
            throw new ErrorService("Las claves no coinciden");

        }

    }

    ////////////////////////////////////////////////////////////////////////
    //Valida Mail
    ////////////////////////////////////////////////////////////////////////
    public void validaEmail(Customer customer) throws ErrorService {

        Pattern pattern = Pattern
                .compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
        Matcher mather = pattern.matcher(customer.getEmail());
        if (mather.find() != true) {
            throw new ErrorService("El email ingresado no es válido.");
        }

        if (customer.getEmail().isEmpty()) {
            throw new ErrorService("Tiene que ingresar un mail");
        }
        if (customer.getEmail().length() < 10) {
            throw new ErrorService("Largo de mail erroneo");

        }
    }

}
