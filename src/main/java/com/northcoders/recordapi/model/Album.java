package com.northcoders.recordapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 255 is used because it's the largest number of characters that can be counted with an 8-bit number
    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Column(nullable = false)
    private int releaseYear;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}