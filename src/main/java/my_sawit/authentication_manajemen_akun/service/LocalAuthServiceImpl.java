package my_sawit.authentication_manajemen_akun.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class LocalAuthServiceImpl implements AuthStrategy {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MandorProfileRepository mandorProfileRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public ApiResponse<AuthResponseDTO> register(RegisterRequestDTO request){

        if(userRepository.existsByUsername(request.getUsername())){
            return new ApiResponse<>(400, "Username is already used", null);
        }

        if(userRepository.existsByEmail(request.getEmail())){
            return new ApiResponse<>(400, "Email is already registered", null);
        }

        Role userRole = roleRepository.findByName(request.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role tidak valid: " + request.getRole()));

        User newUser = User.builder()
                .username(request.getUsername())
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .authProvider("LOCAL")
                .build();
        userRepository.save(newUser);

        String nomorSertifikasi = null;
        if("MANDOR".equalsIgnoreCase(userRole.getName())){
            if(request.getNomorSertifikasi() == null || request.getNomorSertifikasi().isBlank()){
                throw new RuntimeException("Mandor must fill Nomor Sertifikasi");
            }
            if(mandorProfileRepository.existsByNomorSertifikasi(request.getNomorSertifikasi())){
                return new ApiResponse<>(400, "Nomor Sertifikasi is already registered", null);
            }

            MandorProfile mandorProfile = MandorProfile.builder()
                    .user(newUser)
                    .nomorSertifikasi(request.getNomorSertifikasi())
                    .build();
            mandorProfileRepository.save(mandorProfile);
            nomorSertifikasi = request.getNomorSertifikasi();
        }

        UserResponseDTO profileDTO = UserResponseDTO.builder()
                .id(newUser.getId())
                .username(newUser.getUsername())
                .fullname(newUser.getFullname())
                .email(newUser.getEmail())
                .role(userRole.getName())
                .nomorSertifikasi(nomorSertifikasi)
                .build();

        String token = jwtUtils.generateToken(newUser.getEmail(), userRole.getName());

        AuthResponseDTO authData = AuthResponseDTO.builder()
                .accessToken(token)
                .user(profileDTO)
                .build();
        return new ApiResponse<>(201, "Registration succeed, You are authenticated", authData);

    }


}
