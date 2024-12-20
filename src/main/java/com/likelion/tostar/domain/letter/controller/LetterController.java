package com.likelion.tostar.domain.letter.controller;

import com.likelion.tostar.domain.letter.dto.LetterPostRequestDto;
import com.likelion.tostar.domain.letter.service.LetterService;
import com.likelion.tostar.global.jwt.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/letters")
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    /**
     * 편지 전송
     */
    @PostMapping()
    public ResponseEntity<?> post(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody LetterPostRequestDto letterPostRequestDto) {
        return letterService.post(customUserDetails.getId(), letterPostRequestDto);
    }

    /**
     * 편지 목록 전체 조회
     */
    @GetMapping()
    public ResponseEntity<?> searchList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(value = "page", defaultValue = "0")int page,
            @RequestParam(value = "size", defaultValue = "4")int size) {
        return letterService.searchList(customUserDetails.getId(), page, size);
    }

    /**
     * 편지 상세 조회
     */
    @GetMapping("/{letterId}")
    public ResponseEntity<?> searchDetail(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("letterId") Long letterId){
        return letterService.searchDetails(customUserDetails.getId(), letterId);
    }
}
