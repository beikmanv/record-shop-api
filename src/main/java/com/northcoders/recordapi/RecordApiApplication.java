package com.northcoders.recordapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.northcoders.recordapi.model.GenreValid; // Import the custom annotation

@SpringBootApplication
public class RecordApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordApiApplication.class, args);
	}

}
