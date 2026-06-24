package com.freediving.repository;

import com.freediving.domain.ActivitySettlement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivitySettlementRepository extends JpaRepository<ActivitySettlement, Long> {
    List<ActivitySettlement> findByActivityIdOrderByCreatedAtDesc(Long activityId);
}
