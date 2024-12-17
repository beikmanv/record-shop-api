package com.northcoders.recordapi.dto;

import com.northcoders.recordapi.model.Album;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResponseDTO {

    private Long albumId;
    private Long artistId;
    private String artistName;
    private String title;
    private String genre;
    private int releaseYear;
    private int stock;
    private double price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;  // To store messages like error or success messages
    private String imageResource;  // New field for image resource name

    // Constructor to map from Album to AlbumResponseDTO
    public AlbumResponseDTO(Album album) {
        this.albumId = album.getAlbumId();
        this.artistId = album.getArtist() != null ? album.getArtist().getArtistId() : null;
        this.artistName = album.getArtist() != null ? album.getArtist().getArtistName() : null;
        this.title = album.getTitle();
        this.genre = album.getGenre().name();
        this.releaseYear = album.getReleaseYear();
        this.stock = album.getStock();
        this.price = album.getPrice();
        this.createdAt = album.getCreatedAt();
        this.updatedAt = album.getUpdatedAt();
        this.imageResource = mapImageResource(album); // Set image resource here
    }

    // Helper method to determine the image resource
    private String mapImageResource(Album album) {
        switch (album.getGenre()) {
            case ROCK:
                return "album1";  // Return the key, not the drawable path
            case POP:
                return "album2";
            case JAZZ:
                return "album3";
            case HIP_HOP:
                return "album4";
            case ELECTRONIC:
                return "album5";
            case CLASSICAL:
                return "album6";
            case RELIGIOUS:
                return "album7";
            case REGGAE:
                return "album8";
            case COUNTRY:
                return "album9";
            default:
                return "album10";  // Default album if genre is not found
        }
    }

}
