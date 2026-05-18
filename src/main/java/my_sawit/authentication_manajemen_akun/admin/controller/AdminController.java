package my_sawit.authentication_manajemen_akun.admin.controller;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.admin.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
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


    @PutMapping("/{buruhId}/assign-mandor/{mandorId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> assignMandor(
            @PathVariable UUID buruhId,
            @PathVariable UUID mandorId
    ) {
        ApiResponse<UserResponseDTO> response = adminService.assignMandor(buruhId, mandorId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @PutMapping("/{buruhId}/unassign-mandor")
    public ResponseEntity<ApiResponse<UserResponseDTO>> unassignMandor(
            @PathVariable UUID buruhId
    ) {
        ApiResponse<UserResponseDTO> response = adminService.unassignMandor(buruhId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId, Principal principal) {
        ApiResponse<Void> response = adminService.deleteUser(userId, principal.getName());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserDetail(@PathVariable UUID userId) {
        ApiResponse<UserResponseDTO> response = adminService.getUserDetail(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}