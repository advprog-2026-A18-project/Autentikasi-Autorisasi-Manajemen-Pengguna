package my_sawit.authentication_manajemen_akun.admin.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.PagingResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.common.helper.ConvertResponseHandler;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.RefreshTokenRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final MandorProfileRepository mandorProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagingResponseDTO<UserResponseDTO>> searchUsers(String name, String email, String role, int page, int size) {

        if (role != null && !role.isBlank()) {
            String roleUpper = role.toUpperCase();
            if (!roleUpper.equals("BURUH") && !roleUpper.equals("MANDOR") &&
                    !roleUpper.equals("SUPIR") && !roleUpper.equals("ADMIN")) {
                throw new IllegalArgumentException("Role invalid: " + role);
            }
            role = roleUpper;
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersPage = userRepository.searchUsers(name, email, role, pageable);

        Page<UserResponseDTO> responsePage = usersPage.map(
                user -> ConvertResponseHandler.convertToUserResponseDTO(user, mandorProfileRepository)
        );

        PagingResponseDTO<UserResponseDTO> pagingData = PagingResponseDTO.<UserResponseDTO>builder()
                .content(responsePage.getContent())
                .currentPage(responsePage.getNumber())
                .totalPages(responsePage.getTotalPages())
                .totalElements(responsePage.getTotalElements())
                .build();

        String message = responsePage.isEmpty()
                ? "No users fetched"
                : "Successfully fetched users list";

        return ApiResponse.success(message, pagingData);
    }

    @Override
    @Transactional
    public ApiResponse<UserResponseDTO> assignMandor(UUID buruhId, UUID mandorId) {
        User buruh = userRepository.findById(buruhId)
                .orElseThrow(() -> new RuntimeException("Data Buruh tidak ditemukan"));

        User mandor = userRepository.findById(mandorId)
                .orElseThrow(() -> new RuntimeException("Data Mandor tidak ditemukan"));

        if (buruh.getRole() == null || !"BURUH".equalsIgnoreCase(buruh.getRole().getName())) {
            throw new IllegalArgumentException("Pengguna yang ditugaskan harus memiliki role BURUH.");
        }

        if (mandor.getRole() == null || !"MANDOR".equalsIgnoreCase(mandor.getRole().getName())) {
            throw new IllegalArgumentException("Target atasan harus memiliki role MANDOR.");
        }

        buruh.setMandor(mandor);
        userRepository.save(buruh);
        UserResponseDTO data = ConvertResponseHandler.convertToUserResponseDTO(buruh, mandorProfileRepository);
        return ApiResponse.success("Successfully assigned Mandorr", data);
    }

    @Override
    @Transactional
    public ApiResponse<UserResponseDTO> unassignMandor(UUID buruhId) {
        User buruh = userRepository.findById(buruhId)
                .orElseThrow(() -> new RuntimeException("Data Buruh tidak ditemukan"));

        if (buruh.getRole() == null || !"BURUH".equalsIgnoreCase(buruh.getRole().getName())) {
            throw new IllegalArgumentException("Hanya role BURUH yang dapat dicopot penugasannya.");
        }

        buruh.setMandor(null);
        userRepository.save(buruh);
        UserResponseDTO data = ConvertResponseHandler.convertToUserResponseDTO(buruh, mandorProfileRepository);
        return ApiResponse.success("Successfully unassign mandor", data);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(UUID targetId, String currentAdminEmail) {
        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Data pengguna tidak ditemukan"));

        if (targetUser.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("Admin tidak dapat menghapus dirinya sendiri.");
        }

        List<User> buruhList = userRepository.findByMandor(targetUser);
        for (User buruh : buruhList) {
            buruh.setMandor(null);
            userRepository.save(buruh);
        }
        userRepository.flush();

        refreshTokenRepository.findByUser(targetUser)
                .ifPresent(refreshTokenRepository::delete);

        if (targetUser.getRole() != null && "MANDOR".equalsIgnoreCase(targetUser.getRole().getName())) {
            mandorProfileRepository.findByUser(targetUser)
                    .ifPresent(mandorProfileRepository::delete);
        }

        userRepository.flush();

        userRepository.deleteById(targetId);
        return ApiResponse.success("Successfully deleted users", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserResponseDTO> getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Data pengguna tidak ditemukan"));

        UserResponseDTO data = ConvertResponseHandler.convertToUserResponseDTO(user, mandorProfileRepository);
        return ApiResponse.success("Berhasil mengambil detail pengguna", data);
    }




}