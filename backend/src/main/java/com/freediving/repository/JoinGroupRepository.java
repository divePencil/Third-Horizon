package com.freediving.repository;

import com.freediving.domain.JoinGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinGroupRepository extends JpaRepository<JoinGroup, Long> {

    List<JoinGroup> findAllByOrderBySortOrderAscIdAsc();

    List<JoinGroup> findByVisibleTrueOrderBySortOrderAscIdAsc();
}
