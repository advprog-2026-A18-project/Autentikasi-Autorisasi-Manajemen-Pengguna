package my_sawit.authentication_manajemen_akun.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocalAuthServiceImpl implements AuthStrategy {


    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public ApiResponse<AuthResponseDTO> register(RegisterRequestDTO request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return new ApiResponse<>(400, "Username is already used", null);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse<>(400, "Email is already registered", null);
        }

        if (ROLE_ADMIN.equalsIgnoreCase(request.getRole())) {
            return new ApiResponse<>(403, "Registration as ADMIN is not allowed", null);
        }


        Role userRole = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role tidak valid: " + request.getRole()));

        String nomorSertifikasi = null;


        if (ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            if (request.getNomorSertifikasi() == null || request.getNomorSertifikasi().isBlank()) {
                return new ApiResponse<>(400, "Mandor must fill Nomor Sertifikasi", null);
            }
            if (mandorProfileRepository.existsByNomorSertifikasi(request.getNomorSertifikasi())) {
                return new ApiResponse<>(400, "Nomor Sertifikasi is already registered", null);
            }
            nomorSertifikasi = request.getNomorSertifikasi();
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .authProvider("LOCAL")
                .build();
        userRepository.save(newUser);

        if (ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            MandorProfile mandorProfile = MandorProfile.builder()
                    .user(newUser)
                    .nomorSertifikasi(nomorSertifikasi)
                    .build();
            mandorProfileRepository.save(mandorProfile);
        }

        return buildSuccessResponse(newUser, nomorSertifikasi, "Registration succeed, You are authenticated", 201);
    }

    @Override
    public ApiResponse<AuthResponseDTO> login(LoginRequestDTO request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(401, "Incorrect email or password", null);
        }

        User user = userOptional.get();
        if (user.getPassword() == null) {
            return new ApiResponse<>(400, "Please check whether you logged in with Google Auth", null);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ApiResponse<>(401, "Incorrect email or password", null);
        }

        String nomorSertifikasi = null;

        if (ROLE_MANDOR.equalsIgnoreCase(user.getRole().getName())) {
            Optional<MandorProfile> mandorProfileOpt = mandorProfileRepository.findByUser(user);
            if (mandorProfileOpt.isPresent()) {
                nomorSertifikasi = mandorProfileOpt.get().getNomorSertifikasi();
            }
        }

        return buildSuccessResponse(user, nomorSertifikasi, "Login succeed! You are authenticated", 200);
    }

    private ApiResponse<AuthResponseDTO> buildSuccessResponse(User user, String nomorSertifikasi, String message, int statusCode) {
        UserResponseDTO profileDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .nomorSertifikasi(nomorSertifikasi)
                .build();

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().getName());

        AuthResponseDTO authData = AuthResponseDTO.builder()
                .accessToken(token)
                .user(profileDTO)
                .build();

        return new ApiResponse<>(statusCode, message, authData);
    }
}