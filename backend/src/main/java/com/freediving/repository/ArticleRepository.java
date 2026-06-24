package com.freediving.repository;

import com.freediving.domain.Article;
import com.freediving.domain.Visibility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByVisibilityAndPublishedTrueOrderByUpdatedAtDesc(Visibility visibility);

    Optional<Article> findBySlugAndPublishedTrue(String slug);
}
