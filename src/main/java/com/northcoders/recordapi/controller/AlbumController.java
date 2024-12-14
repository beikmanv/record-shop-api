package com.northcoders.recordapi.controller;

import com.northcoders.recordapi.dto.AlbumResponseDTO;
import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Artist;
import com.northcoders.recordapi.repository.AlbumRepository;
import com.northcoders.recordapi.repository.ArtistRepository;
import com.northcoders.recordapi.service.AlbumService;
import com.northcoders.recordapi.service.AlbumServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    ArtistRepository artistRepository;

    // Get all albums
    @GetMapping("/album")
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.getAllAlbums();
        return ResponseEntity.ok(albums);
    }

    // Get album by ID
    @GetMapping("/album/{id}")
    public ResponseEntity<AlbumResponseDTO> getAlbumById(@PathVariable Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        AlbumResponseDTO response = new AlbumResponseDTO(album);
        return ResponseEntity.ok(response);
    }


    // Create a new album
    @PostMapping("/album")
    public ResponseEntity<Album> createAlbum(@RequestBody Album album) {
        try {
            Album savedAlbum = albumService.createAlbum(album);
            return new ResponseEntity<>(savedAlbum, HttpStatus.CREATED);
        } catch (AlbumAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Include detailed error message here if needed
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an album
    @PutMapping("/album/{id}")
    public ResponseEntity<AlbumResponseDTO> updateAlbum(@PathVariable Long id, @RequestBody Album album) {
        // Retrieve the existing album by ID
        Album existingAlbum = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        // Fetch the artist based on the artistId from the album request
        Artist artist = album.getArtist() != null && album.getArtist().getArtistId() != null
                ? artistRepository.findById(album.getArtist().getArtistId())
                .orElseThrow(() -> new RuntimeException("Artist not found"))
                : existingAlbum.getArtist(); // If no artist ID is provided, keep the existing artist
        // Update the album's fields
        existingAlbum.setTitle(album.getTitle());
        existingAlbum.setGenre(album.getGenre());
        existingAlbum.setReleaseYear(album.getReleaseYear());
        existingAlbum.setStock(album.getStock());
        existingAlbum.setPrice(album.getPrice());
        existingAlbum.setArtist(artist);  // Set the artist (could be new or existing)
        existingAlbum.setUpdatedAt(LocalDateTime.now());
        // Save the updated album
        Album updatedAlbum = albumRepository.save(existingAlbum);
        // Return the updated album in the response with all necessary details
        AlbumResponseDTO responseDTO = new AlbumResponseDTO(updatedAlbum);
        return ResponseEntity.ok(responseDTO);
    }


    // Delete an album by ID
    @DeleteMapping("album/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id) {
        Optional<Album> deletedAlbum = albumService.deleteAlbum(id);
        if (deletedAlbum.isPresent()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // Status 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();   // Status 404: Not Found
        }
    }

}
