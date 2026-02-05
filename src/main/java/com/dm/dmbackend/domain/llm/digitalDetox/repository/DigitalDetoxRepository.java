package com.dm.dmbackend.domain.llm.digitalDetox.repository;

import com.dm.dmbackend.domain.llm.digitalDetox.entity.DigitalDetox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DigitalDetoxRepository extends JpaRepository<DigitalDetox, Long> {
}
