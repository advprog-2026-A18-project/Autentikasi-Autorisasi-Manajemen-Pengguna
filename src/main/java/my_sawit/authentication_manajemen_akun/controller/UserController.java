package my_sawit.authentication_manajemen_akun.controller;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
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
}