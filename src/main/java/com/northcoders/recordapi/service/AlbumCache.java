package com.northcoders.recordapi.service;

import com.northcoders.recordapi.model.Album;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@Data
public class AlbumCache {

    private final HashMap<Long, AlbumCachedObject> albumCache = new HashMap<>();
    private long timeToLive = 20000; // Default TTL is 20 seconds
    public boolean isValid;

    @Data
    public static class AlbumCachedObject {
        private long lastAccessed = System.currentTimeMillis();
        private Album cachedAlbum;

        public AlbumCachedObject(Album cachedAlbum) {
            this.cachedAlbum = cachedAlbum;
        }
    }

    public void removeExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        long expirationThreshold = 20000; // 20 seconds (20,000 milliseconds)
        System.out.println("Removing expired entries...");
        // Iterate over cache entries and remove expired ones
        albumCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue().getLastAccessed() > expirationThreshold
        );
        System.out.println("Cache cleanup completed.");
    }

    public void putAlbum(Long id, Album album) {
        AlbumCachedObject cachedObject = new AlbumCachedObject(album);
        cachedObject.setLastAccessed(System.currentTimeMillis());
        albumCache.put(id, cachedObject);
    }

    public Album getAlbum(Long id) {
        AlbumCachedObject cachedObject = albumCache.get(id);
        if (cachedObject != null) {
            cachedObject.setLastAccessed(System.currentTimeMillis()); // Update access time when accessed
            return cachedObject.getCachedAlbum();
        }
        return null; // Return null if not found or expired
    }
}

