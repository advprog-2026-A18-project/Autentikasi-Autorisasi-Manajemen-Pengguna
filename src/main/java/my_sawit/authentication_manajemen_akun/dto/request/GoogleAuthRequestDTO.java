package my_sawit.authentication_manajemen_akun.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequestDTO {

    @NotBlank(message = "Google ID Token should be filled")
    private String idToken;

    private String role;

    private String nomorSertifikasi;

}
