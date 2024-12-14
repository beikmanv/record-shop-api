package com.northcoders.recordapi.dto;

import com.northcoders.recordapi.model.Artist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistResponseDTO {

    private Long artistId;
    private String artistName;
    private List<String> albums;

    public ArtistResponseDTO(Artist artist) {
        this.artistId = artist.getArtistId();
        this.artistName = artist.getArtistName();
        this.albums = albums;
    }

}