package my_sawit.authentication_manajemen_akun.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {
    @NotBlank(message = "Nama lengkap tidak boleh kosong")
    private String fullname;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;
}