package com.freediving.repository;

import com.freediving.domain.ActivityMember;
import com.freediving.domain.ActivityMemberStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityMemberRepository extends JpaRepository<ActivityMember, Long> {
    List<ActivityMember> findByActivityIdAndStatusOrderByCreatedAtAsc(Long activityId, ActivityMemberStatus status);

    List<ActivityMember> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ActivityMemberStatus status);

    Optional<ActivityMember> findByActivityIdAndUserId(Long activityId, Long userId);
}
