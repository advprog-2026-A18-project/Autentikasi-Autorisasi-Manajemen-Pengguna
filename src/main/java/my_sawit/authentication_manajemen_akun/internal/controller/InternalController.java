package my_sawit.authentication_manajemen_akun.internal.controller;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.admin.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalController {

    private final AdminService adminService;


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagingResponseDTO<UserResponseDTO>>> searchUsers(@ModelAttribute UserSearchRequestDTO searchRequest) {
        ApiResponse<PagingResponseDTO<UserResponseDTO>> response = adminService.searchUsers(
                searchRequest.getName(),
                searchRequest.getEmail(),
                searchRequest.getRole(),
                searchRequest.getPage(),
                searchRequest.getSize()
        );

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserDetail(@PathVariable UUID userId) {
        ApiResponse<UserResponseDTO> response = adminService.getUserDetail(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
