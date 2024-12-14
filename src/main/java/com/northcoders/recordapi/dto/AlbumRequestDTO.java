package com.northcoders.recordapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumRequestDTO {
    private String artistName;
    private String title;
    private String genre;
    private int releaseYear;
    private int stock;
    private double price;
}
