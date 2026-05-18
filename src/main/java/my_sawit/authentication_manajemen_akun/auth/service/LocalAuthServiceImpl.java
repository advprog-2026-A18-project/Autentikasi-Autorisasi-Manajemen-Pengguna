package my_sawit.authentication_manajemen_akun.auth.service;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.auth.service.registration.RegisteredUser;
import my_sawit.authentication_manajemen_akun.auth.service.registration.RegistrationCommand;
import my_sawit.authentication_manajemen_akun.auth.service.registration.UserRegistrationService;
import my_sawit.authentication_manajemen_akun.common.exception.ApiException;
import my_sawit.authentication_manajemen_akun.common.mapper.AuthResponseMapper;
import my_sawit.authentication_manajemen_akun.dto.request.LoginRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.request.RegisterRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.AuthResponseDTO;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocalAuthServiceImpl implements LocalAuthService {

    private static final String AUTH_PROVIDER_LOCAL = "LOCAL";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuthResponseMapper authResponseMapper;
    private final UserRegistrationService userRegistrationService;

    @Override
    @Transactional
    public ApiResponse<AuthResponseDTO> register(RegisterRequestDTO request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.badRequest("Username is already used");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.badRequest("Email is already registered");
        }

        RegistrationCommand command = new RegistrationCommand(
                request.getUsername(),
                request.getFullname(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                request.getNomorSertifikasi(),
                AUTH_PROVIDER_LOCAL
        );

        RegisteredUser registeredUser;
        try {
            registeredUser = userRegistrationService.register(command);
        } catch (ApiException e) {
            return ApiResponse.of(e.getStatus(), e.getMessage(), null);
        }

        String refreshToken = refreshTokenService.createRefreshToken(registeredUser.user().getId()).getToken();
        AuthResponseDTO authData = authResponseMapper.toDto(
                registeredUser.user(),
                registeredUser.nomorSertifikasi(),
                refreshToken
        );
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

        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();
        AuthResponseDTO authData = authResponseMapper.toDto(user, refreshToken);
        return ApiResponse.success("Login succeed! You are authenticated", authData);
    }


}
