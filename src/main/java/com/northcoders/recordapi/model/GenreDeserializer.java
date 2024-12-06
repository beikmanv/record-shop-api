package com.northcoders.recordapi.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class GenreDeserializer extends JsonDeserializer<Genre> {

    @Override
    public Genre deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String genreValue = parser.getText().toUpperCase();
        try {
            return Genre.valueOf(genreValue);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid genre: " + genreValue);
        }
    }
}
