package my_sawit.authentication_manajemen_akun.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true)
    private String email;

    // nullable true karena Google OAuth tidak pakai password
    @Column(nullable = true)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // membedakan user yang regist via manual n login; LOCAL --> manual, GOOGLE --> oauth
    @Column(name = "auth_provider", nullable = false)
    @Builder.Default
    private String authProvider = "LOCAL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mandor_id")
    private User mandor;


}

