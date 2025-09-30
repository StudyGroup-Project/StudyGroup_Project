package com.study.focus.announcement.service;

import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.UserService;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.domain.Comment;
import com.study.focus.announcement.dto.AnnouncementComments;
import com.study.focus.announcement.dto.AnnouncementFiles;
import com.study.focus.announcement.dto.GetAnnouncementDetailResponse;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.repository.CommentRepository;
import com.study.focus.common.domain.File;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private  final StudyMemberRepository studyMemberRepository;
    private  final FileRepository fileRepository;
    private  final S3Uploader s3Uploader;
    private  final CommentRepository commentRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserService userService;

    //ID를 통해 StudyId에 userId가 포함되는지 확인하여 그룹 내 유효성 검증
    public List<GetAnnouncementsResponse> findAllSummaries(Long studyId, Long userId)
    {
        memberValidation(studyId, userId);
        List<Announcement> resultList = announcementRepository.findAllByStudyId(studyId);
        return  resultList.stream().map(a -> {
            return new GetAnnouncementsResponse(a.getId(), a.getTitle(), a.getCreatedAt());
        }).toList();
    }


    // 공지 생성하기(공지 데이터는 컨트롤러부분에서 유효성 검증을 하기 때문에 검증 x)
    @Transactional
    public Long createAnnouncement(Long studyId, Long userId, String title, String content, List<MultipartFile> files)
    {
        StudyMember userStudyMember = memberValidation(studyId, userId);
        isLeader(userStudyMember);

        Study study = userStudyMember.getStudy();
        Announcement announcement = Announcement.builder().
                study(study).author(userStudyMember).title(title)
                .description(content).build();

        Announcement saveAnnouncements = announcementRepository.save(announcement);
        //파일이 있는 경우
        if(files !=null && !files.isEmpty()){
            List<FileDetailDto> list = files.stream().map(s3Uploader::makeMetaData).toList();
           //파일 데이터 db 저장
           IntStream.range(0,list.size())
                   .forEach(index ->fileRepository.save(
                           File.ofAnnouncement(announcement,list.get(index))
                   ));
           //파일 데이터 s3에 업로드
            List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
            s3Uploader.uploadFiles(keys,files);
        }
        return saveAnnouncements.getId();
    }

    // 공지 삭제하기
    @Transactional
    public void deleteAnnouncement(Long studyId, Long userId, Long announcementId) {
        //검증
        StudyMember userStudyMember = memberValidation(studyId, userId);
        isLeader(userStudyMember);
        Announcement findAnnouncement = validationAnnouncement(announcementId, studyId, userStudyMember.getId());


        //파일 삭제
        List<File> findAnnouncementFiles = fileRepository.findAllByAnnouncement_Id(announcementId);
        findAnnouncementFiles.forEach(File::deleteAnnouncementFile);
        //null 반영
        fileRepository.saveAll(findAnnouncementFiles);
        fileRepository.flush();

        //댓글 삭제
        List<Comment> findAnnouncementComments = commentRepository.findAllByAnnouncement_Id(announcementId);
        commentRepository.deleteAll(findAnnouncementComments);

        //공지 삭제
        announcementRepository.delete(findAnnouncement);
    }

    //공지 상세 데이터 가져오기
    public GetAnnouncementDetailResponse getAnnouncementDetail(Long studyId, Long announcementId, Long userId) {
        // 멤버 검증
        memberValidation(studyId, userId);

        //공지 가져오기
        Announcement announcement = findAnnouncement(studyId, announcementId);
        GetMyProfileResponse authorProfile = userService.getMyProfile(announcement.getAuthor().getUser().getId());

        //공지 댓글 가져오기
        List<Comment> comments = commentRepository.findAllByAnnouncement_Id(announcementId);
        List<AnnouncementComments> announcementComments = makeCommentToAnnouncementComment(comments);

        //공지 파일 가져오기
        List<File> files = fileRepository.findAllByAnnouncement_Id(announcementId);
        List<AnnouncementFiles> announcementFiles = makeFileToAnnouncementFile(files);

        return GetAnnouncementDetailResponse.builder().
                announcementId(announcementId).studyId(studyId).
                title(announcement.getTitle()).content(announcement.getDescription()).
                updatedAt(announcement.getUpdatedAt()).userName(authorProfile.getNickname()).
                userProfileImageUrl(authorProfile.getProfileImageUrl())
                .comments(announcementComments).files(announcementFiles).build();
    }

    @NotNull
    private List<AnnouncementFiles> makeFileToAnnouncementFile(List<File> files) {
        return files.stream().map(f -> {
            String fileUrl = s3Uploader.getUrlFile(f.getFileKey());
            return AnnouncementFiles.builder()
                    .fileId(f.getId())
                    .fileName(f.getFileName())
                    .fileUrl(fileUrl)
                    .build();
        }).toList();
    }

    @NotNull
    private List<AnnouncementComments> makeCommentToAnnouncementComment(List<Comment> comments) {
        return comments.stream().map(c -> {
            GetMyProfileResponse commentUserProfile = userService.getMyProfile(c.getCommenter().getUser().getId());
            return AnnouncementComments.builder()
                    .commentId(c.getId())
                    .userName(commentUserProfile.getNickname())
                    .userProfileImageUrl(commentUserProfile.getProfileImageUrl())
                    .content(c.getContent())
                    .createdAt(c.getCreatedAt())
                    .build();
        }).toList();
    }

    private  Announcement findAnnouncement(Long studyId, Long announcementId) {
         return announcementRepository.findByIdAndStudyId(announcementId, studyId)
                 .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }


    private  Announcement validationAnnouncement(Long announcementId, Long studyId, Long authorId)
    {
        if(studyId == null || authorId ==null || announcementId ==null)
        {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }
        Optional<Announcement> announcement = announcementRepository.findByIdAndStudy_IdAndAuthor_Id(announcementId, studyId,authorId);
        return announcement.orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }





    private static void isLeader(StudyMember userStudyMember) {
        if(!userStudyMember.getRole().equals(StudyRole.LEADER))
        {
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }
    }



    // 공지 수정하기
    public void updateAnnouncement(Long studyId, Long announcementId) {
        // TODO: 공지 수정
    }



    //인터셉터 및 Aop 반영 시 수정 필요
    private StudyMember memberValidation(Long studyId, Long userId) {
        if(studyId == null || userId == null) {throw new BusinessException(CommonErrorCode.INVALID_REQUEST);}
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId).
                orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }


}
