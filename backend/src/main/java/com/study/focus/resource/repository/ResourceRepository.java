package com.study.focus.resource.repository;

import com.study.focus.resource.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findAllByStudyId(Long studyId);

    void deleteAllByStudy_Id(Long studyId);
}
