package com.example.movieshare.controller;

import com.example.movieshare.model.Movie;
import com.example.movieshare.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.Optional;

@Controller
public class PublicMovieController {

    private final MovieService movieService;

    public PublicMovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "user-movies"; // Netflix-style UI template
    }

    @GetMapping("/movies/download/{id}")
    public void downloadMovie(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<Movie> opt = movieService.getMovie(id);
        if (opt.isEmpty() || opt.get().getType() != Movie.Type.FILE) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Movie movie = opt.get();
        Path filePath = Paths.get(movie.getSource());
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = filePath.getFileName().toString();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");

        Files.copy(filePath, response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/movies/open/{id}")
    public String openLink(@PathVariable Long id) {
        Optional<Movie> opt = movieService.getMovie(id);
        if (opt.isEmpty() || opt.get().getType() != Movie.Type.LINK) {
            return "redirect:/";
        }
        return "redirect:" + opt.get().getSource();
    }

    @GetMapping("/movies/watch/{id}")
    public String watchMovie(@PathVariable Long id, Model model) {
        Optional<Movie> opt = movieService.getMovie(id);
        if (opt.isEmpty() || opt.get().getType() != Movie.Type.FILE) {
            return "redirect:/";
        }
        model.addAttribute("movie", opt.get());
        return "watch-movie";
    }

    @GetMapping("/movies/stream/{id}")
    public void streamMovie(@PathVariable Long id,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        Optional<Movie> opt = movieService.getMovie(id);
        if (opt.isEmpty() || opt.get().getType() != Movie.Type.FILE) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Movie movie = opt.get();
        Path filePath = Paths.get(movie.getSource());
        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = Files.size(filePath);
        String rangeHeader = request.getHeader("Range");

        response.setHeader("Accept-Ranges", "bytes");
        response.setContentType("video/mp4");

        if (rangeHeader == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Content-Length", String.valueOf(fileLength));

            try (InputStream in = Files.newInputStream(filePath);
                 OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
            }
            return;
        }

        long start = 0;
        long end = fileLength - 1;

        String[] parts = rangeHeader.replace("bytes=", "").split("-");
        try {
            if (!parts[0].isEmpty()) start = Long.parseLong(parts[0]);
            if (parts.length > 1 && !parts[1].isEmpty()) end = Long.parseLong(parts[1]);
        } catch (Exception ignored) {}

        if (start > end || start >= fileLength) {
            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            response.setHeader("Content-Range", "bytes */" + fileLength);
            return;
        }

        if (end >= fileLength) end = fileLength - 1;

        long contentLength = end - start + 1;

        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range",
                String.format("bytes %d-%d/%d", start, end, fileLength));

        try (SeekableByteChannel channel = Files.newByteChannel(filePath, StandardOpenOption.READ);
             OutputStream out = response.getOutputStream()) {

            channel.position(start);

            byte[] buffer = new byte[8192];
            long bytesRemaining = contentLength;

            while (bytesRemaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, bytesRemaining);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesToRead);
                int bytesRead = channel.read(byteBuffer);

                if (bytesRead == -1) break;

                out.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }
        }
    }
}
