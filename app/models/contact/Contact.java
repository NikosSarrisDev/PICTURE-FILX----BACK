package models.contact;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "contact", schema = "PICTURE_FLIX")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String email;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String text;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact contact)) return false;
        return id == contact.id && Objects.equals(email, contact.email) && Objects.equals(text, contact.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, text);
    }
}
