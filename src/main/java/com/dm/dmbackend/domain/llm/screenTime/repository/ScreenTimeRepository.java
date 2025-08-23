package com.dm.dmbackend.domain.llm.screenTime.repository;

import com.dm.dmbackend.domain.llm.screenTime.entity.ScreenTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScreenTimeRepository extends JpaRepository<ScreenTime, Long> {
}
