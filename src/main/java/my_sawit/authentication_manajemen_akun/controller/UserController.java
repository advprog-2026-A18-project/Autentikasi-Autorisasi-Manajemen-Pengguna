package my_sawit.authentication_manajemen_akun.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserUpdateRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.UserService;
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
        UserResponseDTO profile = userService.getMyProfile(principal.getName());

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Berhasil mengambil profil",
                profile
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMyProfile(Principal principal, @Valid @RequestBody UserUpdateRequestDTO request) {

        UserResponseDTO updatedProfile = userService.updateMyProfile(principal.getName(), request);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Berhasil memperbarui profil",
                updatedProfile
        ));
    }
}