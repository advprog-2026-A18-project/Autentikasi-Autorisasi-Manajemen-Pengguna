package my_sawit.authentication_manajemen_akun.mandor.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.BawahanSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.common.helper.ConvertResponseHandler;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MandorServiceImpl implements MandorService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<UserResponseDTO>> getMyBawahan(String email, BawahanSearchRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        String searchName = (request != null) ? request.getName() : null;

        List<User> bawahanList;
        if (searchName != null && !searchName.trim().isEmpty()) {
            bawahanList = userRepository.findByMandorAndFullnameContainingIgnoreCase(user, searchName.trim());
        } else {
            bawahanList = userRepository.findByMandor(user);
        }

        List<UserResponseDTO> data = bawahanList.stream()
                .map(ConvertResponseHandler::convertToUserResponseDTO)
                .collect(Collectors.toList());

        return ApiResponse.success("Successfully fetched bawahan", data);
    }


}
