package com.likelion.tostar.domain.articles.service;

import com.likelion.tostar.domain.articles.dto.ArticleCreateModifyRequestDto;
import com.likelion.tostar.global.jwt.dto.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArticleService {
    // 추억 등록
    ResponseEntity<?> createArticle(Long userId, ArticleCreateModifyRequestDto articleCreateModifyRequestDto, List<MultipartFile> images);

    // 추억 수정
    ResponseEntity<?> modifyArticle(Long articleId, Long userId, ArticleCreateModifyRequestDto articleCreateModifyRequestDto, List<MultipartFile> images);

    // 추억 삭제
    ResponseEntity<?> deleteArticle(Long articleId, Long userId);

    // 특정 사용자의 추억 조회
    ResponseEntity<?> getArticlesByUserId(CustomUserDetails customUserDetails, Long userId, int page, int size);

}
