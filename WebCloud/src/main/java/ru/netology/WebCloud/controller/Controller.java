package ru.netology.WebCloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.netology.WebCloud.data.LoginRequest;
import ru.netology.WebCloud.data.LoginResponse;
import ru.netology.WebCloud.service.AuthService;

@RestController
//@RequestMapping("/cloud")
public class Controller {

    @Autowired
    private AuthService authService;

    @GetMapping("/test")
    public String hello(){
        return "Hello from App";
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody(required = false) LoginRequest request){
        System.out.println("=== DEBUG: WHAT FRONTEND SENDS ===");
        System.out.println("Request object: " + request);
        System.out.println("Is request null: " + (request == null));

        // Если request null - значит фронт отправил пустой JSON {}
        if (request == null) {
            System.out.println("ERROR: Frontend sent empty JSON object!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Отправьте email и password в формате JSON");
        }

        // Если поля null - значит фронт отправил {"email": null, "password": null}
        if (request.getLogin() == null) {
            System.out.println("ERROR: Email field is null in request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Поле 'email' обязательно");
        }

        if (request.getPassword() == null) {
            System.out.println("ERROR: Password field is null in request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Поле 'password' обязательно");
        }
        System.out.println("Email from request: '" + request.getLogin() + "'");
        System.out.println("Password from request: '" + request.getPassword() + "'");

        return authService.authenticate(request);
    }


    @PostMapping("/logout")
    public void logout(@RequestHeader ("auth-token") String token){
        authService.logout(token);
    }


}
