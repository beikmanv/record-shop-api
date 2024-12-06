package com.northcoders.recordapi.controller;

import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.service.AlbumService;
import com.northcoders.recordapi.service.AlbumServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    // Get all albums
    @GetMapping("/album")
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.getAllAlbums();
        return ResponseEntity.ok(albums);
    }

    // Get album by ID
    @GetMapping("album/{id}")
    public ResponseEntity<Album> getAlbumById(@PathVariable Long id) {
        Album album = albumService.getAlbumById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Album not found"));
        return ResponseEntity.ok(album);
    }

    // Create a new album
    @PostMapping("/album")
    public ResponseEntity<Album> createAlbum(@Valid @RequestBody Album album) {
        Album createdAlbum = albumService.createAlbum(album);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlbum);
    }

    // Update an album
    @PutMapping("album/{id}")
    public ResponseEntity<Album> updateAlbum(@PathVariable Long id, @RequestBody @Valid Album album) {
        Album existingAlbum = albumService.updateAlbum(id, album);
        if (existingAlbum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Album not found
        } else {
            return ResponseEntity.ok(existingAlbum); // Update successful
        }
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
