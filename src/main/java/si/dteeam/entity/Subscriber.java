package si.dteeam.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private Long chatID;

    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "subscriber",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)

    private List<Link> url = new ArrayList<>();

    @Override
    public String toString() {
        return "Subscriber{" +
                "createdAt=" + createdAt +
                ", chatID=" + chatID +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", id=" + id +
                '}';
    }
}

