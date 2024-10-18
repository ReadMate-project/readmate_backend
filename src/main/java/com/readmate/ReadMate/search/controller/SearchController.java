package com.readmate.ReadMate.search.controller;

import com.readmate.ReadMate.common.message.BasicResponse;
import com.readmate.ReadMate.search.dto.SearchResponse;
import com.readmate.ReadMate.search.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "검색", description = "게시글+북클럽 검색 API")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<BasicResponse<SearchResponse>> search(
            @RequestParam(name = "keyword") String keyword,
            Authentication authentication) {

        System.out.println("Keyword: " + keyword);

        UserDetails userDetails = (authentication != null) ? (UserDetails) authentication.getPrincipal() : null;

        SearchResponse searchResult = searchService.searchAll(keyword, userDetails);
        return ResponseEntity.ok(BasicResponse.ofSuccess(searchResult));
    }
}
