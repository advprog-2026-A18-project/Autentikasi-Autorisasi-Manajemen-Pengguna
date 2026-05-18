package my_sawit.authentication_manajemen_akun.auth.service.registration;

public record RegistrationCommand(
        String username,
        String fullname,
        String email,
        String encodedPassword,
        String role,
        String nomorSertifikasi,
        String authProvider
) {
}
