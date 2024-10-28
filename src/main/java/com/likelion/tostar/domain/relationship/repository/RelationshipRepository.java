package com.likelion.tostar.domain.relationship.repository;

import com.likelion.tostar.domain.relationship.entity.Relationship;
import com.likelion.tostar.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    // 서비스 계층에서 id 크기순으로 정렬 후 호출
    @Query("SELECT r FROM Relationship r WHERE r.user1 = :user1 AND r.user2 = :user2")
    Optional<Relationship> findByUsers(User user1, User user2);
}
