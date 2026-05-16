package my_sawit.authentication_manajemen_akun.admin.service;

import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AdminService {

    ApiResponse<PagingResponseDTO<UserResponseDTO>> searchUsers(
            String name,
            String email,
            String role,
            int page,
            int size
    );

    ApiResponse<UserResponseDTO> assignMandor(UUID buruhId, UUID mandorId);

    ApiResponse<UserResponseDTO> unassignMandor(UUID buruhId);

    ApiResponse<Void> deleteUser(UUID userId, String adminEmail);

    ApiResponse<UserResponseDTO> getUserDetail(UUID userId);

}
