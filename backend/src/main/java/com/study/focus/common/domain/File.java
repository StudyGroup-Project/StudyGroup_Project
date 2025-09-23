package com.study.focus.common.domain;

import com.study.focus.announcement.domain.Announcement;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.resource.domain.Resource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Check(
        constraints = """
   (
     (CASE WHEN resource_id     IS NOT NULL THEN 1 ELSE 0 END) +
     (CASE WHEN announcement_id IS NOT NULL THEN 1 ELSE 0 END) +
     (CASE WHEN assignment_id   IS NOT NULL THEN 1 ELSE 0 END) +
     (CASE WHEN submission_id   IS NOT NULL THEN 1 ELSE 0 END)
   ) = 1
   """
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder//스케줄러 테스트를 위한 builder추가
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

    public static File ofResource(Resource resource,
                                  String fileName, String fileKey, String mimeType, long fileSize) {
        File f = base(fileName, fileKey, mimeType, fileSize);
        f.resource = resource;
        return f;
    }

    public static File ofAnnouncement(Announcement announcement,
                                      String fileName, String fileKey, String mimeType, long fileSize) {
        File f = base(fileName, fileKey, mimeType, fileSize);
        f.announcement = announcement;
        return f;
    }

    public static File ofAssignment(Assignment assignment,
                                    String fileName, String fileKey, String mimeType, long fileSize) {
        File f = base(fileName, fileKey, mimeType, fileSize);
        f.assignment = assignment;
        return f;
    }

    public static File ofSubmission(Submission submission,
                                    String fileName, String fileKey, String mimeType, long fileSize) {
        File f = base(fileName, fileKey, mimeType, fileSize);
        f.submission = submission;
        return f;
    }

    // 공통 생성 로직
    private static File base(String fileName, String fileKey, String mimeType, long fileSize) {
        File f = new File();
        f.fileName = fileName;
        f.fileKey = fileKey;
        f.mimeType = mimeType;
        f.fileSize = fileSize;
        return f;
    }
}
