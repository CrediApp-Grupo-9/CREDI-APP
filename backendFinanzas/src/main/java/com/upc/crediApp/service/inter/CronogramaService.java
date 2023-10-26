package com.upc.crediApp.service.inter;

import com.upc.crediApp.dto.CalculoCronogramaDTO;
import com.upc.crediApp.model.Cronograma;
import com.upc.crediApp.model.Cuota;
import com.upc.crediApp.model.Informacion;

import java.util.List;

public interface CronogramaService {

    List<Cronograma> getAllCronogramas();
    Cronograma createCronograma(Cronograma cronograma);

    List<Cuota>getAllCuotasByCronograma(Long id);

    Informacion getInformacionByCronograma(Long id);
    void deleteCronograma(Long id);

    Cronograma calculoCronogramaSinPlazoDeGracia(CalculoCronogramaDTO variablesCalculo);

    Cronograma calculoCronogramaConPlazoDeGraciaParcial(CalculoCronogramaDTO variablesCalculo);

    Cronograma calculoCronogramaConPlazoDeGraciaTotal(CalculoCronogramaDTO variablesCalculo);

}
