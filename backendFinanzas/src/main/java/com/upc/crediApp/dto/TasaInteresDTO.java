package com.upc.crediApp.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class TasaInteresDTO {
    public String tipo;
    public String plazo;
    public String abreviatura;
}
