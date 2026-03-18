package my_sawit.authentication_manajemen_akun.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.service.AuthStrategy;
import my_sawit.authentication_manajemen_akun.service.GoogleAuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthStrategy authStrategy;
    private final GoogleAuthServiceImpl googleAuthServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request){
        ApiResponse<AuthResponseDTO> response = authStrategy.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {

        ApiResponse<AuthResponseDTO> response = authStrategy.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> googleLogin(@Valid @RequestBody GoogleAuthRequestDTO request) {

        ApiResponse<AuthResponseDTO> response = googleAuthServiceImpl.authenticate(request);

        if (response.getStatusCode() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
    }

    @GetMapping("/tes")
    public Map<String, Object> tes() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello World");
        response.put("status", "success");
        return response;
    }

}
