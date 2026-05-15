package my_sawit.authentication_manajemen_akun.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.auth.service.LocalAuthService;
import my_sawit.authentication_manajemen_akun.auth.service.OAuthService;
import my_sawit.authentication_manajemen_akun.auth.service.RefreshTokenService;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.TokenRefreshRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LocalAuthService localAuthService;
    private final OAuthService<GoogleAuthRequestDTO> googleAuthService;

    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request){
        ApiResponse<AuthResponseDTO> response = localAuthService.register(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        ApiResponse<AuthResponseDTO> response = localAuthService.login(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> googleLogin(@Valid @RequestBody GoogleAuthRequestDTO request) {
        ApiResponse<AuthResponseDTO> response = googleAuthService.authenticate(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refresh(@Valid @RequestBody TokenRefreshRequestDTO request) {
        AuthResponseDTO authData = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(200, "Token refreshed successfully", authData));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequestDTO request) {

        refreshTokenService.deleteByToken(request.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse<>(200, "Successfully logout", null));
    }


}
