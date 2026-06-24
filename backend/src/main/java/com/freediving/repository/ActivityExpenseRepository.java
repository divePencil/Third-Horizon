package com.freediving.repository;

import com.freediving.domain.ActivityExpense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityExpenseRepository extends JpaRepository<ActivityExpense, Long> {
    List<ActivityExpense> findByActivityIdOrderByCreatedAtAsc(Long activityId);
}
