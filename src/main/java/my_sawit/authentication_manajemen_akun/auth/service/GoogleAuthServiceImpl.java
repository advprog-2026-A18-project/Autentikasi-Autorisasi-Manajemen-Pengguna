package my_sawit.authentication_manajemen_akun.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_sawit.authentication_manajemen_akun.common.helper.CheckerHelper;
import my_sawit.authentication_manajemen_akun.common.mapper.AuthResponseMapper;
import my_sawit.authentication_manajemen_akun.dto.request.GoogleAuthRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.RoleRepository;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements OAuthService<GoogleAuthRequestDTO> {

    private static final String ROLE_MANDOR = "MANDOR";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthResponseMapper authResponseMapper;

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
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        AuthResponseDTO authResponseDTO = authResponseMapper.toDto(user, refreshToken);
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

        CheckerHelper.NomorSertifikasiCheckResult sertifikasiCheck =
                CheckerHelper.validateNomorSertifikasiForMandor(
                        userRole,
                        request.getNomorSertifikasi(),
                        mandorProfileRepository
                );

        if (!sertifikasiCheck.valid()) {
            return ApiResponse.badRequest(sertifikasiCheck.message());
        }

        String nomorSertifikasi = sertifikasiCheck.nomorSertifikasi();

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
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        AuthResponseDTO authResponseDTO = authResponseMapper.toDto(user, nomorSertifikasi, refreshToken);
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
