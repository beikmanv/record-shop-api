package com.northcoders.recordapi.service;

import com.northcoders.recordapi.model.Album;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AlbumCache {
    private final HashMap<Long, AlbumCachedObject> albumCache = new HashMap<>();
    boolean isValid;
    private long timeToLive;

    public class AlbumCachedObject {
        public long lastAccessed = System.currentTimeMillis();
        public Album cachedAlbum;

        public AlbumCachedObject(Album cachedAlbum) {
            this.cachedAlbum = cachedAlbum;
        }
    }

    public HashMap<Long, AlbumCachedObject> getAlbumCache() {
        return albumCache;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
