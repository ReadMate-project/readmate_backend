package com.readmate.ReadMate.image.service;
import com.readmate.ReadMate.board.entity.Board;
import com.readmate.ReadMate.board.repository.BoardRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.image.entity.Image;
import com.readmate.ReadMate.image.repository.ImageRepository;
import com.readmate.ReadMate.image.utils.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Uploader s3Uploader;
    private final ImageRepository imageRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public List<Image> uploadImages(Long boardId, List<MultipartFile> images) throws IOException {

        boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER));

        List<Image> savedImages = images.stream().map(image -> {
            try {
                String imageUrl = s3Uploader.uploadFiles(image, "uploads");
                Image newImage = new Image();
                newImage.setBoardId(boardId);
                newImage.setImageUrl(imageUrl);
                return imageRepository.save(newImage);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
        }).collect(Collectors.toList());

        return savedImages;
    }


    @Transactional
    public void deleteImagesByBoardId(Long boardId) { //게시글 삭제 시 해당 이미지들도 삭제
        List<Image> images = imageRepository.findByBoardId(boardId);

        // 각 이미지를 S3와 데이터베이스에서 삭제
        for (Image image : images) {
            // S3에서 이미지 삭제
            String fileName = extractFileName(image.getImageUrl());
            s3Uploader.deleteFile("uploads", fileName);

            // DB에서 이미지 삭제
            imageRepository.delete(image);
        }
    }

    private String extractFileName(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }



    @Transactional
    public List<Image> updateImages(Long boardId, List<MultipartFile> newImages) throws IOException {
        List<Image> existingImages = imageRepository.findByBoardId(boardId);

        for (Image image : existingImages) {
            // S3에서 이미지 삭제
            String fileName = extractFileName(image.getImageUrl());
            s3Uploader.deleteFile("uploads", fileName);

            // DB에서 이미지 삭제
            imageRepository.delete(image);
        }

        List<Image> savedImages = new ArrayList<>();
        for (MultipartFile newImage : newImages) {
            String imageUrl = s3Uploader.uploadFiles(newImage, "uploads");
            Image newImageEntity = new Image();
            newImageEntity.setBoardId(boardId);
            newImageEntity.setImageUrl(imageUrl);
            savedImages.add(imageRepository.save(newImageEntity));
        }

        return savedImages;
    }
}
