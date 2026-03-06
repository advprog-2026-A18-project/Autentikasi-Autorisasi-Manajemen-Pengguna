package my_sawit.authentication_manajemen_akun.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Email should be filled!")
    @Email(message = "Email format is invalid!")
    private String email;

    @NotBlank(message = "Password should be filled!")
    private String password;
}
