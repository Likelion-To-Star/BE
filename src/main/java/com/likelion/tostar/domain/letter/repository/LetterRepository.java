package com.likelion.tostar.domain.letter.repository;

import com.likelion.tostar.domain.letter.entity.Letter;
import com.likelion.tostar.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;


public interface LetterRepository extends JpaRepository<Letter, Long> {
    // 해당 회원이 쓴 편지 페이징 조회(최신 데이터부터 오래된 순으로)
    Page<Letter> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}

