package my_sawit.authentication_manajemen_akun.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.service.AuthStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthStrategy authStrategy;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request){
        ApiResponse<AuthResponseDTO> response = authStrategy.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
