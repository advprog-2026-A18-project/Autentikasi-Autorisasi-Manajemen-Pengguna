package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserResponseDTO> searchUsers(String name, String email, String role, int page, int size);


}
