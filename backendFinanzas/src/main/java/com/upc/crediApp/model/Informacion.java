package com.upc.crediApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "informacion")
public class Informacion {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    public Long id;
    @Column(name = "numero_anios", length = 100, nullable = false)
    public int numeroAnios;
    @Column(name = "porcentaje_cuota_inicial", length = 100, nullable = false)
    public double porcentajeCuotaInicial;
    @Column(name = "tipo_tasa_interes", length = 100, nullable = false)
    public String tipoTasaInteres;
    @Column(name = "plazo_tasa_interes", length = 100, nullable = false)
    public int plazoTasaInteres;
    @Column(name = "abreviatura_tasa_interes", length = 100, nullable = false)
    public String abreviaturaTasaInteres;
    @Column(name = "porcentaje_tasa_interes", length = 100, nullable = false)
    public double porcentajeTasaInteres;
    @Column(name = "capitalizacion", length = 100, nullable = true)
    public String capitalizacion;
    @Column(name = "plazo_de_gracia", length = 100, nullable = true)
    public String plazoDeGracia;
    @Column(name = "tiempo_plazo_de_gracia", length = 100, nullable = true)
    public int tiempoPlazoDeGracia;
    @Column(name = "porcentaje_seguro_desgravamen", length = 100, nullable = false)
    public double porcentajeSeguroDesgravamen;
    @Column(name = "tiempo_seguro_desgravamen", length = 100, nullable = false)
    public String tiempoSeguroDesgravamen;
    @Column(name = "porcentaje_seguro_vehicular", length = 100, nullable = false)
    public double porcentajeSeguroVehicular;
    @Column(name = "tiempo_seguro_vehicular", length = 100, nullable = false)
    public String tiempoSeguroVehicular;
    @Column(name = "portes", length = 100, nullable = false)
    public double portes;
    @Column(name = "costos_registrales", length = 100, nullable = false)
    public double costosRegistrales;
    @Column(name = "costos_notariales", length = 100, nullable = false)
    public double costosNotariales;
    @Column(name = "fecha_inicio", length = 100, nullable = false)
    public String fechaInicio;
    @Column(name = "porcentaje_prestamo_financiar", length = 100, nullable = false)
    public double porcentajePrestamoFinanciar;
    @Column(name = "monto_prestamo_financiar", length = 100, nullable = false)
    public double montoPrestamoFinanciar;
    @Column(name = "porcentaje_cuota_final", length = 100, nullable = false)
    public double porcentajeCuotaFinal;
    @Column(name = "monto_cuota_final", length = 100, nullable = false)
    public double montoCuotaFinal;

    @JsonIgnore
    @ManyToMany
    private List<Moneda> monedas;

    @OneToOne(mappedBy = "informacion")
    private Cronograma cronograma;


}
