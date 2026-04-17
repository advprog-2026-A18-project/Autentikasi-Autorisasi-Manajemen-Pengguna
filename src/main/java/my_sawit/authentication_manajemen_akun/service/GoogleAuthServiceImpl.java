package my_sawit.authentication_manajemen_akun.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.model.RefreshToken;
import my_sawit.authentication_manajemen_akun.model.Role;
import my_sawit.authentication_manajemen_akun.model.User;
import my_sawit.authentication_manajemen_akun.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.repository.UserRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl {

    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.google.clientId}")
    private String googleClientId;

    @Transactional
    public ApiResponse<AuthResponseDTO> authenticate(GoogleAuthRequestDTO request) {
        try {
            GoogleIdToken idToken = verifyGoogleToken(request.getIdToken());

            if (idToken == null) {
                return new ApiResponse<>(401, "Invalid Google Token", null);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String username = email.split("@")[0] + "_" + System.currentTimeMillis() % 1000;

            Optional<User> userOptional = userRepository.findByEmail(email);


            if (userOptional.isPresent()) {
                return processExistingUser(userOptional.get());
            } else {
                return registerNewGoogleUser(request, email, name, username);
            }

        } catch (Exception e) {
            log.error("Error while verification Google Token: ", e);
            return new ApiResponse<>(500, "Error while verification Google Token: " + e.getMessage(), null);
        }
    }


    private ApiResponse<AuthResponseDTO> processExistingUser(User user) {
        String nomorSertifikasi = null;

        if (ROLE_MANDOR.equalsIgnoreCase(user.getRole().getName())) {
            Optional<MandorProfile> mandorProfileOpt = mandorProfileRepository.findByUser(user);
            if (mandorProfileOpt.isPresent()) {
                nomorSertifikasi = mandorProfileOpt.get().getNomorSertifikasi();
            }
        }

        return buildSuccessResponse(user, nomorSertifikasi);
    }

    private ApiResponse<AuthResponseDTO> registerNewGoogleUser(GoogleAuthRequestDTO request, String email, String name, String username) {
        if (request.getRole() == null || request.getRole().isBlank()) {
            return new ApiResponse<>(400, "Account hasn't registered. Please select one of the roles (BURUH/MANDOR/SUPIR) to be registered.", null);
        }

        if (ROLE_ADMIN.equalsIgnoreCase(request.getRole())) {
            return new ApiResponse<>(403, "Registration as ADMIN is not allowed", null);
        }

        Role userRole = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role invalid: " + request.getRole()));

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

        User user = User.builder()
                .username(username)
                .fullname(name)
                .email(email)
                .password(null) // Password null because login via Google
                .role(userRole)
                .authProvider("GOOGLE")
                .build();
        user = userRepository.save(user);

        if (ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            MandorProfile mandorProfile = MandorProfile.builder()
                    .user(user)
                    .nomorSertifikasi(nomorSertifikasi)
                    .build();
            mandorProfileRepository.save(mandorProfile);
        }

        return buildSuccessResponse(user, nomorSertifikasi);
    }

    private ApiResponse<AuthResponseDTO> buildSuccessResponse(User user, String nomorSertifikasi) {
        String namaMandor = (user.getMandor() != null) ? user.getMandor().getFullname() : null;

        UserResponseDTO profileDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .nomorSertifikasi(nomorSertifikasi)
                .namaMandor(namaMandor)
                .build();

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().getName(), user.getId().toString());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthResponseDTO authData = AuthResponseDTO.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .user(profileDTO)
                .build();

        return new ApiResponse<>(200, "Google Auth succeed! You are authenticated", authData);
    }

    @lombok.Generated
    protected GoogleIdToken verifyGoogleToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        return verifier.verify(idTokenString);
    }

}