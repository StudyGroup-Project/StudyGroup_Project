package com.study.focus.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FileRepository fileRepository;

    private User user;
    private Study study;
    private StudyMember studyMember;
    private Resource resource1;



    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .trustScore(50L)
                .lastLoginAt(LocalDateTime.now())
                .build());

        UserProfile userProfile = userProfileRepository.save(UserProfile.builder()
                .user(user)
                .nickname("tester")
                .job(Job.FREELANCER)
                .preferredCategory(Category.IT)
                .address(Address.builder().province("p").district("dis").build())
                .birthDate(LocalDate.now())
                .build());

        study = studyRepository.save(Study.builder()
                .maxMemberCount(20)
                .recruitStatus(RecruitStatus.OPEN)
                .build());

        studyMember = studyMemberRepository.save(StudyMember.builder()
                .user(user)
                .study(study)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .exitedAt(LocalDateTime.now().plusMonths(1))
                .build());

        resource1 = resourceRepository.save(Resource.builder()
                .study(study)
                .author(studyMember)
                .title("자료1")
                .description("내용1")
                .build());

        fileRepository.save(File.ofResource(resource1, FileDetailDto.builder().originalFileName("fileName.txt")
                .key("fileKey").contentType("txt").fileSize(10L).build()));
    }

    @AfterEach
    void after() {
        fileRepository.deleteAll();
        resourceRepository.deleteAll();
        studyMemberRepository.deleteAll();
        studyRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("자료 목록 조회- 성공")
    void getResources_success() throws Exception {
        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources")
                        .with(user(new CustomUserDetails(user.getId())))
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("자료 목록 조회- 실패: 스터디 멤버가 아닌 경우")
    void getResources_fail_notStudyMember() throws Exception {
        User anotherUser = userRepository.save(User.builder()
                .trustScore(10L)
                .lastLoginAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources")
                        .with(user(new CustomUserDetails(anotherUser.getId()))))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("자료 상세 조회 - 성공")
    void getResourceDetail_success() throws Exception {
        //when & then
        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources" +"/"+resource1.getId())
                        .with(user(new CustomUserDetails(user.getId())))
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.files.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(resource1.getTitle()));

    }
    @Test
    @DisplayName("자료 상세 가져오기 - 실패: 스터디 멤버가 아닌 경우 ")
    void getResourceDetail_fail_isNotStudyMember() throws Exception
    {
        //given
        User notStudyMember = userRepository.save(User.builder().trustScore(30L).build());


        //when & then
        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources" +"/"+resource1.getId())
                        .with(user(new CustomUserDetails(notStudyMember.getId())))
                        .with(csrf())
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("자료 상세 가져오기 - 실패: 자료가 없는 경우 ")
    void getResourceDetail_fail_resourceNotFound() throws Exception {
        //when & then
        mockMvc.perform(get("/api/studies/" + study.getId() + "/resources" +"/"+999999L)
                        .with(user(new CustomUserDetails(user.getId())))
                        .with(csrf())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("자료 생성 - 성공: 파일 포함")
    void createResource_withFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "hello.txt", "text/plain", "HelloWorld".getBytes());

        long beforeResourceCount = resourceRepository.findAll().size();
        long beforeFileCount = fileRepository.findAll().size();

        mockMvc.perform(multipart("/api/studies/" + study.getId() + "/resources")
                        .file(file)
                        .param("title", "새 자료 제목")
                        .param("content", "새 자료 내용")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(new CustomUserDetails(user.getId())))
                        .with(csrf()))
                .andExpect(status().isCreated());

        Assertions.assertThat(resourceRepository.findAll().size()).isEqualTo(beforeResourceCount + 1);
        Assertions.assertThat(fileRepository.findAll().size()).isEqualTo(beforeFileCount+1);
    }

    @Test
    @DisplayName("자료 생성 - 성공: 파일 제외 ")
    void createResource_withOutFile_success() throws Exception {


        long beforeResourceCount = resourceRepository.findAll().size();
        long beforeFileCount = fileRepository.findAll().size();

        mockMvc.perform(multipart("/api/studies/" + study.getId() + "/resources")
                        .param("title", "새 자료 제목")
                        .param("content", "새 자료 내용")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(new CustomUserDetails(user.getId())))
                        .with(csrf()))
                        .andExpect(status().isCreated());

        Assertions.assertThat(resourceRepository.findAll().size()).isEqualTo(beforeResourceCount + 1);
        Assertions.assertThat(fileRepository.findAll().size()).isEqualTo(beforeFileCount);
    }

    @Test
    @DisplayName("자료 생성 - 실패: 스터디 멤버가 아닌 경우")
    void createResource_fail_notMember() throws Exception {

        mockMvc.perform(multipart("/api/studies/" + study.getId() + "/resources")
                        .param("title", "제목")
                        .param("content", "내용")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(new CustomUserDetails(9999L)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


}
