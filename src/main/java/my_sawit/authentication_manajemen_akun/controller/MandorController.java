package my_sawit.authentication_manajemen_akun.controller;

import lombok.RequiredArgsConstructor;
import my_sawit.authentication_manajemen_akun.dto.request.BawahanSearchRequestDTO;
import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.service.MandorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/mandor")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANDOR')")
public class MandorController {

    private final MandorService mandorService;

    @GetMapping("/bawahan")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getMyBawahan(Principal principal, @ModelAttribute BawahanSearchRequestDTO request ) {
        List<UserResponseDTO> bawahanList = mandorService.getMyBawahan(principal.getName(), request);

        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Berhasil mengambil daftar bawahan",
                bawahanList
        ));
    }
}