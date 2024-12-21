package models.actors;

import jakarta.persistence.*;
import models.Movies.Movie;

import java.util.*;

@Entity
@Table(name = "actors", schema = "PICTURE_FLIX")
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;

    private float rating;

    @ManyToMany(mappedBy = "actors")
    private Set<Movie> movies = new HashSet<>();

    @Lob
    @Column(name = "photo", columnDefinition = "BLOB")
    private byte[] photo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Set<Movie> getMovies() {
        return movies;
    }

    public void setMovies(Set<Movie> movies) {
        this.movies = movies;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Actor actor)) return false;
        return age == actor.age && Float.compare(rating, actor.rating) == 0 && Objects.equals(id, actor.id) && Objects.equals(name, actor.name) && Objects.equals(movies, actor.movies) && Arrays.equals(photo, actor.photo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, name, age, rating, movies);
        result = 31 * result + Arrays.hashCode(photo);
        return result;
    }
}
