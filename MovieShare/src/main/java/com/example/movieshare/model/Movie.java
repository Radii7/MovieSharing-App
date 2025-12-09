package com.example.movieshare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movie")
public class Movie {

    public enum Type {
        FILE,
        LINK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    // If FILE: saved file path in /uploads/
    // If LINK: external streaming URL
    @Column(nullable = false)
    private String source;

    // Poster URL (local or external)
    @Column(name = "poster_url", length = 1000)   // ⭐ Fix: allow long URLs
    private String posterUrl;

    private LocalDateTime sharedAt;

    public Movie() {
    }

    public Movie(String title, Type type, String source, String posterUrl) {
        this.title = title;
        this.type = type;
        this.source = source;
        this.posterUrl = posterUrl;
        this.sharedAt = LocalDateTime.now();
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getPosterUrl() {
        return posterUrl;
    }
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }
    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
}
