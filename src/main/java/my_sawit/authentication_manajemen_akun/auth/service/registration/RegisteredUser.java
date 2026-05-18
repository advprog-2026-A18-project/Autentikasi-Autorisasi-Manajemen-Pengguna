package my_sawit.authentication_manajemen_akun.auth.service.registration;

import my_sawit.authentication_manajemen_akun.domain.model.User;

public record RegisteredUser(
        User user,
        String nomorSertifikasi
) {
}
