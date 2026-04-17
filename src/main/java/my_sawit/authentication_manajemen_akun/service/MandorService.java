package my_sawit.authentication_manajemen_akun.service;

import my_sawit.authentication_manajemen_akun.dto.request.BawahanSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import java.util.List;

public interface MandorService {
    List<UserResponseDTO> getMyBawahan(String email, BawahanSearchRequestDTO request);
}