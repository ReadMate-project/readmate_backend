package com.readmate.ReadMate.book.service;

import com.readmate.ReadMate.book.dto.req.BookRequest;
import com.readmate.ReadMate.book.dto.res.AladinBook;
import com.readmate.ReadMate.book.dto.res.AladinBookResponse;
import com.readmate.ReadMate.book.dto.res.BookResponse;
import com.readmate.ReadMate.book.dto.res.SubInfo;
import com.readmate.ReadMate.book.entity.Book;
import com.readmate.ReadMate.book.entity.BookCategory;
import com.readmate.ReadMate.book.repository.BookRepository;
import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import com.readmate.ReadMate.common.genre.Genre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {
    @Value("${aladin.url}")
    private String aladinUrl;
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 책 저장
     */
    public String saveBook(BookRequest bookRequest){
        Book book = Book.builder()
                .description(bookRequest.getDescription())
                .genre(bookRequest.getGenre())
                .isbn13(bookRequest.getIsbn13())
                .publisher(bookRequest.getPublisher())
                .title(bookRequest.getTitle())
                .totalPages(bookRequest.getTotalPages())
                .publisher(bookRequest.getPublisher())
                .bookCover(bookRequest.getBookCover())
                .build();
        bookRepository.save(book);
        return "책이 저장되었습니다";
    }
    /**
     * DB에 저장된 Book ID로 조회하기
     */
    public BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOOK));
        return BookResponse.builder()
                .bookId(book.getBookId())
                .description(book.getDescription())
                .genre(book.getGenre())
                .isbn13(book.getIsbn13())
                .publisher(book.getPublisher())
                .totalPages(book.getTotalPages())
                .title(book.getTitle())
                .bookCover(book.getBookCover())
                .author(book.getAuthor())
                .build();


    }
    /**
     * Book ISBN13으로 조회하기
     */
    public BookResponse getBookByIsbn(Long isbn13) {
        Book book = bookRepository.findByIsbn13(isbn13)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOOK));

        return BookResponse.builder()
                .bookId(book.getBookId())
                .description(book.getDescription())
                .genre(book.getGenre())
                .isbn13(book.getIsbn13())
                .publisher(book.getPublisher())
                .totalPages(book.getTotalPages())
                .title(book.getTitle())
                .bookCover(book.getBookCover())
                .author(book.getAuthor())
                .build();
    }

    /**
     * ISBN13 으로 책 저장하기
     * @param isbn13
     * @return String
     */
    public String saveBookByIsbn(Long isbn13) {
        String url = String.format("%s&ItemId=%s", aladinUrl, isbn13);

        ResponseEntity<AladinBookResponse> response = restTemplate.getForEntity(url, AladinBookResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            AladinBookResponse bookResponse = response.getBody();
            System.out.println("bookResponse.getItem() = " + bookResponse.getItem());

            // Check if the book response is valid
            if (bookResponse == null || bookResponse.getItem() == null || bookResponse.getItem().isEmpty()) {
                log.error("Book not found or empty response for ISBN: {}", isbn13);
                throw new CustomException(ErrorCode.BOOK_NOT_FOUND);
            }

            // Extract book information from the first item
            AladinBook bookInfo = bookResponse.getItem().get(0);
            SubInfo subInfo = bookInfo.getSubInfo();
            Long totalPages = (subInfo != null) ? subInfo.getItemPage() : null;

            // 데이터 가져오기
            String title = bookInfo.getTitle();
            String author = bookInfo.getAuthor();
            String description = bookInfo.getDescription();
            String isbn = bookInfo.getIsbn();
            String isbn13Str = bookInfo.getIsbn13();
            String publisher = bookInfo.getPublisher();
            String coverUrl = bookInfo.getCover();
            String categoryName = bookInfo.getCategoryName();

            String[] categories = categoryName.split(">");
            String genreName = categories.length > 1 ? categories[1] : null;


            // 필요한 로직 수행 후 저장
            Book book = Book.builder()
                    .title(title)
                    .author(author)
                    .description(description)
                    .isbn13(Long.parseLong(isbn13Str))
                    .publisher(publisher)
                    .totalPages(totalPages)
                    .bookCover(coverUrl)
                    .build();

            bookRepository.save(book);

            return "책이 저장되었습니다";
        } else {
            log.error("Failed to retrieve book from Aladin API: {}", response.getStatusCode());
            throw new CustomException(ErrorCode.API_CALL_FAILED);
        }
    }


    }

