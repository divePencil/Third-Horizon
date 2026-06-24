package com.freediving.repository;

import com.freediving.domain.ActivityExpenseShare;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityExpenseShareRepository extends JpaRepository<ActivityExpenseShare, Long> {
    List<ActivityExpenseShare> findByExpenseId(Long expenseId);

    List<ActivityExpenseShare> findByExpenseIdIn(List<Long> expenseIds);

    void deleteByExpenseId(Long expenseId);
}
