package com.northcoders.recordapi.controller;

import com.northcoders.recordapi.dto.ArtistResponseDTO;
import com.northcoders.recordapi.model.Artist;
import com.northcoders.recordapi.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.northcoders.recordapi.model.Album;


@RestController
@RequestMapping("/api/v1")  // Base URL for all requests in this controller
public class ArtistController {

    @Autowired
    private ArtistRepository artistRepository;

    @GetMapping("/artist")
    public ResponseEntity<List<ArtistResponseDTO>> getAllArtists() {
        List<Artist> artists = artistRepository.findAll();
        List<ArtistResponseDTO> artistDTOs = artists.stream()
                .map(artist -> new ArtistResponseDTO(
                        artist.getArtistId(),
                        artist.getName(),
                        artist.getAlbums().stream().map(Album::getTitle).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(artistDTOs);
    }


    // Get artist by ID
    @GetMapping("/artist/{id}")
    public ResponseEntity<Artist> getArtistById(@PathVariable Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found with id " + id));
        return ResponseEntity.ok(artist);
    }

    // Create a new artist
    @PostMapping("/artist")
    public ResponseEntity<Artist> createArtist(@RequestBody Artist artist) {
        Artist savedArtist = artistRepository.save(artist);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedArtist);
    }
}
