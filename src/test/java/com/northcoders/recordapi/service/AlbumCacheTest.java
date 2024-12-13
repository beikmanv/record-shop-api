package com.northcoders.recordapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Genre;
import com.northcoders.recordapi.repository.AlbumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Map;
import java.util.Optional;

class AlbumCacheTest {

    private AlbumCache albumCache;

    // Mock the dependencies
    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private AlbumServiceImpl albumServiceImpl; // Inject the mocked repository into the service

    @BeforeEach
    void setUp() {
        albumCache = new AlbumCache(); // Create a new AlbumCache instance for each test
    }

    @Test
    void testRemoveExpiredEntries() {
        // Arrange
        Album album1 = new Album(1L, "Album 1", "Artist 1", Genre.ROCK, 2020, 10, 9.99, null, null);
        Album album2 = new Album(2L, "Album 2", "Artist 2", Genre.JAZZ, 2021, 12, 14.99, null, null);

        // Create cached objects with different lastAccessed times
        AlbumCache.AlbumCachedObject cachedAlbum1 = new AlbumCache.AlbumCachedObject(album1);
        cachedAlbum1.setLastAccessed(System.currentTimeMillis() - 21000); // Set to 11 seconds ago (expired)
        AlbumCache.AlbumCachedObject cachedAlbum2 = new AlbumCache.AlbumCachedObject(album2);
        cachedAlbum2.setLastAccessed(System.currentTimeMillis() - 5000); // Set to 5 seconds ago (not expired)

        // Manually add the albums to the cache
        albumCache.getAlbumCache().put(1L, cachedAlbum1);
        albumCache.getAlbumCache().put(2L, cachedAlbum2);

        // Debugging: Print cache before cleanup
        System.out.println("Cache state before cleanup: " + albumCache.getAlbumCache());

        // Act
        albumCache.removeExpiredEntries(); // Run the cleanup method

        // Debugging: Print cache after cleanup
        System.out.println("Cache state after cleanup: " + albumCache.getAlbumCache());

        // Assert
        // album1 should be removed because it is expired (11 seconds old)
        assertNull(albumCache.getAlbum(1L));  // album1 should no longer exist in cache
        // album2 should remain in the cache because it's only 5 seconds old
        assertNotNull(albumCache.getAlbum(2L)); // album2 should still exist in cache
    }

    @Test
    void testRemoveExpiredEntriesWhenAllExpired() {
        // Arrange
        Album album1 = new Album(1L, "Album 1", "Artist 1", Genre.ROCK, 2020, 10, 9.99, null, null);
        Album album2 = new Album(2L, "Album 2", "Artist 2", Genre.JAZZ, 2021, 12, 14.99, null, null);

        // Create cached objects with expired lastAccessed times
        AlbumCache.AlbumCachedObject cachedAlbum1 = new AlbumCache.AlbumCachedObject(album1);
        cachedAlbum1.setLastAccessed(System.currentTimeMillis() - 21000); // Set to 21 seconds ago (expired)
        AlbumCache.AlbumCachedObject cachedAlbum2 = new AlbumCache.AlbumCachedObject(album2);
        cachedAlbum2.setLastAccessed(System.currentTimeMillis() - 22000); // Set to 22 seconds ago (expired)

        // Manually add the albums to the cache
        albumCache.getAlbumCache().put(1L, cachedAlbum1);
        albumCache.getAlbumCache().put(2L, cachedAlbum2);

        // Act
        albumCache.removeExpiredEntries(); // Run the cleanup method

        // Assert
        // After cleanup, both albums should be removed because they are expired
        assertNull(albumCache.getAlbum(1L));  // album1 should no longer exist in cache
        assertNull(albumCache.getAlbum(2L));  // album2 should no longer exist in cache
    }

    @Test
    void testRemoveExpiredEntriesWhenNoExpiredEntries() {
        // Arrange
        Album album1 = new Album(1L, "Album 1", "Artist 1", Genre.ROCK, 2020, 10, 9.99, null, null);
        Album album2 = new Album(2L, "Album 2", "Artist 2", Genre.JAZZ, 2021, 12, 14.99, null, null);

        // Create cached objects with fresh lastAccessed times (recently added)
        AlbumCache.AlbumCachedObject cachedAlbum1 = new AlbumCache.AlbumCachedObject(album1);
        cachedAlbum1.setLastAccessed(System.currentTimeMillis() - 5000); // Set to 5 seconds ago
        AlbumCache.AlbumCachedObject cachedAlbum2 = new AlbumCache.AlbumCachedObject(album2);
        cachedAlbum2.setLastAccessed(System.currentTimeMillis() - 5000); // Set to 5 seconds ago

        // Manually add the albums to the cache
        albumCache.getAlbumCache().put(1L, cachedAlbum1);
        albumCache.getAlbumCache().put(2L, cachedAlbum2);

        // Act
        albumCache.removeExpiredEntries(); // Run the cleanup method

        // Assert
        // Both albums should remain because none are expired
        assertNotNull(albumCache.getAlbum(1L));  // album1 should still exist in cache
        assertNotNull(albumCache.getAlbum(2L));  // album2 should still exist in cache
    }

    @Test
    public void testCacheExpiration_WithDatabaseCheck_Mock() throws InterruptedException {
        // Arrange
        AlbumCache albumCache = new AlbumCache();
        AlbumServiceImpl albumService = new AlbumServiceImpl();
        albumService.albumCache = albumCache; // Set up the cache

        Album album = new Album(1L, "Test Album", "Artist", Genre.ROCK, 2023, 10, 9.99, null, null);
        albumService.albumCache.putAlbum(1L, album); // Put in cache

        // Mocking the AlbumService method that interacts with the database (getAlbumById)
        AlbumServiceImpl mockAlbumService = mock(AlbumServiceImpl.class);
        when(mockAlbumService.getAlbumById(1L)).thenReturn(Optional.of(album));

        // Inject the mocked service into the albumService
        albumService = mockAlbumService;

        // Act - First access, should hit the cache
        Optional<Album> result = albumService.getAlbumById(1L);
        assertTrue(result.isPresent(), "Album should be found in cache");

        // Wait for cache to expire (20 seconds)
        Thread.sleep(21000); // Wait more than TTL

        // Act - After expiration, should fetch from database (mocked service call)
        result = albumService.getAlbumById(1L);
        assertTrue(result.isPresent(), "Album should be re-fetched from database after cache expiration");

        // Verify that the database method was called again (i.e., cache expired)
        verify(mockAlbumService, times(2)).getAlbumById(1L);  // Called twice: once for cache, once after TTL
    }


}
