package models.Movies;

import jakarta.persistence.*;
import models.actors.Actor;

import java.util.*;

@Entity
@Table(name = "movies", schema = "PICTURE_FLIX")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    private String director;

    private String producer;

    private double rating;

    private String type;

    private String trailerCode;

    private String rated;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id")
    )
    private Set<Actor> actors = new HashSet<>();

    private int duration;

    @Temporal(TemporalType.DATE)
    @Column(name = "release_date")
    private Date releaseDate;


    private int ticketCount;

    @Lob
    @Column(name = "thumbnail", columnDefinition = "BLOB")
    private byte[] thumbnail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTrailerCode() {
        return trailerCode;
    }

    public void setTrailerCode(String trailerCode) {
        this.trailerCode = trailerCode;
    }

    public String getRated() {
        return rated;
    }

    public void setRated(String rated) {
        this.rated = rated;
    }

    public Set<Actor> getActors() {
        return actors;
    }

    public void setActors(Set<Actor> actors) {
        this.actors = actors;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie movie)) return false;
        return duration == movie.duration && ticketCount == movie.ticketCount && Objects.equals(id, movie.id) && Objects.equals(title, movie.title) && Objects.equals(description, movie.description) && Objects.equals(director, movie.director) && Objects.equals(producer, movie.producer) && Objects.equals(actors, movie.actors) && Arrays.equals(thumbnail, movie.thumbnail);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, title, description, director, producer, actors, duration, ticketCount);
        result = 31 * result + Arrays.hashCode(thumbnail);
        return result;
    }
}
