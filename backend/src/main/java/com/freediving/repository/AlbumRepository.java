package com.freediving.repository;

import com.freediving.domain.Album;
import com.freediving.domain.Visibility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByVisibilityOrderByActivityDateDesc(Visibility visibility);

    List<Album> findByActivityId(Long activityId);
}
