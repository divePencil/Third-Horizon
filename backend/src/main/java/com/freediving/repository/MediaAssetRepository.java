package com.freediving.repository;

import com.freediving.domain.MediaAsset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    List<MediaAsset> findByAlbumIdOrderBySortOrderAscIdAsc(Long albumId);

    void deleteByAlbumId(Long albumId);
}
