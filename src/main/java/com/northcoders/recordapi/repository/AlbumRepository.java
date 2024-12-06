package com.northcoders.recordapi.repository;

import com.northcoders.recordapi.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByTitleAndArtistAndReleaseYear(String title, String artist, int releaseYear);

    // GET all albums with .findAll() is provided by JPA by default
}
