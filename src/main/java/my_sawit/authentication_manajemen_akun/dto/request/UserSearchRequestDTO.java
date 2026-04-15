package my_sawit.authentication_manajemen_akun.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequestDTO {
    private String name;
    private String email;
    private String role;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;
}