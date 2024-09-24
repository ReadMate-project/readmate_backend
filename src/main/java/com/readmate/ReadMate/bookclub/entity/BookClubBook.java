package com.readmate.ReadMate.bookclub.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
public class BookClubBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_club_id")
    private BookClub bookClub;

    @Builder.Default
    private boolean isActive = true;

    @Builder
    public BookClubBook(Long id, Long isbn, BookClub bookClub, boolean isActive) {
        this.id = id;
        this.isbn = isbn;
        this.bookClub = bookClub;
        this.isActive = isActive;
    }
    public void update() {
        this.isActive = false;
    }
}