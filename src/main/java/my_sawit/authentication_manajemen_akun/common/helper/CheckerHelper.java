package my_sawit.authentication_manajemen_akun.common.helper;

import my_sawit.authentication_manajemen_akun.domain.model.MandorProfile;
import my_sawit.authentication_manajemen_akun.domain.model.User;
import my_sawit.authentication_manajemen_akun.domain.repository.MandorProfileRepository;

import java.util.Optional;

public class CheckerHelper {
    private static final String ROLE_MANDOR = "MANDOR";

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
