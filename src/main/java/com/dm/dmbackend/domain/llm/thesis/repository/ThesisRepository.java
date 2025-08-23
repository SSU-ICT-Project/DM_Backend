package com.dm.dmbackend.domain.llm.thesis.repository;

import com.dm.dmbackend.domain.llm.thesis.entity.Thesis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThesisRepository extends JpaRepository<Thesis, Long> {
    Page<Thesis> findByMemberId(Long memberId, Pageable pageable);
    List<Thesis> findAllByIdIn(List<Long> ids);
}
