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
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    @OneToMany(mappedBy = "link")
    private List<Vehicle> vehicles = new ArrayList<>();

    @Lob
    @Column(columnDefinition = "TEXT", nullable = true)
    private String url;

    boolean isSubscribed;

    @Override
    public String toString() {
        return "Link{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", subscriber=" + subscriber +
                ", url='" + url + '\'' +
                ", isSubscribed=" + isSubscribed +
                '}';
    }
}
