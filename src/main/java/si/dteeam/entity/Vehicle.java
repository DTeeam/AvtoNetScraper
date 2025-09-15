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
    private String brand;
    private String model;
    @Column(nullable = true)
    private Integer price;
    @Column(nullable = true)
    private String modelYear;
    @Column(nullable = true)
    private Integer mileage;
    @Column(nullable = true)
    private Integer powerKW;
    private String url;
    private LocalDateTime dateOfChange;

    @ManyToOne
    @JoinColumn(name = "link_id")
    private Link link;

    private boolean isSubscribed;
}
