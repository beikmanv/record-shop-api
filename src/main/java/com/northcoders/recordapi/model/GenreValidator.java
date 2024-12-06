package com.northcoders.recordapi.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.northcoders.recordapi.model.Genre;

public class GenreValidator implements ConstraintValidator<GenreValid, Genre> {

    @Override
    public boolean isValid(Genre value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return true;
    }
}
