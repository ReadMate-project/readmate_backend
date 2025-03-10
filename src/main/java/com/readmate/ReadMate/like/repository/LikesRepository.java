package com.readmate.ReadMate.like.repository;

import com.readmate.ReadMate.like.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    Optional<Likes> findByBoardIdAndUserId(Long boardId, Long userId);

    int countByBoardIdAndLikedTrue(Long boardId);

    List<Likes> findByBoardIdAndLikedTrue(Long boardId);
}
