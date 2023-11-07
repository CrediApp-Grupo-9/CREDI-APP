package com.upc.crediApp.service.inter;

import com.upc.crediApp.dto.DatosEntradaCronograma;
import com.upc.crediApp.model.Cronograma;
import com.upc.crediApp.model.Cuota;
import com.upc.crediApp.model.Informacion;

import java.util.List;

public interface CronogramaService {

    List<Cronograma> getAllCronogramas();
    Cronograma createCronogramaParaUsuario(Long idUsuario, Cronograma cronograma);


    List<Cuota>getAllCuotasByCronograma(Long id);

    Informacion getInformacionByCronograma(Long id);
    void deleteCronograma(Long id);

    Cronograma calculoCronogramaSinPlazoDeGracia(DatosEntradaCronograma variablesCalculo);

    Cronograma calculoCronogramaConPlazoDeGraciaParcial(DatosEntradaCronograma variablesCalculo);

    Cronograma calculoCronogramaConPlazoDeGraciaTotal(DatosEntradaCronograma variablesCalculo);

    Cronograma saveCronogramaParaUsuario(Long idUsuario, Cronograma cronograma);

}
