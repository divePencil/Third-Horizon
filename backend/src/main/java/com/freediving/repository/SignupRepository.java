package com.freediving.repository;

import com.freediving.domain.Signup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupRepository extends JpaRepository<Signup, Long> {

    List<Signup> findByActivityIdOrderByCreatedAtAsc(Long activityId);

    void deleteByActivityId(Long activityId);
}
