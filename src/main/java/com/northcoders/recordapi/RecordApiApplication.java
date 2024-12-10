package com.northcoders.recordapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.northcoders.recordapi.model.GenreValid; // Import the custom annotation
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecordApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordApiApplication.class, args);
	}

}
