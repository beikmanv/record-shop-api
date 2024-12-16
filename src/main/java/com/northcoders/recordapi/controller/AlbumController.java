package com.northcoders.recordapi.controller;

import com.northcoders.recordapi.dto.AlbumRequestDTO;
import com.northcoders.recordapi.dto.AlbumResponseDTO;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Artist;
import com.northcoders.recordapi.model.Genre;
import com.northcoders.recordapi.repository.AlbumRepository;
import com.northcoders.recordapi.repository.ArtistRepository;
import com.northcoders.recordapi.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.northcoders.recordapi.model.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class AlbumController {

    private static final Logger log = LoggerFactory.getLogger(AlbumController.class);

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

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
    @PostMapping("/album")  // Ensure this method is mapped to a POST request for album creation
    public ResponseEntity<AlbumResponseDTO> createAlbum(@RequestBody @Valid AlbumRequestDTO albumRequest) {
        try {
            // Check if the artist exists in the database
            Optional<Artist> optionalArtist = artistRepository.findByArtistName(albumRequest.getArtistName());

            Artist artist;
            if (optionalArtist.isPresent()) {
                // If the artist exists, use the existing artist
                artist = optionalArtist.get();
            } else {
                // If the artist doesn't exist, create a new artist
                artist = new Artist();
                artist.setArtistName(albumRequest.getArtistName());
                artist = artistRepository.save(artist); // Save the new artist
                log.warn("Artist '{}' was not found, a new artist has been created.", albumRequest.getArtistName());
            }

            // Create and set album details
            Album album = new Album();
            album.setTitle(albumRequest.getTitle());
            album.setGenre(Genre.valueOf(albumRequest.getGenre()));  // Make sure Genre matches valid enum values
            album.setReleaseYear(albumRequest.getReleaseYear());
            album.setStock(albumRequest.getStock());
            album.setPrice(albumRequest.getPrice());
            album.setArtist(artist); // Set the artist
            album.setCreatedAt(LocalDateTime.now());
            album.setUpdatedAt(LocalDateTime.now());

            // Save the album to the database
            Album savedAlbum = albumRepository.save(album);

            // Return response with the saved album, using the correct HTTP status
            AlbumResponseDTO response = new AlbumResponseDTO(savedAlbum);
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created

        } catch (DataIntegrityViolationException e) {
            log.error("Error inserting album: {}", e.getMessage());
            // Return a generic error response but wrapped inside an AlbumResponseDTO (no actual album created)
            AlbumResponseDTO errorResponse = new AlbumResponseDTO();
            errorResponse.setMessage("Error with album insertion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            // Return a generic error response but wrapped inside an AlbumResponseDTO (no actual album created)
            AlbumResponseDTO errorResponse = new AlbumResponseDTO();
            errorResponse.setMessage("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    // Update an album
    @PutMapping("/album/{id}")
    @Transactional
    public ResponseEntity<AlbumResponseDTO> updateAlbum(@PathVariable Long id, @RequestBody @Valid Album album) {
        try {
            Album existingAlbum = albumRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Album not found"));

            if (!album.getAlbumId().equals(id)) {
                AlbumResponseDTO errorResponse = new AlbumResponseDTO();
                errorResponse.setMessage("Album ID mismatch: Cannot update with a different ID.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Artist artist = album.getArtist() != null && album.getArtist().getArtistId() != null
                    ? artistRepository.findById(album.getArtist().getArtistId())
                    .orElseThrow(() -> new RuntimeException("Artist not found"))
                    : existingAlbum.getArtist();

            if (album.getArtist() != null && album.getArtist().getArtistName() != null) {
                artist.setArtistName(album.getArtist().getArtistName());
                artist = artistRepository.saveAndFlush(artist); // Ensure immediate persistence
            }

            existingAlbum.setTitle(album.getTitle());
            existingAlbum.setGenre(album.getGenre());
            existingAlbum.setReleaseYear(album.getReleaseYear());
            existingAlbum.setStock(album.getStock());
            existingAlbum.setPrice(album.getPrice());
            existingAlbum.setArtist(artist);
            existingAlbum.setUpdatedAt(LocalDateTime.now());

            Album updatedAlbum = albumRepository.saveAndFlush(existingAlbum); // Force immediate update

            AlbumResponseDTO responseDTO = new AlbumResponseDTO(updatedAlbum);
            return ResponseEntity.ok(responseDTO);

        } catch (DataIntegrityViolationException e) {
            log.error("Error inserting album: {}", e.getMessage());
            AlbumResponseDTO errorResponse = new AlbumResponseDTO();
            errorResponse.setMessage("Error with album insertion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            AlbumResponseDTO errorResponse = new AlbumResponseDTO();
            errorResponse.setMessage("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }








    // Delete an album by ID
    @DeleteMapping("/album/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id) {
        Optional<Album> deletedAlbum = albumService.deleteAlbum(id);
        if (deletedAlbum.isPresent()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // Status 204: No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();   // Status 404: Not Found
        }
    }
}
