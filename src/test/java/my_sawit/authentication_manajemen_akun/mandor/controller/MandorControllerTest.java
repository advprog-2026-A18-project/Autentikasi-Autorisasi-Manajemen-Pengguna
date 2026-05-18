package my_sawit.authentication_manajemen_akun.mandor.controller;

import my_sawit.authentication_manajemen_akun.dto.response.ApiResponse;
import my_sawit.authentication_manajemen_akun.dto.response.UserResponseDTO;
import my_sawit.authentication_manajemen_akun.mandor.service.MandorService;
import my_sawit.authentication_manajemen_akun.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MandorController.class)
@AutoConfigureMockMvc(addFilters = false)
class MandorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MandorService mandorService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    void getMyBawahan_ShouldReturn200AndList() throws Exception {
        Principal mockPrincipal = () -> "mandor@sawit.com";

        UserResponseDTO bawahanDTO = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("budi")
                .fullname("Budi Buruh")
                .role("BURUH")
                .build();

        List<UserResponseDTO> mockList = List.of(bawahanDTO);

        when(mandorService.getMyBawahan(eq("mandor@sawit.com"), any()))
                .thenReturn(ApiResponse.success("Successfully fetched bawahan", mockList));

        mockMvc.perform(get("/mandor/bawahan")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched bawahan"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("budi"))
                .andExpect(jsonPath("$.data[0].fullname").value("Budi Buruh"))
                .andExpect(jsonPath("$.data[0].role").value("BURUH"));
    }

    @Test
    void getMyBawahan_WithSearchName_ShouldPassQueryToService() throws Exception {
        Principal mockPrincipal = () -> "mandor@sawit.com";

        UserResponseDTO bawahanDTO = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("budi")
                .fullname("Budi Buruh")
                .role("BURUH")
                .build();

        when(mandorService.getMyBawahan(eq("mandor@sawit.com"), any()))
                .thenReturn(ApiResponse.success("Successfully fetched bawahan", List.of(bawahanDTO)));

        mockMvc.perform(get("/mandor/bawahan")
                        .principal(mockPrincipal)
                        .param("name", "Budi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data[0].fullname").value("Budi Buruh"));
    }


}
