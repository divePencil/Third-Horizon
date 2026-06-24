package com.freediving.repository;

import com.freediving.domain.ActivitySettlementItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivitySettlementItemRepository extends JpaRepository<ActivitySettlementItem, Long> {
    List<ActivitySettlementItem> findBySettlementIdOrderByIdAsc(Long settlementId);

    void deleteBySettlementId(Long settlementId);
}
