package ru.netology.WebCloud.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.netology.WebCloud.domain.User;
import ru.netology.WebCloud.dto.LoginRequest;
import ru.netology.WebCloud.dto.LoginResponse;
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

        User user = authService.authenticate(request.getLogin(),request.getPassword());

        return new LoginResponse(user.getToken());
    }


    @PostMapping("/logout")
    public void logout(@RequestHeader ("auth-token") String token){
        authService.logout(token);
    }


}
