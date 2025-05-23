package si.dteeam.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int price ;
    private String modelYear;
    private int mileage ;
    private int powerKW;
    private String url;

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", modelYear=" + modelYear +
                ", mileage=" + mileage +
                ", power=" + powerKW +
                ", url='" + url + '\'' +
                '}';
    }
}
