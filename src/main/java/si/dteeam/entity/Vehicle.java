package si.dteeam.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer price;
    private String modelYear;
    private int mileage ;
    private int powerKW;
    private String url;
    private LocalDateTime dateOfChange;

    @ManyToOne
    @JoinColumn(name = "link_id")
    private Link link;

    private boolean isSubscribed;

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", modelYear=" + modelYear +
                ", mileage=" + mileage +
                ", power=" + powerKW +
                ", url='" + url +
                ", dateOfChange='" + dateOfChange + '\'' +
                '}';
    }
}
