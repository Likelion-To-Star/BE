package com.likelion.tostar.domain.comment.service;

import com.likelion.tostar.domain.articles.entity.Article;
import com.likelion.tostar.domain.articles.repository.ArticleRepository;
import com.likelion.tostar.domain.comment.converter.CommentConverter;
import com.likelion.tostar.domain.comment.dto.CommentRequestDTO;
import com.likelion.tostar.domain.comment.dto.CommentResponseDTO;
import com.likelion.tostar.domain.comment.entity.Comment;
import com.likelion.tostar.domain.comment.repository.CommentRepository;
import com.likelion.tostar.global.enums.statuscode.ErrorStatus;
import com.likelion.tostar.global.exception.GeneralException;
import com.likelion.tostar.global.response.ApiResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * GET 요청은 여기에 작성
 */
@Service
@RequiredArgsConstructor
public class CommentQueryServiceImpl implements CommentQueryService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final CommentConverter commentConverter;


    @Override
    public ResponseEntity<?> getCommentsByArticleId(Long articleId) {
        // 1. 게시글 조회
        Article article = findArticleById(articleId);
        // 2. 게시글 관련 댓글 조회
        List<Comment> comments = commentRepository.findByArticleOrderByCreatedAtDesc(article);
        // 3. 반환 DTO 작성
        ArrayList<CommentResponseDTO> responseDTOArrayList = new ArrayList<>();
        for (Comment comment : comments) {
            responseDTOArrayList.add(commentConverter.toCommentResponseDTO(comment));
        }
        return ResponseEntity.ok(ApiResponse.onSuccess(responseDTOArrayList));
    }

    public Article findArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._ARTICLE_NOT_FOUND));
    }
}