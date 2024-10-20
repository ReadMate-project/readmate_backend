package com.readmate.ReadMate.comment.dto;

import com.readmate.ReadMate.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResWithPageInfo {

    private List<Comment> comments;
    private PageInfo pageInfo;
}
