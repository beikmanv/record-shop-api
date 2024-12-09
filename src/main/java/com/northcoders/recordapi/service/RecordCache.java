package com.northcoders.recordapi.service;

import com.northcoders.recordapi.model.Album;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class RecordCache {
    private final HashMap<Long, Album> recordCache = new HashMap<>();
    boolean isValid;

    public HashMap<Long, Album> getRecordCache() {
        return recordCache;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
