package my_sawit.authentication_manajemen_akun.controller;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.RegisterRequest;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody RegisterRequest request){
        User savedUser = authService.register(request);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> fetchAllUsers(){
        List<User> users = authService.fetchAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/ping")
    public String ping(){
        return "Auth is starting";
    }
}
