package com.freediving.repository;

import com.freediving.domain.Activity;
import com.freediving.domain.ActivityStatus;
import com.freediving.domain.Visibility;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByVisibilityAndStatusInOrderByStartTimeAsc(Visibility visibility, Collection<ActivityStatus> statuses);
}
