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

    private int row;

    private int col;

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
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
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
        return row == seat.row && col == seat.col && reserved == seat.reserved && Objects.equals(id, seat.id) && Objects.equals(room, seat.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, row, col, reserved, room);
    }
}
