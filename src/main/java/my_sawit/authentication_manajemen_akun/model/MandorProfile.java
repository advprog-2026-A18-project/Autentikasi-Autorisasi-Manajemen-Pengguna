package my_sawit.authentication_manajemen_akun.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "mandor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "nomor_sertifikasi", nullable = false, unique = true)
    private String nomorSertifikasi;

}
