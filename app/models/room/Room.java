package models.room;

import jakarta.persistence.*;
import models.seat.Seat;

import java.util.*;

@Entity
@Table(name = "rooms", schema = "PICTURE_FLIX")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private int availableNumberOfSeats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Seat> seats = new HashSet<>();

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

    public int getAvailableNumberOfSeats() {
        return availableNumberOfSeats;
    }

    public void setAvailableNumberOfSeats(int availableNumberOfSeats) {
        this.availableNumberOfSeats = availableNumberOfSeats;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
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
        if (!(o instanceof Room room)) return false;
        return availableNumberOfSeats == room.availableNumberOfSeats && Objects.equals(id, room.id) && Objects.equals(title, room.title) && Objects.equals(description, room.description) && Objects.equals(seats, room.seats) && Arrays.equals(thumbnail, room.thumbnail);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, title, description, availableNumberOfSeats, seats);
        result = 31 * result + Arrays.hashCode(thumbnail);
        return result;
    }
}
