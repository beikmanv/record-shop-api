package com.northcoders.recordapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "artist_id", "releaseYear"})
})
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long albumId;

    @Column(nullable = false, length = 255)
    @NotNull(message = "Title is required")
    @NotBlank(message = "Title must not be blank")
    private String title;

    @ManyToOne(fetch = FetchType.EAGER) // or LAZY or EAGER based on your need
    @JoinColumn(name = "artist_id", nullable = false)
    @NotNull(message = "Artist is required")
    @JsonBackReference  // Prevent infinite recursion by not serializing this side
    private Artist artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Genre is required")
    @GenreValid(message = "Genre can be only: BLUES, CLASSICAL, " +
            "COUNTRY, ELECTRONIC, FOLK, HIP_HOP, JAZZ, " +
            "REGGAE, RELIGIOUS, ROCK, SOUNDTRACK, LATIN, POP, STAGE_AND_SCREEN")
    @JsonDeserialize(using = GenreDeserializer.class)
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
    public Album(String title, Artist artist, Genre genre, int releaseYear, int stock, double price) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.stock = stock;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
