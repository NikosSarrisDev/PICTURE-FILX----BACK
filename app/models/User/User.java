package models.User;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "users", schema = "PICTURE_FLIX")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String password;

    private String role;

    private boolean verified = false;

    private String phone;

    @Lob
    @Column(name = "photo", columnDefinition = "LONGTEXT")
    private String photo;

    private boolean HasEntered;

    private int numOfTickets;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isHasEntered() {
        return HasEntered;
    }

    public void setHasEntered(boolean hasEntered) {
        HasEntered = hasEntered;
    }

    public int getNumOfTickets() {
        return numOfTickets;
    }

    public void setNumOfTickets(int numOfTickets) {
        this.numOfTickets = numOfTickets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return verified == user.verified && HasEntered == user.HasEntered && numOfTickets == user.numOfTickets && Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(role, user.role) && Objects.equals(phone, user.phone) && Objects.equals(photo, user.photo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role, verified, phone, photo, HasEntered, numOfTickets);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", verified=" + verified +
                ", phone='" + phone + '\'' +
                ", photo='" + photo + '\'' +
                ", HasEntered=" + HasEntered +
                ", numOfTickets=" + numOfTickets +
                '}';
    }
}
