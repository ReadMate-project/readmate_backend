package com.readmate.ReadMate.image.controller;

import com.readmate.ReadMate.image.dto.ImageResponse;
import com.readmate.ReadMate.image.entity.Image;
import com.readmate.ReadMate.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/post/images")
@RequiredArgsConstructor
@Tag(name = "게시글 이미지 API", description = "게시글 이미지 업로드 API")
public class ImageController {
    private final ImageService imageService;

    @Operation(summary = "게시글 이미지 업로드", description = "게시글 이미지들 업로드")
    @PostMapping("/{postId}")
    public ResponseEntity<List<ImageResponse>> uploadImages(
            @PathVariable("postId") Long postId,
            @RequestPart("images") List<MultipartFile> images) throws IOException {

        List<Image> savedImages = imageService.uploadImages(postId, images);
        List<ImageResponse> imageDtos = savedImages.stream()
                .map(ImageResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(imageDtos);
    }


    @Operation(summary = "게시글 이미지 수정", description = "기존 게시글 이미지를 새로운 이미지로 수정")
    @PutMapping("/images/{imageId}")
    public ResponseEntity<Image> updateImage(
            @PathVariable("imageId") Long imageId,
            @RequestPart MultipartFile newImage) throws IOException {

        Image updatedImage = imageService.updateImage(imageId, newImage);
        return ResponseEntity.ok(updatedImage);
    }

    @Operation(summary = "게시글 이미지 삭제", description = "게시글 이미지 삭제")
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable("imageId") Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

}
