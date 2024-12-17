package com.northcoders.recordapi.dto;

import com.northcoders.recordapi.model.Artist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumRequestDTO {

    private Artist artist;    // Use Artist object instead of artistName
    private String title;
    private String genre;
    private int releaseYear;
    private int stock;
    private double price;

}
