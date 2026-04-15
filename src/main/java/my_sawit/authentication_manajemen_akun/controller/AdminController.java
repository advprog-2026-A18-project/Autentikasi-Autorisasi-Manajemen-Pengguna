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
}