package com.northcoders.recordapi.service;

import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumServiceImpl implements AlbumService{

    @Autowired
    private AlbumRepository albumRepository;

    @Override
    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    @Override
    public Optional<Album> getAlbumById(Long id) {
        return albumRepository.findById(id);
    }

    @Override
    public Album createAlbum(Album album) {
        Optional<Album> existingAlbum = albumRepository.findByTitleAndArtistAndReleaseYear(
                album.getTitle(), album.getArtist(), album.getReleaseYear());

        if (existingAlbum.isPresent()) {
            throw new AlbumAlreadyExistsException("Album already exists");
        } else {
            return albumRepository.save(album);
        }
    }

    @Override
    public Album updateAlbum(Long id, Album album) {
        if (albumRepository.existsById((id))) {
            album.setId(id);
            return albumRepository.save(album);
        } else {
            throw new RuntimeException("There's no album with " + id + " id");
        }
    }

    @Override
    public Optional<Album> deleteAlbum(Long id) {
        Optional<Album> album = albumRepository.findById(id);
        if (album.isPresent()) {
            albumRepository.deleteById(id);
            return album;  // Return the deleted album
        }
        return Optional.empty();  // Return empty if the album was not found
    }

}
