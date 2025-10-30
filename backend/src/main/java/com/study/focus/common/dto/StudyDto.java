package com.study.focus.common.dto;

import com.study.focus.common.domain.Category;
import com.study.focus.common.util.CategoryListConverter;
import com.study.focus.study.domain.StudyProfile;
import lombok.Getter;
// import lombok.AllArgsConstructor; // <- 제거! (수동으로 생성자를 만들어야 함)

import java.util.List;

@Getter
// @AllArgsConstructor // QueryDSL용 생성자와 of()용 생성자를 분리하기 위해 제거합니다.
public class StudyDto {
    private Long id;
    private String title;
    private int maxMemberCount;
    private int memberCount;
    private long bookmarkCount;
    private String bio;
    private List<Category> category; // 최종 필드는 List<Category> 유지
    private long trustScore;
    private boolean bookmarked;

    /**
     * StudyRepositoryImpl의 Projections.constructor()에서 사용할 생성자
     * QueryDSL이 DB에서 가져온 String(e.g., "IT_BUSINESS")을 categoryString 파라미터로 받습니다.
     */
    public StudyDto(Long id,
                    String title,
                    int maxMemberCount,
                    int memberCount,
                    long bookmarkCount,
                    String bio,
                    String categoryString, // <--- QueryDSL은 여기에 String을 주입
                    long trustScore,
                    boolean bookmarked) {
        this.id = id;
        this.title = title;
        this.maxMemberCount = maxMemberCount;
        this.memberCount = memberCount;
        this.bookmarkCount = bookmarkCount;
        this.bio = bio;

        // --- 여기서 컨버터를 수동으로 호출하여 변환 ---
        CategoryListConverter converter = new CategoryListConverter();
        this.category = converter.convertToEntityAttribute(categoryString); // String -> List<Category>
        // --- ---

        this.trustScore = trustScore;
        this.bookmarked = bookmarked;
    }

    /**
     * of() 메서드에서 사용할 생성자 (기존 로직 유지)
     * 이 생성자는 엔티티의 List<Category>를 직접 받습니다.
     */
    public StudyDto(Long id,
                    String title,
                    int maxMemberCount,
                    int memberCount,
                    long bookmarkCount,
                    String bio,
                    List<Category> category, // <--- of() 메서드는 여기에 List를 주입
                    long trustScore,
                    boolean bookmarked) {
        this.id = id;
        this.title = title;
        this.maxMemberCount = maxMemberCount;
        this.memberCount = memberCount;
        this.bookmarkCount = bookmarkCount;
        this.bio = bio;
        this.category = category; // <--- 변환 필요 없음
        this.trustScore = trustScore;
        this.bookmarked = bookmarked;
    }


    /**
     * 엔티티로부터 DTO를 생성 (JPA가 엔티티를 조회한 경우 사용)
     * 이 메서드는 StudyProfile 엔티티가 이미 로드되었음을 전제로 합니다.
     * (이때는 AttributeConverter가 이미 동작하여 profile.getCategory()가 List<Category>를 반환)
     */
    public static StudyDto of(StudyProfile profile,
                              int memberCount,
                              long bookmarkCount,
                              long trustScore,
                              boolean bookmarked) {
        // 이 new StudyDto() 호출은 위쪽의 'of() 메서드용 생성자'를 사용합니다.
        return new StudyDto(
                profile.getStudy().getId(),
                profile.getTitle(),
                profile.getStudy().getMaxMemberCount(),
                memberCount,
                bookmarkCount,
                profile.getBio(),
                profile.getCategory(), // profile.getCategory()는 List<Category> 타입
                trustScore,
                bookmarked
        );
    }
}
