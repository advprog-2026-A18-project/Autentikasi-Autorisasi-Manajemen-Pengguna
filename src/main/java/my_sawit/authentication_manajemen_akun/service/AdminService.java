package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AdminService {

    Page<UserResponseDTO> searchUsers(String name, String email, String role, int page, int size);

    UserResponseDTO assignMandor(UUID buruhId, UUID mandorId);
    UserResponseDTO unassignMandor(UUID buruhId);

    void deleteUser(UUID userId, String adminEmail);

    UserResponseDTO getUserDetail(UUID userId);

}
