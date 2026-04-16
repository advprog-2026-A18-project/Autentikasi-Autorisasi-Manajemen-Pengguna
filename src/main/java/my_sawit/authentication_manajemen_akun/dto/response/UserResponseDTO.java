package my_sawit.authentication_manajemen_akun.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String fullname;
    private String email;
    private String role;
    private String nomorSertifikasi; // null if user not mandor
    private String namaMandor;
}
