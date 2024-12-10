package com.northcoders.recordapi.controller;

import com.northcoders.recordapi.service.AlbumCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {

    @Autowired
    private AlbumCache albumCache;

    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupCache() {
        albumCache.removeExpiredEntries();  // Manually trigger cache cleanup
        return ResponseEntity.ok("Cache cleanup completed.");
    }

    @GetMapping("/state")
    public ResponseEntity<Map<Long, AlbumCache.AlbumCachedObject>> getCacheState() {
        return ResponseEntity.ok(albumCache.getAlbumCache());  // Return the current cache state
    }
}
