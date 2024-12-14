package com.northcoders.recordapi.repository;

import com.northcoders.recordapi.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findById(Long id);  // This is already provided by JpaRepository
    Optional<Artist> findByArtistName(String name);  // Find an artist by their name
}
