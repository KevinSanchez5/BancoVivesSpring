package vives.bancovives.rest.products.model;

import lombok.Data;

@Data
public class ProductAccount extends Product{
    private String name;
    private double interes;


    // ejemplo cuenta nombre ahorro con interes 0.5
    // ejemplo cuenta nombre corriente con interes 0.0
}
