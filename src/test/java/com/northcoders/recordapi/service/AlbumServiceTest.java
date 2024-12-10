package com.northcoders.recordapi.service;

import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.exception.AlbumNotFoundException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Genre;
import com.northcoders.recordapi.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumCache albumCache;
//    AlbumCache albumCache = new AlbumCache();

    @InjectMocks
    private AlbumServiceImpl albumService;

    @Test
    public void testGetAllAlbums_Success() {
        // Arrange
        List<Album> albums = List.of(
                new Album(1L, "Album 1", "Artist 1", Genre.ROCK, 2021, 10, 9.99, null, null),
                new Album(2L, "Album 2", "Artist 2", Genre.JAZZ, 2020, 5, 14.99, null, null)
        );
        when(albumRepository.findAll()).thenReturn(albums);

        // Act
        List<Album> result = albumService.getAllAlbums();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(albumRepository, times(1)).findAll();
    }

    @Test
    public void testGetAlbumById_Success() {
        // Arrange
        Album album = new Album(1L, "Album 1", "Artist 1", Genre.ROCK, 2021, 10, 9.99, null, null);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act
        Optional<Album> result = albumService.getAlbumById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Album 1", result.get().getTitle());
        verify(albumRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAlbumById_NotFound() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Album> result = albumService.getAlbumById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(albumRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateAlbum_Success() {
        // Arrange
        Album album = new Album(null, "New Album", "Artist", Genre.ROCK, 2022, 15, 19.99, null, null);
        Album savedAlbum = new Album(1L, "New Album", "Artist", Genre.ROCK, 2022, 15, 19.99, null, null);
        when(albumRepository.save(album)).thenReturn(savedAlbum);

        // Act
        Album result = albumService.createAlbum(album);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Album", result.getTitle());
        verify(albumRepository, times(1)).save(album);
    }

    @Test
    public void testUpdateAlbum_Success() {
        // Arrange
        Album existingAlbum = new Album(1L, "Old Album", "Artist", Genre.ROCK, 2020, 5, 9.99, null, null);
        Album updatedAlbum = new Album(null, "Updated Album", "Updated Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(albumRepository.save(updatedAlbum)).thenReturn(updatedAlbum);

        // Act
        Album result = albumService.updateAlbum(1L, updatedAlbum);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Album", result.getTitle());
        verify(albumRepository, times(1)).existsById(1L);
        verify(albumRepository, times(1)).save(updatedAlbum);
    }

    @Test
    public void testUpdateAlbum_NotFound() {
        // Arrange
        Album updatedAlbum = new Album(null, "Updated Album", "Updated Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> albumService.updateAlbum(1L, updatedAlbum));
        assertEquals("Album not found with ID: 1", exception.getMessage());
        verify(albumRepository, times(1)).existsById(1L);
        verify(albumRepository, times(0)).save(any());
    }

    @Test
    public void testDeleteAlbum_Success() {
        // Arrange
        Album album = new Album(1L, "Album to Delete", "Artist", Genre.CLASSICAL, 2020, 5, 9.99, null, null);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // Act
        Optional<Album> result = albumService.deleteAlbum(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Album to Delete", result.get().getTitle());
        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteAlbum_NotFound() {
        // Arrange
        when(albumRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Album> result = albumService.deleteAlbum(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(albumRepository, times(1)).findById(1L);
        verify(albumRepository, times(0)).deleteById(anyLong());
    }

    @Test
    public void testCreateDuplicateAlbum() {
        // Arrange
        Album album = new Album(null, "The Dark Side of the Moon", "Pink Floyd", Genre.ROCK, 1973, 10, 29.99, null, null);
        when(albumRepository.findByTitleAndArtistAndReleaseYear(anyString(), anyString(), anyInt()))
                .thenReturn(java.util.Optional.of(album));  // Simulate the duplicate check

        // Act & Assert: Expect the exception to be thrown
        assertThrows(AlbumAlreadyExistsException.class, () -> {
            albumService.createAlbum(album);
        });

        // Verify the repository was not called to save a duplicate
        verify(albumRepository, times(0)).save(any(Album.class));
    }

    @Test
    public void testScheduledCacheCleanup() {
        // Mock the AlbumCache
        AlbumCache mockCache = mock(AlbumCache.class);

        // Create an instance of AlbumServiceImpl with mock dependencies
        AlbumServiceImpl service = new AlbumServiceImpl();
        service.albumCache = mockCache;

        // Act
        service.cleanUpCache();

        // Assert
        verify(mockCache, times(1)).removeExpiredEntries(); // Ensure method is called
    }

    @Test
    public void testCacheExpiration_Ver2() throws InterruptedException {
        // Arrange
        Album album = new Album(1L, "Test Album", "Test Artist", Genre.ROCK, 2020, 10, 9.99, null, null);
        albumCache.putAlbum(1L, album);  // Put the album into the cache

        // Wait for the cache to expire (timeToLive = 20000ms)
        Thread.sleep(25000);  // Sleep longer than the TTL

        // Act
        Album retrievedAlbum = albumCache.getAlbum(1L);

        // Assert
        assertNull(retrievedAlbum);  // The album should be removed from cache after TTL expiration
    }

    @Test
    public void testCacheReloadAfterExpiration() throws InterruptedException {
        // Arrange
        Album album = new Album(1L, "Test Album", "Test Artist", Genre.ROCK, 2020, 10, 9.99, null, null);
        albumCache.putAlbum(1L, album);  // Put the album into the cache

        // Simulate cache expiration
        Thread.sleep(25000);  // Sleep longer than TTL for the cache

        // Act
        // Simulate fetching from the database after the cache has expired
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        Optional<Album> reloadedAlbum = albumService.getAlbumById(1L);

        // Assert
        assertTrue(reloadedAlbum.isPresent());  // The album should be reloaded from the database
        assertEquals(album.getTitle(), reloadedAlbum.get().getTitle());  // Ensure the reloaded album is correct
        assertEquals(album.getArtist(), reloadedAlbum.get().getArtist());
    }

    @Test
    public void testCachePopulationAndRetrieval() {
        // Arrange
        Album album = new Album(1L, "Test Album", "Test Artist", Genre.ROCK, 2020, 10, 9.99, null, null);
        albumCache.putAlbum(1L, album);  // Put the album into the cache
        // Set up mock behavior
        when(albumCache.getAlbum(1L)).thenReturn(album);

        // Act
        Album retrievedAlbum = albumCache.getAlbum(1L);

        // Assert
        assertNotNull(retrievedAlbum);  // Cache should return the album
        assertEquals(album.getTitle(), retrievedAlbum.getTitle());  // Ensure the album data is correct
        assertEquals(album.getArtist(), retrievedAlbum.getArtist());
    }

}
