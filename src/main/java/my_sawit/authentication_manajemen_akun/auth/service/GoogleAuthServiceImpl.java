package my_sawit.authentication_manajemen_akun.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import static my_sawit.authentication_manajemen_akun.common.helper.ConvertResponseHandler.convertToAuthResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements OAuthService<GoogleAuthRequestDTO> {

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
                return ApiResponse.unauthorized("Invalid Google Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8);

            Optional<User> userOptional = userRepository.findByEmail(email);


            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();

                if (!"GOOGLE".equalsIgnoreCase(existingUser.getAuthProvider())) {
                    return ApiResponse.badRequest("Email is already registered with local authentication");
                }
                return processExistingUser(existingUser);
            } else {
                return registerNewGoogleUser(request, email, name, username);
            }

        } catch (GeneralSecurityException | IOException e) {
            log.error("Error while verification Google Token: ", e);
            return ApiResponse.internalServerError("Error while verification Google Token: " + e.getMessage());
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
        AuthResponseDTO authResponseDTO = convertToAuthResponseDTO(user, nomorSertifikasi, refreshTokenService, jwtUtils);
        return ApiResponse.success("Google Auth succeed! You are authenticated", authResponseDTO);
    }

    private ApiResponse<AuthResponseDTO> registerNewGoogleUser(GoogleAuthRequestDTO request, String email, String name, String username) {
        if (request.getRole() == null || request.getRole().isBlank()) {
            return ApiResponse.badRequest("Account hasn't registered. Please select one of the roles (BURUH/MANDOR/SUPIR) to be registered.");
        }

        if (ROLE_ADMIN.equalsIgnoreCase(request.getRole())) {
            return ApiResponse.forbidden("Registration as ADMIN is not allowed");
        }

        Optional<Role> roleChecker = roleRepository.findByName(request.getRole().toUpperCase());

        if (roleChecker.isEmpty()) {
            return ApiResponse.badRequest("Role invalid: " + request.getRole());
        }

        Role userRole = roleChecker.get();

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

        User user = User.builder()
                .username(username)
                .fullname(name)
                .email(email)
                .password(null)
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
        AuthResponseDTO authResponseDTO = convertToAuthResponseDTO(user, nomorSertifikasi, refreshTokenService, jwtUtils);
        return ApiResponse.created("Google registration succeed! You are authenticated", authResponseDTO);
    }


    @lombok.Generated
    protected GoogleIdToken verifyGoogleToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        return verifier.verify(idTokenString);
    }

}