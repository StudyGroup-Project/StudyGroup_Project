package com.study.focus.account.controller;

import com.study.focus.account.service.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class TokenApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    // ✅ JPA 관련 빈 목 처리 (JPA metamodel must not be empty 오류 방지)
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("refresh_token 쿠키를 통해 새로운 access token을 발급한다")
    void createNewAccessToken() throws Exception {
        // given
        String refreshToken = "refresh-token-sample";
        String newAccessToken = "new-access-token";

        given(tokenService.createNewAccessToken(refreshToken))
                .willReturn(newAccessToken);

        // when & then
        mockMvc.perform(post("/api/auth/token")
                        .cookie(new Cookie("refresh_token", refreshToken))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));

        // verify
        verify(tokenService).createNewAccessToken(refreshToken);
    }
}
