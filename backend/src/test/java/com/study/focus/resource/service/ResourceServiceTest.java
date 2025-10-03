package com.study.focus.resource.service;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.UserService;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.GroupService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.dto.GetResourceDetailResponse;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.repository.StudyMemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @InjectMocks
    private GroupService groupService;

    @InjectMocks
    private ResourceService resourceService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserService userService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private S3Uploader s3Uploader;


    @BeforeEach
    void setUp(){
        groupService = new GroupService(studyMemberRepository);
        resourceService = new ResourceService(resourceRepository,groupService,userService);
    }

    User testUser = User.builder().id(1L). trustScore(30L).lastLoginAt(LocalDateTime.now()).build();
    Study testStudy = Study.builder().maxMemberCount(30).build();
    StudyMember teststudyMember = StudyMember.builder().user(testUser).study(testStudy).build();

    @Test
    @DisplayName("자료 목록 가져오기- 성공: 자료가 있는 경우 ")
    void getResources_success(){
        //given
        Long userId = 1L;
        Long studyId = 1L;

        Resource resource = Resource.builder()
                .study(testStudy).author(teststudyMember).title("test").description("testd").build();

        GetMyProfileResponse mockProfile = new GetMyProfileResponse(1L,"test","test"
        ,"test","30",Job.FREELANCER, Category.IT,null,30L);

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findAllByStudy_Id(studyId))
                .willReturn(List.of(resource));
        given(userService.getMyProfile(any()))
                .willReturn(mockProfile);

        //when
        List<GetResourcesResponse> result = resourceService.getResources(studyId, userId);

        //then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getUserName()).isEqualTo("test");
    }

    @Test
    @DisplayName("자료 목록 가져오기- 성공: 자료가 없는 경우")
    void getResources_success_EmptyList(){
        //given
        Long userId = 1L;
        Long studyId = 1L;


        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findAllByStudy_Id(studyId))
                .willReturn(Collections.emptyList());

        //when
        List<GetResourcesResponse> result = resourceService.getResources(studyId, userId);

        //then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("자료 목록 가져오기- 실패: 스터디 멤버가 아닌경우")
    void getResources_Fail_isNotMember(){
        //given
        Long userId = 1L;
        Long studyId = 1L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.empty());

        //when && then
        Assertions.assertThatThrownBy(() ->{
            resourceService.getResources(studyId, userId);
        }).isInstanceOf(BusinessException.class);

    }

    @Test
    @DisplayName("자료 상세 가져오기 - 성공")
    void getResourceDetail_success(){
        // given
        Long userId = 1L;
        Long studyId = 1L;
        Long resourceId = 100L;

        Resource resource = Resource.builder()
                .id(resourceId)
                .study(testStudy)
                .author(teststudyMember)
                .title("자료 제목")
                .description("자료 설명")
                .build();

        GetMyProfileResponse mockProfile = new GetMyProfileResponse(
                1L, "authorNick", "profileImg.png",
                "서울", "30", Job.FREELANCER, Category.IT,
                null, 100L
        );

        File file = File.ofResource(resource, new FileDetailDto
                ("sample.txt","fileKey","txt",123L));

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findById(resourceId))
                .willReturn(Optional.of(resource));
        given(userService.getMyProfile(any()))
                .willReturn(mockProfile);
        given(fileRepository.findAllByResource_Id(resourceId))
                .willReturn(List.of(file));
        given(s3Uploader.getUrlFile("fileKey"))
                .willReturn("https://s3.bucket/fileKey");

        // when
        GetResourceDetailResponse result = resourceService.getResourceDetail(studyId, resourceId, userId);

        // then
        /*
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTitle()).isEqualTo("자료 제목");
        Assertions.assertThat(result.getDescription()).isEqualTo("자료 설명");
        Assertions.assertThat(result.getUserName()).isEqualTo("authorNick");
        Assertions.assertThat(result.getProfileImageUrl()).isEqualTo("profileImg.png");
        Assertions.assertThat(result.getFiles()).hasSize(1);

        ResourceDetailFileDto fileDto = result.getFiles().get(0);
        Assertions.assertThat(fileDto.getFileName()).isEqualTo("sample.txt");
        Assertions.assertThat(fileDto.getFileUrl()).isEqualTo("https://s3.bucket/fileKey");

         */
    }




}