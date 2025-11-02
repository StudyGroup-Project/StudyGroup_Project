package com.study.focus.resource.service;

import com.study.focus.account.domain.Job;
import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.UserService;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.common.domain.Category;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.GroupService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.dto.CreateResourceRequest;
import com.study.focus.resource.dto.GetResourceDetailResponse;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.dto.UpdateResourceRequest;
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
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


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
    private UserService userService;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private S3Uploader s3Uploader;


    @BeforeEach
    void setUp(){
        groupService = new GroupService(studyMemberRepository);
        resourceService = new ResourceService(resourceRepository,groupService,userService,fileRepository,s3Uploader);
    }

    User testUser = User.builder().id(1L). trustScore(30L).lastLoginAt(LocalDateTime.now()).build();
    Study testStudy = Study.builder().maxMemberCount(30).build();
    StudyMember teststudyMember = StudyMember.builder().user(testUser)
            .study(testStudy).id(1L).build();

    @Test
    @DisplayName("자료 목록 가져오기- 성공: 자료가 있는 경우 ")
    void getResources_success(){
        //given
        Long userId = 1L;
        Long studyId = 1L;

        Resource resource = Resource.builder()
                .study(testStudy).author(teststudyMember).title("test").description("testd").build();

        GetMyProfileResponse mockProfile = new GetMyProfileResponse(1L,"test","test"
        ,"test","30",Job.FREELANCER, List.of(Category.IT),null,30L);

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findAllByStudyId(studyId))
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
        given(resourceRepository.findAllByStudyId(studyId))
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
        assertThatThrownBy(() ->{
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
                "서울", "30", Job.FREELANCER, List.of(Category.IT),
                null, 100L
        );

        File file = File.ofResource(resource, new FileDetailDto
                ("sample.txt","fileKey","txt",123L));

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));

        given(resourceRepository.findByIdAndStudyId(resourceId,studyId  ))
                .willReturn(Optional.of(resource));

        given(userService.getMyProfile(any()))
                .willReturn(mockProfile);

        given(fileRepository.findAllByResource_Id(resourceId))
                .willReturn(List.of(file));
        given(s3Uploader.getUrlFile("fileKey"))
                .willReturn("https://s3.bucket/fileKey");

        // when
        GetResourceDetailResponse result = resourceService.getResourceDetail(studyId, resourceId, userId);

        //then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTitle()).isEqualTo(resource.getTitle());
        Assertions.assertThat(result.getContent()).isEqualTo(resource.getDescription());
        Assertions.assertThat(result.getAuthor()    ).isEqualTo(mockProfile.getNickname() );
        Assertions.assertThat(result.getProfileUrl()).isNull();
        Assertions.assertThat(result.getFiles()).hasSize(1);

    }

    @Test
    @DisplayName("자료 상세 가져오기 - 실패: 스터디 멤버가 아닌 경우 ")
    void getResourceDetail_fail_isNotStudyMember(){
        // given
        Long userId = 1L;
        Long studyId = 1L;
        Long resourceId = 100L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            resourceService.getResourceDetail(studyId, resourceId, userId);
        }).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("자료 상세 가져오기 - 실패: 자료가 없는 경우 ")
    void getResourceDetail_fail_resourceNotFound(){
        // given
        Long userId = 1L;
        Long studyId = 1L;
        Long resourceId = 100L;

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));

        given(resourceRepository.findByIdAndStudyId(resourceId,studyId))
                .willReturn(Optional.empty());


        // when && then
        assertThatThrownBy(() -> {
            resourceService.getResourceDetail(studyId, resourceId, userId);
        }).isInstanceOf(BusinessException.class);

    }

    @Test
    @DisplayName("자료 생성 성공 - 파일 없는 경우")
    void createResource_success_withoutFiles() {
        // given
        Long studyId = 1L;
        Long userId = 1L;

        CreateResourceRequest request = new CreateResourceRequest("자료제목", "자료내용", List.of());
        Resource resource = Resource.builder().author(teststudyMember).study(testStudy).title("title").description("desc").build();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.save(any(Resource.class)))
                .willReturn(resource);

        // when
        resourceService.createResource(studyId, userId, request);

        // then
        then(resourceRepository).should(times(1)).save(any(Resource.class));
        then(fileRepository).should(times(0)).save(any(File.class));
    }

    @Test
    @DisplayName("자료 생성 성공 - 파일 있는 경우")
    void createResource_success_withFiles() {
        // given
        Long studyId = 1L;
        Long userId = 1L;

        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(mockFile);


        CreateResourceRequest request = new CreateResourceRequest("자료제목", "자료내용", files);
        Resource resource = Resource.builder().author(teststudyMember).
                study(testStudy).title("title").description("desc").build();

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.save(any(Resource.class)))
                .willReturn(resource);

        FileDetailDto fileDetailDto = new FileDetailDto("file.txt", "fileKey", "txt", 10L);
        given(s3Uploader.makeMetaData(mockFile)).willReturn(fileDetailDto);

        // when
        resourceService.createResource(studyId, userId, request);

        // then
        then(resourceRepository).should(times(1)).save(any(Resource.class));
        then(fileRepository).should(times(1)).save(any(File.class));
        then(s3Uploader).should(times(1)).uploadFiles(List.of(fileDetailDto
                        .getKey()), files);
    }

    @Test
    @DisplayName("자료 생성 실패 - 스터디 멤버가 아닌 경우")
    void createResource_fail_notMember() {
        // given
        Long studyId = 1L;
        Long userId = 1L;
        CreateResourceRequest request = new CreateResourceRequest("제목", "내용", List.of());

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                resourceService.createResource(studyId, userId, request)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("자료 삭제 성공")
    void deleteResource_success() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        Resource resource = Resource.builder().author(teststudyMember).study(testStudy)
                .title("title").description("des").build();

        File file1 = File.ofResource(resource,
                new FileDetailDto("file1.txt", "fileKey", "txt", 10L));
        File file2 = File.ofResource(resource,new FileDetailDto("file2.txt", "fileKey",
                "txt", 10L));
        List<File> files = List.of(file1, file2);

        when(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).thenReturn(Optional.of(teststudyMember));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(fileRepository.findAllByResource_Id(resourceId)).thenReturn(files);

        // when
        resourceService.deleteResource(studyId, resourceId, userId);

        // then
        assertTrue(files.stream().allMatch(File::getIsDeleted));
        verify(resourceRepository, times(1)).delete(resource);
    }

    @Test
    @DisplayName("자료 삭제 - 실패 : 스터디 멤버가 아닌 경우  ")
    void deleteResource_Fail_isNotMember() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;
        when(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).thenReturn(Optional.empty());

        // when && then
        assertThatThrownBy(() ->  resourceService.deleteResource(studyId, resourceId, userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("자료 삭제 - 실패 : 저자가 아닌 경우 ")
    void deleteResource_Fail_isNotAuthor() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        User anotherUser = User.builder().id(2L). trustScore(30L).lastLoginAt(LocalDateTime.now()).build();
        StudyMember anotherStudyMember = StudyMember.builder().user(anotherUser)
                .study(testStudy).id(999L).build();

        Resource resource = Resource.builder().author(anotherStudyMember).study(testStudy)
                .title("title").description("des").build();


        when(studyMemberRepository.findByStudyIdAndUserId(studyId, userId)).thenReturn(Optional.of(teststudyMember));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

        // when && then
        assertThatThrownBy(() ->  resourceService.deleteResource(studyId, resourceId, userId))
                .isInstanceOf(BusinessException.class);
    }


    @Test
    @DisplayName("자료 수정 성공 - 제목/내용만 수정")
    void updateResource_success_updateTitleContent() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        Resource resource = Resource.builder()
                .id(resourceId)
                .author(teststudyMember)
                .study(testStudy)
                .title("oldTitle")
                .description("oldDesc")
                .build();

        UpdateResourceRequest request = new UpdateResourceRequest(
                "newTitle", "newContent", List.of(), List.of()
        );

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findById(resourceId))
                .willReturn(Optional.of(resource));

        // when
        resourceService.updateResource(studyId, resourceId, userId, request);

        // then
        then(fileRepository).should(times(0)).findAllById(any());
        then(s3Uploader).should(times(0)).makeMetaData(any());
        assert(resource.getTitle().equals("newTitle"));
        assert(resource.getDescription().equals("newContent"));
    }

    @Test
    @DisplayName("자료 수정 성공 - 파일 삭제 포함")
    void updateResource_success_deleteFiles() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        Resource resource = Resource.builder()
                .id(resourceId)
                .author(teststudyMember)
                .study(testStudy)
                .title("oldTitle")
                .description("oldDesc")
                .build();


        File file1 = File.ofResource(resource, new FileDetailDto("file1.txt", "key1", "txt", 10L));
        File file2 = File.ofResource(resource, new FileDetailDto("file2.txt", "key2", "txt", 20L));

        UpdateResourceRequest request = new UpdateResourceRequest(
                "newTitle", "newDesc", null,List.of(1L,2L));

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findById(resourceId))
                .willReturn(Optional.of(resource));
        given(fileRepository.findAllById(request.getDeleteFileIds()))
                .willReturn(List.of(file1,file2));

        // when
        resourceService.updateResource(studyId, resourceId, userId, request);

        // then
        assert(file1.getIsDeleted());
        assert(file2.getIsDeleted());
    }
    @Test
    @DisplayName("자료 수정 성공 - 새 파일 업로드 포함")
    void updateResource_success_addFiles() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        Resource resource = Resource.builder()
                .id(resourceId)
                .author(teststudyMember)
                .study(testStudy)
                .title("oldTitle")
                .description("oldDesc")
                .build();

        MultipartFile mockFile = mock(MultipartFile.class);
        FileDetailDto meta = new FileDetailDto("new.txt", "newKey", "txt", 5L);

        UpdateResourceRequest request = new UpdateResourceRequest(
                "newTitle", "newDesc", List.of(mockFile),null);

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findById(resourceId))
                .willReturn(Optional.of(resource));
        given(s3Uploader.makeMetaData(mockFile)).willReturn(meta);

        // when
        resourceService.updateResource(studyId, resourceId, userId, request);

        // then
        then(fileRepository).should(times(1)).save(any(File.class));
        then(s3Uploader).should(times(1)).uploadFiles(List.of("newKey"), List.of(mockFile));
    }

    @Test
    @DisplayName("자료 수정 실패 - 스터디 멤버가 아님")
    void updateResource_fail_notMember() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        UpdateResourceRequest request = new UpdateResourceRequest("t", "c", List.of(), List.of());

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                resourceService.updateResource(studyId, resourceId, userId, request)
        ).isInstanceOf(BusinessException.class);
    }
    @Test
    @DisplayName("자료 수정 실패 - 작성자가 아닌 경우")
    void updateResource_fail_notAuthor() {
        // given
        Long studyId = 1L;
        Long resourceId = 100L;
        Long userId = 10L;

        User anotherUser = User.builder().id(2L).build();
        StudyMember anotherMember = StudyMember.builder().id(999L).user(anotherUser).study(testStudy).build();
        Resource resource = Resource.builder().id(resourceId).author(anotherMember).study(testStudy).build();

        UpdateResourceRequest request = new UpdateResourceRequest("t", "c", List.of(), List.of());

        given(studyMemberRepository.findByStudyIdAndUserId(studyId, userId))
                .willReturn(Optional.of(teststudyMember));
        given(resourceRepository.findById(resourceId))
                .willReturn(Optional.of(resource));

        // when & then
        assertThatThrownBy(() ->
                resourceService.updateResource(studyId, resourceId, userId, request)
        ).isInstanceOf(BusinessException.class);

    }






}