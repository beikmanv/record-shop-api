package com.northcoders.recordapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Genre;
import com.northcoders.recordapi.service.AlbumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class AlbumControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private AlbumController albumController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(albumController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetAllAlbums_Basic() throws Exception {
        mockMvc.perform(get("/api/v1/albums"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetAllAlbums() throws Exception {
        // Arrange
        Album album1 = new Album("Album 1", "Artist 1", Genre.ROCK, 2021, 10, 9.99);
        Album album2 = new Album("Album 2", "Artist 2", Genre.POP, 2022, 15, 12.99);
        List<Album> albums = Arrays.asList(album1, album2);

        when(albumService.getAllAlbums()).thenReturn(albums);

        // Act
        mockMvc.perform(get("/api/v1/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Album 1"))
                .andExpect(jsonPath("$[1].title").value("Album 2"))
                .andReturn();

        // Assert
        verify(albumService, times(1)).getAllAlbums();
    }

    @Test
    public void testGetAllAlbums_Empty() throws Exception {
        // Arrange
        when(albumService.getAllAlbums()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        verify(albumService, times(1)).getAllAlbums();
    }

    @Test
    public void testGetAlbumById() throws Exception {
        // Arrange
        Album album = new Album(1L, "Album 1", "Artist 1", Genre.ROCK, 2021, 10, 9.99, null, null);
        when(albumService.getAlbumById(1L)).thenReturn(Optional.of(album));

        // Act & Assert
        mockMvc.perform(get("/api/v1/album/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Album 1"));
        verify(albumService, times(1)).getAlbumById(1L);
    }

    @Test
    public void testGetAlbumById_NotFound() throws Exception {
        // Arrange
        when(albumService.getAlbumById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/album/1"))
                .andExpect(status().isNotFound());
        verify(albumService, times(1)).getAlbumById(1L);
    }

    @Test
    public void testCreateAlbum_WithParam() throws Exception {
        // Arrange
        Album createdAlbum = new Album(1L, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumService.createAlbum(Mockito.any(Album.class))).thenReturn(createdAlbum);

        // Build the request using MockHttpServletRequestBuilder
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdAlbum)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.title").value("New Album"))
                .andExpect(jsonPath("$.artist").value("New Artist"));

        verify(albumService, times(1)).createAlbum(Mockito.any(Album.class));
    }

    @Test
    public void testCreateAlbum_WithContent() throws Exception {
        // Arrange
        Album album = new Album(null, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        Album createdAlbum = new Album(1L, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumService.createAlbum(Mockito.any(Album.class))).thenReturn(createdAlbum);

        // Act & Assert
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/albums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "New Album",
                                    "artist": "New Artist",
                                    "genre": "JAZZ",
                                    "releaseYear": 2023,
                                    "stock": 20,
                                    "price": 14.99
                                }
                                """);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Album"));
        verify(albumService, times(1)).createAlbum(Mockito.any(Album.class));
    }

    @Test
    public void testCreateAlbumWithoutParamOrContent() throws Exception {
        // Arrange
        Album createdAlbum = new Album(1L, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumService.createAlbum(Mockito.any(Album.class))).thenReturn(createdAlbum);

        // Build the request using MockHttpServletRequestBuilder
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/albums")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "New Album")
                .param("artist", "New Artist")
                .param("genre", "JAZZ")
                .param("releaseYear", "2023")
                .param("stock", "20")
                .param("price", "14.99");

        // Act & Assert
        mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Album"));

        verify(albumService, times(1)).createAlbum(Mockito.any(Album.class));
    }


    @Test
    public void testUpdateAlbum() throws Exception {
        // Arrange
        Album album = new Album(1L, "Updated Album", "Updated Artist", Genre.ROCK, 2023, 30, 19.99, null, null);
        when(albumService.updateAlbum(eq(1L), Mockito.any(Album.class))).thenReturn(album);

        // Act & Assert
        mockMvc.perform(put("/api/v1/album/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated Album",
                                    "artist": "Updated Artist",
                                    "genre": "ROCK",
                                    "releaseYear": 2023,
                                    "stock": 30,
                                    "price": 19.99
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Album"));
        verify(albumService, times(1)).updateAlbum(eq(1L), Mockito.any(Album.class));
    }

    @Test
    public void testUpdateAlbum_NotFound() throws Exception {
        // Arrange
        when(albumService.updateAlbum(eq(1L), Mockito.any(Album.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/api/v1/album/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Updated Album",
                                    "artist": "Updated Artist",
                                    "genre": "ROCK",
                                    "releaseYear": 2023,
                                    "stock": 30,
                                    "price": 19.99
                                }
                                """))
                .andExpect(status().isNotFound());
        verify(albumService, times(1)).updateAlbum(eq(1L), Mockito.any(Album.class));
    }

    @Test
    public void testDeleteAlbum() throws Exception {
        // Arrange
        Album album = new Album(1L, "Album to Delete", "Artist", Genre.CLASSICAL, 2020, 5, 9.99, null, null);
        when(albumService.deleteAlbum(1L)).thenReturn(Optional.of(album));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/album/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Album to Delete"));
        verify(albumService, times(1)).deleteAlbum(1L);
    }

    @Test
    public void testDeleteAlbum_NotFound() throws Exception {
        // Arrange
        when(albumService.deleteAlbum(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/album/1"))
                .andExpect(status().isNotFound());
        verify(albumService, times(1)).deleteAlbum(1L);
    }


}
