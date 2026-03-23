package my_sawit.authentication_manajemen_akun.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.TokenRefreshRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.service.AuthStrategy;
import my_sawit.authentication_manajemen_akun.service.GoogleAuthServiceImpl;
import my_sawit.authentication_manajemen_akun.service.RefreshTokenServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthStrategy authStrategy;
    private final GoogleAuthServiceImpl googleAuthServiceImpl;
    private final RefreshTokenServiceImpl refreshTokenServiceImpl;

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
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refresh(@Valid @RequestBody TokenRefreshRequestDTO request) {
        AuthResponseDTO authData = refreshTokenServiceImpl.refreshAccessToken(request.getRefreshToken());

        return ResponseEntity.ok(new ApiResponse<>(200, "Token refreshed successfully", authData));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequestDTO request) {

        refreshTokenServiceImpl.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(200, "Successfully logout", null));
    }


}
