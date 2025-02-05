package models.seat;

import jakarta.persistence.*;
import models.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "seats", schema = "PICTURE_FLIX")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int rowSeat;

    private int colSeat;

    private boolean reserved;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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

    public int getRow() {
        return rowSeat;
    }

    public void setRow(int rowSeat) {
        this.rowSeat = rowSeat;
    }

    public int getCol() {
        return colSeat;
    }

    public void setCol(int colSeat) {
        this.colSeat = colSeat;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat seat)) return false;
        return rowSeat == seat.rowSeat && colSeat == seat.colSeat && reserved == seat.reserved && Objects.equals(id, seat.id) && Objects.equals(title, seat.title) && Objects.equals(room, seat.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, rowSeat, colSeat, reserved, room);
    }
}
