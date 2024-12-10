package com.northcoders.recordapi.service;

import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    AlbumCache albumCache;

    @Override
    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    @Override
    public Optional<Album> getAlbumById(Long id) {
        // Check if the album is in the cache and if the cache is valid
        if (albumCache.getAlbumCache().containsKey(id) && albumCache.isValid()) {
            // Return the cached album
            return Optional.of(albumCache.getAlbumCache().get(id).getCachedAlbum());
        } else {
            // If the album is not in the cache, fetch it from the database
            Optional<Album> album = albumRepository.findById(id);
            if (album.isPresent()) {
                // Create a new AlbumCachedObject to put into the cache
                AlbumCache.AlbumCachedObject albumCachedObject = new AlbumCache.AlbumCachedObject(album.get());
                // Put the album into the cache
                albumCache.getAlbumCache().put(id, albumCachedObject);
                // Optionally, update the cache validity flag
                albumCache.isValid = true;
            }
            // Return the album from the database (which was not in the cache)
            return album;
        }
    }

    @Override
    public Album createAlbum(Album album) {
        Optional<Album> existingAlbum = albumRepository.findByTitleAndArtistAndReleaseYear(
                album.getTitle(), album.getArtist(), album.getReleaseYear());

        if (existingAlbum.isPresent()) {
            throw new AlbumAlreadyExistsException("Album already exists");
        } else {
            Album savedAlbum = albumRepository.save(album);
            // Add the newly created album to the cache
            albumCache.putAlbum(savedAlbum.getId(), savedAlbum);
            return savedAlbum;
        }
    }

    @Override
    public Album updateAlbum(Long id, Album album) {
        if (albumRepository.existsById(id)) {
            albumCache.removeExpiredEntries(); // Clean up expired entries before updating
            albumCache.setValid(false); // Invalidate the cache
            album.setId(id);
            Album updatedAlbum = albumRepository.save(album);
            // Add the updated album to the cache
            albumCache.putAlbum(updatedAlbum.getId(), updatedAlbum);
            return updatedAlbum;
        } else {
            throw new RuntimeException("Album not found with ID: " + id);
        }
    }

    @Override
    public Optional<Album> deleteAlbum(Long id) {
        Optional<Album> album = albumRepository.findById(id);
        if (album.isPresent()) {
            // Remove the album from the cache
            albumCache.getAlbumCache().remove(id);
            albumRepository.deleteById(id);
            return album;
        }
        return Optional.empty();
    }

    // Scheduled task to prune expired cache entries every 20 seconds
    @Scheduled(fixedRate = 20000)
    public void cleanUpCache() {
        System.out.println("Running cache cleanup task...");
        int initialSize = albumCache.getAlbumCache().size();
        albumCache.removeExpiredEntries(); // Perform cache cleanup
        int finalSize = albumCache.getAlbumCache().size();
        System.out.println("Cache cleanup completed. Entries removed: " + (initialSize - finalSize));
    }
}

