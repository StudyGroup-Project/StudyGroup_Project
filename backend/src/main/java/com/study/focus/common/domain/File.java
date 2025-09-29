package com.study.focus.common.domain;

import com.study.focus.announcement.domain.Announcement;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.resource.domain.Resource;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Entity
@Check(
        constraints = """
   (
     (CASE WHEN resource_id     IS NOT NULL THEN 1 ELSE 0 END) +
     (CASE WHEN announcement_id IS NOT NULL THEN 1 ELSE 0 END) +
     (CASE WHEN assignment_id   IS NOT NULL THEN 1 ELSE 0 END) +
     (CASE WHEN submission_id   IS NOT NULL THEN 1 ELSE 0 END)
   ) <= 1
   """
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, unique = true, length = 512)
    private String fileKey;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;

    public void delete() {
        isDeleted = true;
    }

    public void deleteAnnouncementFile() {
        isDeleted = true;
        announcement = null;
    }

    public static File ofResource(Resource resource,
                                  FileDetailDto fileDetail) {
        File f = base(fileDetail);
        f.resource = resource;
        return f;
    }

    public static File ofAnnouncement(Announcement announcement,
                                      FileDetailDto fileDetail) {
        File f = base(fileDetail);
        f.announcement = announcement;
        return f;
    }

    public static File ofAssignment(Assignment assignment,
                                    FileDetailDto fileDetail) {
        File f = base(fileDetail);
        f.assignment = assignment;
        return f;
    }

    public static File ofSubmission(Submission submission,
                                    FileDetailDto fileDetail) {
        File f = base(fileDetail);
        f.submission = submission;
        return f;
    }

    /**
     * UserProfile 프로필 이미지용 파일 생성
     */
    public static File ofProfileImage(FileDetailDto fileDetail) {
        return base(fileDetail);
    }

    // 공통 생성 로직
    private static File base( FileDetailDto fileDetail) {
        File f = new File();
        f.fileName = fileDetail.getOriginalFileName();
        f.fileKey = fileDetail.getKey();
        f.mimeType = fileDetail.getContentType();
        f.fileSize = fileDetail.getFileSize();
        return f;
    }
}
