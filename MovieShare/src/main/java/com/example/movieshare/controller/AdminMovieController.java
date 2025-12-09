package com.example.movieshare.controller;

import com.example.movieshare.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminMovieController {

    private final MovieService movieService;

    public AdminMovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin-movies";
    }

    @PostMapping("/admin/add-link")
    public String addLink(@RequestParam String title,
                          @RequestParam String url,
                          @RequestParam(required = false) String posterUrl) {

        movieService.saveLink(title, url, posterUrl);
        return "redirect:/admin";
    }

    @PostMapping("/admin/add-file")
    public String addFile(@RequestParam String title,
                          @RequestParam("file") MultipartFile file,
                          @RequestParam(required = false) String posterUrl) {

        try {
            movieService.saveFile(title, file, posterUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin";
    }
}
