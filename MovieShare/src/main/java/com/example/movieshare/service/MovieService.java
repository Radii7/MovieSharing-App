package com.example.movieshare.service;

import com.example.movieshare.model.Movie;
import com.example.movieshare.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Value("${movie.upload.dir}")
    private String uploadDir;

    private static final String DEFAULT_POSTER = "/images/default-poster.jpg";

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie saveLink(String title, String url, String posterUrl) {
        String finalPoster =
                (posterUrl == null || posterUrl.trim().isEmpty())
                        ? DEFAULT_POSTER
                        : posterUrl.trim();

        Movie movie = new Movie(title, Movie.Type.LINK, url, finalPoster);
        return movieRepository.save(movie);
    }

    public Movie saveFile(String title, MultipartFile file, String posterUrl) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Empty file");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        String storedName = UUID.randomUUID() + extension;
        Path target = uploadPath.resolve(storedName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = uploadDir + "/" + storedName;

        String finalPoster =
                (posterUrl == null || posterUrl.trim().isEmpty())
                        ? DEFAULT_POSTER
                        : posterUrl.trim();

        Movie movie = new Movie(title, Movie.Type.FILE, relativePath, finalPoster);
        return movieRepository.save(movie);
    }

    public Optional<Movie> getMovie(Long id) {
        return movieRepository.findById(id);
    }
}
