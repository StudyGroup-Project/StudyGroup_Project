package com.study.focus.common.repository;

import com.study.focus.common.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    //True인 모든 리스트 조회
    List<File> findAllByIsDeletedTrue();

    List<File>findAllByAnnouncement_Id(Long announcement_id);



}
