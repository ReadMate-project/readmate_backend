package com.readmate.ReadMate.image.dto;

import com.readmate.ReadMate.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private Long imageId;
    private Long boardId;
    private String imageUrl;

    public ImageResponse(Image image) {
        this.imageId = image.getImageId();
        this.boardId = image.getBoardId();
        this.imageUrl = image.getImageUrl();
    }

}
