package com.upc.crediApp.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class CalculoCronogramaDTO {

    public double precioVehiculo;
    public int numeroAnios;
    public double porcentajeCuotaInicial;
    public String tipoTasaInteres;
    public String plazoTasaInteres;
    public double porcentajeTasaInteres;
    public String capitalizacion;
    @Nullable
    public String plazoDeGracia;
    public Integer tiempoPlazoDeGracia;
    public double porcentajeSeguroDesgravamen;
    public String tiempoSeguroDesgravamen;
    public double porcentajeSeguroVehicular;
    public String tiempoSeguroVehicular;
    public double portes;
    public double costosRegistrales;
    public double costosNotariales;
    public String frecuenciaPago;
    public String fechaInicio;
    public double porcentajeCuotaFinal;


}
