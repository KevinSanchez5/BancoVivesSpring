package vives.bancovives.rest.clients.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Address implements Serializable {
    @Column(name = "street")
    private String street;
    @Column(name = "house_number")
    private String houseNumber;
    @Column(name="city")
    private String city;
    @Column(name="country")
    private String country;
}
