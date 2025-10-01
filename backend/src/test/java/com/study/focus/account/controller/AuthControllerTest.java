package com.study.focus.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.domain.Provider;
import com.study.focus.account.dto.LoginResponse;
import com.study.focus.account.dto.RegisterRequest;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// 👇 JPA Auditing에서 필요한 Context를 Mock으로 대체
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private UserProfileRepository userProfileRepository;
    
    // 👇 추가: JPA MappingContext를 Mock 처리 (Auditing 때문에 필요)
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("회원가입 성공")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest("id", "pw");
        doNothing().when(accountService).register(any());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
