package com.freediving.repository;

import com.freediving.domain.ActivityOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityOptionRepository extends JpaRepository<ActivityOption, Long> {

    List<ActivityOption> findAllByOrderByCategoryAscActivityTypeAscSortOrderAscIdAsc();

    List<ActivityOption> findByVisibleTrueOrderByCategoryAscActivityTypeAscSortOrderAscIdAsc();
}
