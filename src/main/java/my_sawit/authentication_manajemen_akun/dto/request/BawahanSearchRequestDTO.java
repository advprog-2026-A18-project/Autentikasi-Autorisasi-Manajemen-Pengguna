package my_sawit.authentication_manajemen_akun.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BawahanSearchRequestDTO {
    private String name;

}