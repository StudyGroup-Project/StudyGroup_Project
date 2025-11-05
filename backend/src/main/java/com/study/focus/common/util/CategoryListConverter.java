package com.study.focus.common.util;

import com.study.focus.common.domain.Category;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * List<Category>를 DB의 단일 String 컬럼에 '_'로 구분하여 저장하기 위한 JPA AttributeConverter
 * * @Converter(autoApply = false) // false가 기본값. 필요한 곳에서 @Convert로 명시적 사용
 */
@Slf4j
@Converter
public class CategoryListConverter implements AttributeConverter<List<Category>, String> {

    private static final String DELIMITER = "_";

    /**
     * List<Category> -> DB String (e.g., "IT_BUSINESS")
     * * @param attribute 엔티티의 List<Category> 필드
     * @return DB에 저장될 단일 String (e.g., "IT_BUSINESS_DESIGN")
     */
    @Override
    public String convertToDatabaseColumn(List<Category> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null; // DB에 null로 저장
        }
        // Category enum의 이름(name)을 가져와 DELIMITER로 연결합니다.
        return attribute.stream()
                .map(Category::name)
                .collect(Collectors.joining(DELIMITER));
    }

    /**
     * DB String -> List<Category> (e.g., "IT_BUSINESS" -> List.of(Category.IT, Category.BUSINESS))
     *
     * @param dbData DB에서 읽어온 단일 String 값
     * @return 엔티티의 List<Category> 필드
     */
    @Override
    public List<Category> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Collections.emptyList(); // null이나 빈 문자열이면 빈 리스트 반환
        }

        // DELIMITER로 문자열을 분리하고, 각각을 Category enum으로 변환합니다.
        return Arrays.stream(dbData.split(DELIMITER))
                .map(name -> {
                    try {
                        // 문자열을 Category enum으로 변환
                        return Category.valueOf(name.trim());
                    } catch (IllegalArgumentException e) {
                        // DB에 저장된 값이 Category enum에 존재하지 않는 경우
                        // (e.g. enum에서 삭제되었거나, 데이터가 잘못 들어간 경우)
                        // 여기서는 null을 반환하고 아래에서 필터링합니다.
                        // TODO: 로깅 추가 권장
                        log.error("Invalid category value in DB: " + name);
                        return null;
                    }
                })
                .filter(Objects::nonNull) // null이 아닌(유효한) 값만 필터링
                .collect(Collectors.toList());
    }
}