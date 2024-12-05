package com.northcoders.recordapi.repository;

import com.northcoders.recordapi.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    // GET all albums with .findAll() (provided by JPA)
    Optional<Album> findById(Long id);  // Redundant, can be removed
}
