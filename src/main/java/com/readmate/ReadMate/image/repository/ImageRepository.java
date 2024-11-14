package com.readmate.ReadMate.image.repository;

import com.readmate.ReadMate.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository  extends JpaRepository<Image, Long> {
    List<Image> findByBoardId(Long boardId);

}
