package my_sawit.authentication_manajemen_akun.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequestDTO {
    @NotBlank(message = "Refresh token cannot be empty")
    private String refreshToken;
}
