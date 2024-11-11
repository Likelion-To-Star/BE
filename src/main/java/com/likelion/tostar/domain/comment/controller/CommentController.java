package com.likelion.tostar.domain.comment.controller;

import com.likelion.tostar.domain.comment.dto.CommentDTO;
import com.likelion.tostar.domain.comment.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 특정 게시글에 대한 댓글들을 최신순으로 조회
     */
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long articleId) {
        List<CommentDTO> comments = commentService.getCommentsByArticleId(articleId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long articleId,
            @RequestBody CommentDTO commentDTO {
        CommentDTO createdComment = commentService.createComment(articleId, commentDTO);
        return ResponseEntity.status(201).body(createdComment);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @RequestBody CommentDTO commentDTO) {
        CommentDTO updatedComment = commentService.updateComment(articleId, commentId, commentDTO);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        commentService.deleteComment(articleId, commentId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }
}