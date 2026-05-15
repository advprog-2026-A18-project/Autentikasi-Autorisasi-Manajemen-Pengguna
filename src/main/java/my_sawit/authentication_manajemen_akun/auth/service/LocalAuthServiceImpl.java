package my_sawit.authentication_manajemen_akun.auth.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
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

import static my_sawit.authentication_manajemen_akun.helper.ConvertResponseHandler.convertToAuthResponseDTO;

@Service
@RequiredArgsConstructor
public class LocalAuthServiceImpl implements LocalAuthService {


    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public ApiResponse<AuthResponseDTO> register(RegisterRequestDTO request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.badRequest("Username is already used");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.badRequest("Email is already registered");
        }

        if (ROLE_ADMIN.equalsIgnoreCase(request.getRole())) {
            return ApiResponse.forbidden("Registration as ADMIN is not allowed");
        }


        Role userRole = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role tidak valid: " + request.getRole()));

        String nomorSertifikasi = null;


        if (ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            if (request.getNomorSertifikasi() == null || request.getNomorSertifikasi().isBlank()) {
                return ApiResponse.badRequest("Mandor must fill Nomor Sertifikasi");
            }
            if (mandorProfileRepository.existsByNomorSertifikasi(request.getNomorSertifikasi())) {
                return ApiResponse.badRequest("Nomor Sertifikasi is already registered");
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
        newUser = userRepository.save(newUser);

        if (ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            MandorProfile mandorProfile = MandorProfile.builder()
                    .user(newUser)
                    .nomorSertifikasi(nomorSertifikasi)
                    .build();
            mandorProfileRepository.save(mandorProfile);
        }
        AuthResponseDTO authData = convertToAuthResponseDTO(newUser, nomorSertifikasi, refreshTokenService, jwtUtils);
        return ApiResponse.created("Registration succeed, You are authenticated", authData);
    }

    @Override
    public ApiResponse<AuthResponseDTO> login(LoginRequestDTO request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ApiResponse.unauthorized("Incorrect email or password");
        }

        User user = userOptional.get();
        if (user.getPassword() == null) {
            return ApiResponse.badRequest("Please check whether you logged in with Google Auth");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.unauthorized("Incorrect email or password");
        }

        String nomorSertifikasi = null;

        if (ROLE_MANDOR.equalsIgnoreCase(user.getRole().getName())) {
            Optional<MandorProfile> mandorProfileOpt = mandorProfileRepository.findByUser(user);
            if (mandorProfileOpt.isPresent()) {
                nomorSertifikasi = mandorProfileOpt.get().getNomorSertifikasi();
            }
        }
        AuthResponseDTO authData = convertToAuthResponseDTO(user, nomorSertifikasi, refreshTokenService, jwtUtils);
        return ApiResponse.success("Login succeed! You are authenticated", authData);
    }


}