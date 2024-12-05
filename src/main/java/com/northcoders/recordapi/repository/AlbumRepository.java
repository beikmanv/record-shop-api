package com.northcoders.recordapi.repository;

import com.northcoders.recordapi.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}
