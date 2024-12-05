package com.northcoders.recordapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotNull(message = "Title is required")
    private String title;

    @Column(nullable = false, length = 255)
    @NotNull(message = "Artist is required")
    private String artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Genre is required")
    private Genre genre;

    @Column(nullable = false)
    @NotNull(message = "Release year is required")
    private int releaseYear;

    @Column(nullable = false)
    @NotNull(message = "Stock is required")
    private int stock;

    @Column(nullable = false)
    @NotNull(message = "Price is required")
    private double price;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Manually defined constructor (excluding 'id')
    public Album(String title, String artist, Genre genre, int releaseYear, int stock, double price) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.stock = stock;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
