package vives.bancovives.rest.clients.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Adress {
    private String street;
    private String number;
    private String city;
    private String province;
    private String country;
}
