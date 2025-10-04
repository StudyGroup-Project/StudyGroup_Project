package com.study.focus.resource.repository;

import com.study.focus.resource.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findAllByStudy_Id(Long studyId);

    Optional<Resource> findByIdAndStudyId(Long id, Long study_Id);

    Optional<Resource>findByIdAndAuthorId(Long id, Long author_Id);
}
