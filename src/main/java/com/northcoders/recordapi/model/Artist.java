package com.northcoders.recordapi.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

//    @Column(nullable = false, length = 255, unique = true)
//    @NotNull(message = "Name is required")
//    @NotBlank(message = "Name must not be blank")
//    private String name;

    @Column(nullable = false)
    private String artistName = "Default Name";

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Manage forward relationship for serialization
    private List<Album> albums;

}
