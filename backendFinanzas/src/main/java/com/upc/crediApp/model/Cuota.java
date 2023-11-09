package com.upc.crediApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="cuota")
public class Cuota {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    public Long id;
    @Column(name = "numero_de_cuota", length = 100, nullable = false)
    public int numeroDeCuota;
    @Column(name = "fecha_de_pago", length = 100, nullable = false)
    public String fechaDePago;
    @Column(name = "monto_del_prestamo", length = 100, nullable = false)
    public double montoDelPrestamo;
    @Column(name = "amortizacion", length = 100, nullable = false)
    public double amortizacion;
    @Column(name = "interes", length = 100, nullable = false)
    public double interes;
    @Column(name = "seguro_desgravamen", length = 100, nullable = false)
    public double seguroDesgravamen;
    @Column(name = "seguro_vehicular", length = 100, nullable = false)
    public double seguroVehicular;
    @Column(name = "portes", length = 100, nullable = false)
    public double portes;
    @Column(name = "costos_registrales", length = 100, nullable = false)
    public double costosRegistrales;
    @Column(name = "costos_notariales", length = 100, nullable = false)
    public double costosNotariales;
    @Column(name = "cuota_total", length = 100, nullable = false)
    public double cuotaTotal;


    @ManyToOne()
    @JsonIgnore
    @JoinColumn(name = "cronograma_id", nullable = false)
    private Cronograma cronograma;

}
