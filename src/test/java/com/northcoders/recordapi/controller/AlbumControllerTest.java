package com.northcoders.recordapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import com.northcoders.recordapi.model.Album;
import com.northcoders.recordapi.model.Genre;
import com.northcoders.recordapi.service.AlbumService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AlbumControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AlbumService albumService;

    @InjectMocks
    private AlbumController albumController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(albumController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Register the custom handler
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetAllAlbums_Basic() throws Exception {
        mockMvc.perform(get("/api/v1/album"))
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
        mockMvc.perform(get("/api/v1/album"))

                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Album 1"))
                .andExpect(jsonPath("$[1].title").value("Album 2"))
                .andReturn();
        verify(albumService, times(1)).getAllAlbums();
    }

    @Test
    public void testGetAllAlbums_Empty() throws Exception {
        // Arrange
        when(albumService.getAllAlbums()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/album"))
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
    public void testCreateAlbum_ObjectMapper() throws Exception {
        // Arrange
        Album createdAlbum = new Album(1L, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumService.createAlbum(Mockito.any(Album.class))).thenReturn(createdAlbum);

        // Act
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/album")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdAlbum)))

                // Assert
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.title").value("New Album"))
                .andExpect(jsonPath("$.artist").value("New Artist"));
        verify(albumService, times(1)).createAlbum(Mockito.any(Album.class));
    }

    @Test
    public void testCreateAlbum_WithoutObjectMapper() throws Exception {
        // Arrange
        Album album = new Album(null, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        Album createdAlbum = new Album(1L, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumService.createAlbum(Mockito.any(Album.class))).thenReturn(createdAlbum);

        // Act & Assert
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/album")
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
    public void testCreateAlbum_Ver2() throws Exception {
        // Arrange
        Album createdAlbum = new Album(1L, "New Album", "New Artist", Genre.JAZZ, 2023, 20, 14.99, null, null);
        when(albumService.createAlbum(Mockito.any(Album.class))).thenReturn(createdAlbum);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/album") // Ensure the URL matches the controller's mapping
                        .contentType(MediaType.APPLICATION_JSON)  // Content type should match the controller's expected type
                        .content(objectMapper.writeValueAsString(createdAlbum)))  // Convert the created album to JSON

                .andExpect(status().isCreated())  // Expect status code 201 (Created)
                .andExpect(jsonPath("$.title").value("New Album"))  // Validate the response body
                .andExpect(jsonPath("$.artist").value("New Artist"));

        // Verify that the service method was called exactly once
        verify(albumService, times(1)).createAlbum(Mockito.any(Album.class));
    }

    @Test
    public void testCreateEmptyAlbum() throws Exception {
        // Arrange: Create an empty album object
        Album emptyAlbum = new Album(null, null, null, null, 0, 0, 0.0, null, null);

        // Act & Assert: Perform POST with empty data and expect a 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/album")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyAlbum))) // Sending empty album data
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request because required fields are missing
    }

    @Test
    public void testCreateAlbumWithInvalidData() throws Exception {
        // Arrange: Create an album with invalid data (e.g., a non-numeric value for 'releaseYear')
        String invalidAlbumJson = "{"
                + "\"title\": \"New Album\","
                + "\"artist\": \"New Artist\","
                + "\"genre\": \"JAZZ\","
                + "\"releaseYear\": \"invalid_year\","
                + "\"stock\": 20,"
                + "\"price\": 14.99"
                + "}";

        // Act & Assert: Perform POST with invalid data and expect 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/album")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAlbumJson)) // Sending invalid album data
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request because of invalid data
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
    public void testPutEmptyAlbum() throws Exception {
        // Arrange: Create an empty album object
        Album emptyAlbum = new Album(null, null, null, null, 0, 0, 0.0, null, null);

        // Mock the service: when trying to update album with ID 1, return null (simulate a failure, could also be a valid object)
        when(albumService.updateAlbum(eq(1L), any(Album.class))).thenReturn(null); // Simulating that the album is not found or not updated

        // Act & Assert: Perform PUT with empty data and expect a 400 Bad Request because of validation failures
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/album/1") // Assume the album with ID 1 exists
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyAlbum))) // Sending empty album data
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request due to validation failures

        // Verify that the updateAlbum service method is not called with invalid data
        verify(albumService, times(0)).updateAlbum(eq(1L), any(Album.class)); // Ensure the service method is not called
    }

    @Test
    public void testPutAlbumWithInvalidData() throws Exception {
        // Arrange: Create an album with invalid data (e.g., invalid 'releaseYear')
        String invalidAlbumJson = "{"
                + "\"title\": \"Updated Album\","
                + "\"artist\": \"Updated Artist\","
                + "\"genre\": \"JAZZ\","
                + "\"releaseYear\": \"invalid_year\","
                + "\"stock\": 20,"
                + "\"price\": 14.99"
                + "}";

        // Act & Assert: Perform PUT with invalid data and expect 400 Bad Request
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/album/1") // Assume the album with ID 1 exists
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidAlbumJson)) // Sending invalid album data
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request due to invalid data
    }

    @Test
    public void testDeleteAlbum_Success() throws Exception {
        // Arrange
        Album album = new Album(1L, "Album to Delete", "Artist", Genre.CLASSICAL, 2020, 5, 9.99, null, null);
        when(albumService.deleteAlbum(1L)).thenReturn(Optional.of(album));  // Mock successful deletion

        // Act & Assert
        mockMvc.perform(delete("/api/v1/album/1"))
                .andExpect(status().isNoContent());  // Expect 204 No Content for successful deletion

        // Verify the service method was called once
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

    @Test
    public void testDeleteAlbum_NonExistingAlbum() throws Exception {
        // Arrange: Assume album with ID 999 does not exist
        Long nonExistingId = 999L;

        // Mock the service: Return Optional.empty() to simulate the album not existing
        when(albumService.deleteAlbum(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert: Perform DELETE request and expect 404 Not Found status
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/album/" + nonExistingId))  // Perform DELETE with a non-existing ID
                .andExpect(status().isNotFound());  // Expect 404 Not Found because the album doesn't exist

        // Verify that the service method was called with the correct ID
        verify(albumService, times(1)).deleteAlbum(nonExistingId);
    }

    @Test
    public void testCreateDuplicateAlbum() throws Exception {
        // Arrange: Create a duplicate album
        Album duplicateAlbum = new Album(null, "The Dark Side of the Moon", "Pink Floyd", Genre.ROCK, 1973, 10, 29.99, null, null);

        // Mock the service to throw AlbumAlreadyExistsException when trying to create the album
        when(albumService.createAlbum(any(Album.class)))
                .thenThrow(new AlbumAlreadyExistsException("Album already exists"));

        // Act & Assert: Perform POST request and expect 409 Conflict with the error message
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/album")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateAlbum)))
                .andReturn();

        // Assert: Verify the status and response body
        assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Album already exists"));

        // Verify that the service method is called once
        verify(albumService, times(1)).createAlbum(any(Album.class));
    }

}
