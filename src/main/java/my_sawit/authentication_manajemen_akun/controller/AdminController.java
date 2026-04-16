package my_sawit.authentication_manajemen_akun.controller;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.UserSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.UserService;
import org.springframework.data.domain.Page;
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

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagingResponseDTO<UserResponseDTO>>> searchUsers(@ModelAttribute UserSearchRequestDTO searchRequest) {
        Page<UserResponseDTO> usersPage = userService.searchUsers(
                searchRequest.getName(),
                searchRequest.getEmail(),
                searchRequest.getRole(),
                searchRequest.getPage(),
                searchRequest.getSize()
        );

        String message = usersPage.isEmpty() ? "No users fetched" : "Berhasil mengambil daftar pengguna";

        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(usersPage.getContent())
                .currentPage(usersPage.getNumber())
                .totalPages(usersPage.getTotalPages())
                .totalElements(usersPage.getTotalElements())
                .build();

        return ResponseEntity.ok(new ApiResponse<>(200, message, pagingData));
    }


    @PutMapping("/{buruhId}/assign-mandor/{mandorId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> assignMandor(
            @PathVariable UUID buruhId,
            @PathVariable UUID mandorId
    ) {
        UserResponseDTO updatedUser = userService.assignMandor(buruhId, mandorId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Berhasil menugaskan mandor",
                updatedUser
        ));
    }


    @PutMapping("/{buruhId}/unassign-mandor")
    public ResponseEntity<ApiResponse<UserResponseDTO>> unassignMandor(
            @PathVariable UUID buruhId
    ) {
        UserResponseDTO updatedUser = userService.unassignMandor(buruhId);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Berhasil mencopot penugasan mandor",
                updatedUser
        ));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable UUID userId, Principal principal) {
        userService.deleteUser(userId, principal.getName());

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Berhasil menghapus pengguna",
                null
        ));
    }
}