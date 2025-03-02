package models.room;

import jakarta.persistence.*;
import models.View.View;
import models.seat.Seat;

import java.util.*;

@Entity
@Table(name = "rooms", schema = "PICTURE_FLIX")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "TINYTEXT")
    private String quickText;

    private int availableNumberOfSeats;

    private double ticketPrice;

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Seat> seats = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "room_view",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "view_id")
    )
    private Set<View> views;

    @Lob
    @Column(name = "thumbnail", columnDefinition = "LONGTEXT")
    private String thumbnail;

    @Lob
    @Column(name = "image1", columnDefinition = "LONGTEXT")
    private String image1;

    @Lob
    @Column(name = "image2", columnDefinition = "LONGTEXT")
    private String image2;

    @Lob
    @Column(name = "image3", columnDefinition = "LONGTEXT")
    private String image3;

    @Lob
    @Column(name = "image4", columnDefinition = "LONGTEXT")
    private String image4;

    @Lob
    @Column(name = "image5", columnDefinition = "LONGTEXT")
    private String image5;

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

    public String getQuickText() {
        return quickText;
    }

    public void setQuickText(String quickText) {
        this.quickText = quickText;
    }

    public int getAvailableNumberOfSeats() {
        return availableNumberOfSeats;
    }

    public void setAvailableNumberOfSeats(int availableNumberOfSeats) {
        this.availableNumberOfSeats = availableNumberOfSeats;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public Set<View> getViews() {
        return views;
    }

    public void setViews(Set<View> views) {
        this.views = views;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public String getImage5() {
        return image5;
    }

    public void setImage5(String image5) {
        this.image5 = image5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room room)) return false;
        return availableNumberOfSeats == room.availableNumberOfSeats && Objects.equals(id, room.id) && Objects.equals(title, room.title) && Objects.equals(description, room.description) && Objects.equals(seats, room.seats) && Objects.equals(thumbnail, room.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, availableNumberOfSeats, seats, thumbnail);
    }
}
