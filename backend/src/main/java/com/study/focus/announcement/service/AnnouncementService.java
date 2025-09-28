package com.study.focus.announcement.service;

import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.domain.Comment;
import com.study.focus.announcement.dto.AnnouncementUpdateDto;
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
            fileUploadDbAndS3(files, list, announcement);
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


    // 공지 수정하기
    @Transactional
    public void updateAnnouncement(Long studyId, Long announcementId, Long userId, AnnouncementUpdateDto updateDto) {
        //검증
        //1. 스터디 멤버 검증
        StudyMember userStudyMember = memberValidation(studyId, userId);
        //2. 방장 검증
        isLeader(userStudyMember);
        //3. 공지 검증
        Announcement oldAnnouncement = validationAnnouncement(announcementId, studyId, userStudyMember.getId());
        //4. 내용 업데이트
        oldAnnouncement.updateAnnouncement(updateDto.getTitle(), updateDto.getContent());

        //5. 파일 처리
        //5-1. 삭제할 파일이 있는 경우
        if(updateDto.getDeleteFileIds() !=null&& !updateDto.getDeleteFileIds().isEmpty())
        {
            List<File> deleteFiles = fileRepository.findAllById(updateDto.getDeleteFileIds());
            deleteFiles.forEach(File::deleteAnnouncementFile);
            fileRepository.saveAll(deleteFiles);
            fileRepository.flush();
        }
        //5-2. 새로 추가할 파일이 있는 경우
        if(updateDto.getFiles() != null && !updateDto.getFiles().isEmpty()) {
            List<FileDetailDto> list = updateDto.getFiles().stream().map(s3Uploader::makeMetaData).toList();
            fileUploadDbAndS3(updateDto.getFiles(), list, oldAnnouncement);
        }
    }
    private void fileUploadDbAndS3(List<MultipartFile> files, List<FileDetailDto> list, Announcement announcement) {
        //파일 데이터 db 저장
        IntStream.range(0, list.size())
                .forEach(index ->fileRepository.save(
                        File.ofAnnouncement(announcement, list.get(index))
                ));
        //파일 데이터 s3에 업로드
        List<String> keys = list.stream().map(FileDetailDto::getKey).toList();
        s3Uploader.uploadFiles(keys, files);
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



    // 공지 상세 데이터 가져오기
    public void getAnnouncementDetail(Long studyId, Long announcementId) {
        // TODO: 공지 상세 조회
    }






    //인터셉터 및 Aop 반영 시 수정 필요
    private StudyMember memberValidation(Long studyId, Long userId) {
        if(studyId == null || userId == null) {throw new BusinessException(CommonErrorCode.INVALID_REQUEST);}
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId).
                orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
    }



}
