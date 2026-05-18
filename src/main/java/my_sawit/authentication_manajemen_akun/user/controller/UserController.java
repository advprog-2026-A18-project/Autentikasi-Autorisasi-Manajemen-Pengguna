package my_sawit.authentication_manajemen_akun.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMyProfile(Principal principal) {
        ApiResponse<UserResponseDTO> response = userService.getMyProfile(principal.getName());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMyProfile(
            Principal principal,
            @Valid @RequestBody UserUpdateRequestDTO request
    ) {
        ApiResponse<UserResponseDTO> response = userService.updateMyProfile(principal.getName(), request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}