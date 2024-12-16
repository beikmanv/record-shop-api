package com.northcoders.recordapi.service;

import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.exception.ArtistNotFoundException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Artist;
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
    public Album createAlbum(Album album) {
        // Ensure the artist exists before saving the album
        Artist artist = album.getArtist();

        // Check if the artist exists in the database
        Optional<Artist> existingArtist = artistRepository.findById(artist.getArtistId());
        if (existingArtist.isEmpty()) {
            throw new ArtistNotFoundException("Artist with id " + artist.getArtistId() + " not found.");
        }

        // Ensure that no duplicate album exists with the same title, artist, and release year
        Optional<Album> existingAlbum = albumRepository.findByTitleAndArtistAndReleaseYear(
                album.getTitle(),
                existingArtist.get(),  // Use the existing artist
                album.getReleaseYear());

        if (existingAlbum.isPresent()) {
            // Log the conflict details for debugging
            String errorMsg = String.format("Album with title '%s', artist '%s', and release year '%d' already exists.",
                    album.getTitle(),
                    existingArtist.get().getArtistName(),
                    album.getReleaseYear());
            logger.error(errorMsg); // Log detailed conflict error
            throw new AlbumAlreadyExistsException(errorMsg); // Throw detailed exception
        } else {
            // Save the new album in the repository
            Album savedAlbum = albumRepository.save(album);

            // Add the newly created album to the cache
            albumCache.putAlbum(savedAlbum.getAlbumId(), savedAlbum);

            return savedAlbum;
        }
    }

    @Override
    public Album updateAlbum(Long id, Album album) {
        Optional<Album> existingAlbumOpt = albumRepository.findById(id);
        if (existingAlbumOpt.isPresent()) {
            Album existingAlbum = existingAlbumOpt.get();

            // Preserve createdAt and set updatedAt
            album.setCreatedAt(existingAlbum.getCreatedAt()); // Do not change the createdAt
            album.setUpdatedAt(LocalDateTime.now()); // Set updatedAt to the current timestamp

            album.setAlbumId(id); // Ensure that the ID of the album is set correctly

            // Save the updated album
            Album updatedAlbum = albumRepository.save(album);

            // Invalidate and update the cache
            albumCache.removeExpiredEntries(); // Clean up expired entries before updating
            albumCache.setValid(false); // Invalidate the cache
            albumCache.putAlbum(updatedAlbum.getAlbumId(), updatedAlbum); // Update cache with the new version

            return updatedAlbum;
        } else {
            throw new RuntimeException("Album not found with ID: " + id); // If the album doesn't exist
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

