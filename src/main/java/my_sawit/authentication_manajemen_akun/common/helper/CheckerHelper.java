package my_sawit.authentication_manajemen_akun.common.helper;

import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.Role;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;


public class CheckerHelper {
    private static final String ROLE_MANDOR = "MANDOR";

    public record NomorSertifikasiCheckResult(
            boolean valid,
            String message,
            String nomorSertifikasi
    ) {}

    public static NomorSertifikasiCheckResult validateNomorSertifikasiForMandor(
            Role userRole,
            String nomorSertifikasi,
            MandorProfileRepository mandorProfileRepository
    ) {
        if (userRole == null || !ROLE_MANDOR.equalsIgnoreCase(userRole.getName())) {
            return new NomorSertifikasiCheckResult(true, null, null);
        }

        if (nomorSertifikasi == null || nomorSertifikasi.isBlank()) {
            return new NomorSertifikasiCheckResult(
                    false,
                    "Mandor must fill Nomor Sertifikasi",
                    null
            );
        }

        if (mandorProfileRepository.existsByNomorSertifikasi(nomorSertifikasi)) {
            return new NomorSertifikasiCheckResult(
                    false,
                    "Nomor Sertifikasi is already registered",
                    null
            );
        }

        return new NomorSertifikasiCheckResult(true, null, nomorSertifikasi);
    }

    public static String fetchNomorSertifikasi(MandorProfileRepository mandorProfileRepository, User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return null;
        }

        if (!ROLE_MANDOR.equalsIgnoreCase(user.getRole().getName())) {
            return null;
        }

        return mandorProfileRepository.findByUser(user)
                .map(MandorProfile::getNomorSertifikasi)
                .orElse(null);
    }

}
