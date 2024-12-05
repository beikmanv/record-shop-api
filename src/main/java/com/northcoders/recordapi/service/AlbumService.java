package com.northcoders.recordapi.service;

import com.northcoders.recordapi.model.Album;
import java.util.List;
import java.util.Optional;

public interface AlbumService {

    // Get all albums
    List<Album> getAllAlbums();

    // Get album by ID
    Optional<Album> getAlbumById(Long id);

    // Create a new album
    Album createAlbum(Album album);

    // Update an album
    Album updateAlbum(Long id, Album album);

    // Delete an album by ID
    void deleteAlbum(Long id);
}
