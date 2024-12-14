package com.northcoders.recordapi.service;

import com.northcoders.recordapi.dto.AlbumRequestDTO;
import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.exception.ArtistNotFoundException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Artist;
import com.northcoders.recordapi.model.Genre;
import com.northcoders.recordapi.repository.AlbumRepository;
import com.northcoders.recordapi.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AlbumServiceImpl implements AlbumService {

    private static final Logger logger = LoggerFactory.getLogger(AlbumServiceImpl.class);

    @Autowired
    private ArtistRepository artistRepository;

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
    public Album createAlbum(AlbumRequestDTO albumRequestDTO) {
        Artist artist = albumRequestDTO.getArtist();

        // Ensure the artist exists before proceeding
        if (artist == null || artist.getArtistId() == null) {
            throw new RuntimeException("Artist not provided or invalid.");
        }

        // Create the album from the DTO
        Album album = new Album();
        album.setTitle(albumRequestDTO.getTitle());
        album.setGenre(Genre.valueOf(albumRequestDTO.getGenre())); // Assuming Genre is an Enum
        album.setReleaseYear(albumRequestDTO.getReleaseYear());
        album.setStock(albumRequestDTO.getStock());
        album.setPrice(albumRequestDTO.getPrice());
        album.setArtist(artist); // Set the artist directly

        // Save the album
        return albumRepository.save(album);
    }


    @Override
    public Album updateAlbum(Long id, AlbumRequestDTO albumRequestDTO) {
        Optional<Album> existingAlbumOpt = albumRepository.findById(id);
        if (existingAlbumOpt.isPresent()) {
            Album existingAlbum = existingAlbumOpt.get();

            Artist artist = albumRequestDTO.getArtist();

            // Ensure the artist exists before proceeding
            if (artist == null || artist.getArtistId() == null) {
                throw new RuntimeException("Artist not provided or invalid.");
            }

            // Update album details
            existingAlbum.setTitle(albumRequestDTO.getTitle());
            existingAlbum.setGenre(Genre.valueOf(albumRequestDTO.getGenre())); // Assuming Genre is an Enum
            existingAlbum.setReleaseYear(albumRequestDTO.getReleaseYear());
            existingAlbum.setStock(albumRequestDTO.getStock());
            existingAlbum.setPrice(albumRequestDTO.getPrice());
            existingAlbum.setArtist(artist); // Set the artist directly

            // Save the updated album
            return albumRepository.save(existingAlbum);
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
//        System.out.println("Running cache cleanup task...");
        int initialSize = albumCache.getAlbumCache().size();
        albumCache.removeExpiredEntries(); // Perform cache cleanup
        int finalSize = albumCache.getAlbumCache().size();
//        System.out.println("Cache cleanup completed. Entries removed: " + (initialSize - finalSize));
    }
}

