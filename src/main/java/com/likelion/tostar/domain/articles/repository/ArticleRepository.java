package com.likelion.tostar.domain.articles.repository;

import com.likelion.tostar.domain.articles.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article,Long> {
    // 회원 ID로 게시글 조회
    @Query("SELECT a FROM Article a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    Page<Article> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.user.id NOT IN :userIds ORDER BY a.createdAt DESC")
    Page<Article> findArticlesExcludingUsers(@Param("userIds") List<Long> userIds, Pageable pageable);
}
