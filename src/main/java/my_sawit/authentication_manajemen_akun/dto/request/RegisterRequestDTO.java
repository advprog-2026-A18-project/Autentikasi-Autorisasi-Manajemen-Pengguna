package my_sawit.authentication_manajemen_akun.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    @NotBlank(message = "Username should be filled!")
    private String username;

    @NotBlank(message = "Fullname should be filled!")
    private String fullname;

    @NotBlank(message = "Email should be filled!")
    @Email(message = "Email format is invalid!")
    private String email;

    @NotBlank(message = "Password should be filled!")
    @Pattern(
        regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$",
        message = "Password must have a minimum of 8 characters and include an uppercase letter, a lowercase letter, and a number."
    )
    private String password;

    @NotBlank(message = "Role should be filled!")
    private String role;

    private String nomorSertifikasi;
}
