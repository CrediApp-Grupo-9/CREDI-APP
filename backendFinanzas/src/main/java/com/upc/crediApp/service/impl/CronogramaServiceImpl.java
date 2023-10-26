package com.upc.crediApp.service.impl;

import com.upc.crediApp.dto.CalculoCronogramaDTO;
import com.upc.crediApp.exception.ValidationException;
import com.upc.crediApp.helpers.*;
import com.upc.crediApp.model.Cronograma;
import com.upc.crediApp.model.Cuota;
import com.upc.crediApp.model.Informacion;
import com.upc.crediApp.repository.CronogramaRepository;
import com.upc.crediApp.service.inter.CronogramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CronogramaServiceImpl implements CronogramaService {


    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Override
    public List<Cronograma> getAllCronogramas() {
        return null;
    }

    @Override
    public Cronograma createCronograma(Cronograma cronograma) {
        return null;
    }

    @Override
    public List<Cuota> getAllCuotasByCronograma(Long id) {
        return null;
    }

    @Override
    public Informacion getInformacionByCronograma(Long id) {
        return null;
    }

    @Override
    public void deleteCronograma(Long id) {

    }

    @Override
    public Cronograma calculoCronogramaSinPlazoDeGracia(CalculoCronogramaDTO calculoCronogramaDTO) {

        validacionesSinPlazoGracia(calculoCronogramaDTO);
        validacionesCasoTasaNominal(calculoCronogramaDTO);
        validacionesCuotaInicial(calculoCronogramaDTO);
        validacionesCuotaFinal(calculoCronogramaDTO);
        //Aqui hay 2 opciones siempre, o se ingresa la tasa nominal o la tasa efectiva
        //En ese caso todos los calculos se hacen con la tasa efectiva , de no ser así se pasa a tasa efectiva

        Cronograma nuevoCronograma = new Cronograma();
        //Calculamos las cuotas
        List<Cuota> listaCuotas = CalculadoraCuota.obtenerListaCuotasMetodoSinPlazoGracia(calculoCronogramaDTO);
        //Cambiamos el listado de cuotas del cronograma
        nuevoCronograma.setCuotas(listaCuotas);

        return nuevoCronograma;
    }

    @Override
    public Cronograma calculoCronogramaConPlazoDeGraciaParcial(CalculoCronogramaDTO calculoCronogramaDTO) {

        assert calculoCronogramaDTO.getPlazoDeGracia() != null;
        if(calculoCronogramaDTO.getPlazoDeGracia().equalsIgnoreCase("Total")){
            //Si el plazo de gracia es total, se devuelve un error
            throw new ValidationException("El plazo de gracia debe ser parcial");
        }

        validacionesCasoTasaNominal(calculoCronogramaDTO);
        validacionesCuotaInicial(calculoCronogramaDTO);
        validacionesCuotaFinal(calculoCronogramaDTO);
        validacionesConPlazoGracia(calculoCronogramaDTO);

        Cronograma nuevoCronograma= new Cronograma();

        //Calculamos las cuotas

        //Cuando el plazo de gracia es parcial:
        //1- se calculan los interes y el seguro de desgravamen y estos 2 son pagados en la cuota
        //2- el saldo no sufre variaciones y se queda igual hasta que termine el plazo

        List<Cuota> listaCuotas = CalculadoraCuota.obtenerListaCuotasMetodoConPlazoGraciaParcial(calculoCronogramaDTO);
        nuevoCronograma.setCuotas(listaCuotas);
        return nuevoCronograma;
    }

    @Override
    public Cronograma calculoCronogramaConPlazoDeGraciaTotal(CalculoCronogramaDTO calculoCronogramaDTO) {

        assert calculoCronogramaDTO.getPlazoDeGracia() != null;
        if(calculoCronogramaDTO.getPlazoDeGracia().equalsIgnoreCase("Parcial")){
            //Si el plazo de gracia es total, se devuelve un error
            throw new ValidationException("El plazo de gracia debe ser total");
        }

        validacionesCasoTasaNominal(calculoCronogramaDTO);
        validacionesCuotaInicial(calculoCronogramaDTO);
        validacionesCuotaFinal(calculoCronogramaDTO);
        validacionesConPlazoGracia(calculoCronogramaDTO);

        Cronograma nuevoCronograma= new Cronograma();
        //Cuando el plazo de gracia es total:
        //1- se calculan los interes y estos son sumados al saldo final (osea el monto a financiar)
        //2- La cuota unicamente es el seguro de desgravamen
        List<Cuota> listaCuotas = CalculadoraCuota.obtenerListaCuotasMetodoConPlazoGraciaTotal(calculoCronogramaDTO);
        nuevoCronograma.setCuotas(listaCuotas);
        return nuevoCronograma;
    }

    public void validacionesSinPlazoGracia(CalculoCronogramaDTO calculoCronogramaDTO){
        if(calculoCronogramaDTO.getPlazoDeGracia()!=null){
            //Si se ingresa el plazo de gracia, se devuelve un error
            throw new ValidationException("No se puede ingresar el plazo de gracia en el presente endpoint");
        }
    }

    public void validacionesConPlazoGracia(CalculoCronogramaDTO calculoCronogramaDTO){

        double cuotasTotales=CalculadoraCuota.calcularNumeroCuotasTotales(calculoCronogramaDTO.getNumeroAnios(),calculoCronogramaDTO.getFrecuenciaPago());

        double mitadCuotasTotales= cuotasTotales/2;

        if(calculoCronogramaDTO.getPlazoDeGracia()==null || calculoCronogramaDTO.getPlazoDeGracia().isEmpty()){
            //Si no se ingresa el plazo de gracia, se devuelve un error
            throw new ValidationException("No se ingreso el plazo de gracia");
        }
        if(calculoCronogramaDTO.getTiempoPlazoDeGracia()==null || calculoCronogramaDTO.tiempoPlazoDeGracia==0){
            //Si no se ingresa el tiempo del plazo de gracia, se devuelve un error
            throw new ValidationException("No se ingreso el tiempo del plazo de gracia o es 0");
        }
        if(calculoCronogramaDTO.getTiempoPlazoDeGracia()>mitadCuotasTotales){
            //Si el plazo de gracia es mayor a la mitad de las cuotas totales, se devuelve un error
            throw new ValidationException("" +
                    "El plazo de gracia no puede ser mayor a la mitad de las cuotas totales dado la frecuencia de pago" +
                    ". Cuotas totales: "+(int)cuotasTotales+" , mitad de cuotas totales: "+ (int)mitadCuotasTotales+", frecuencia de pago elegida: "+calculoCronogramaDTO.getFrecuenciaPago().toUpperCase());
        }

    }
    public void validacionesCasoTasaNominal(CalculoCronogramaDTO calculoCronogramaDTO){

        if(calculoCronogramaDTO.getTipoTasaInteres().equalsIgnoreCase("NOMINAL")){
            if(calculoCronogramaDTO.getPlazoTasaInteres()==null || calculoCronogramaDTO.getPlazoTasaInteres().isEmpty()){
                //Si no se ingresa el plazo de la tasa nominal, se devuelve un error
                throw new ValidationException("No se ingreso el plazo de la tasa nominal");
            }

            if(calculoCronogramaDTO.getCapitalizacion()==null || calculoCronogramaDTO.getCapitalizacion().isEmpty()) {
                //Si no se ingresa la capitalizacion de la tasa nominal, se devuelve un error
                throw new ValidationException("No se ingreso la capitalizacion de la tasa nominal");
            }

        }
    }

    public void validacionesCuotaInicial(CalculoCronogramaDTO calculoCronogramaDTO){

        int porcentajeMinimoCuotaInicial = 20;
        int porcentajeMaximoCuotaInicial = 30;

        if(calculoCronogramaDTO.getPorcentajeCuotaInicial()<porcentajeMinimoCuotaInicial || calculoCronogramaDTO.getPorcentajeCuotaInicial()>porcentajeMaximoCuotaInicial){
            //Si no se ingresa la cuota inicial, se devuelve un error
            throw new ValidationException("El porcentaje de la cuota inicial debe ser como mínimo "+porcentajeMinimoCuotaInicial+"% y como máximo "+porcentajeMaximoCuotaInicial+"%");
        }
    }

    public void validacionesCuotaFinal(CalculoCronogramaDTO calculoCronogramaDTO){

            int porcentajeMinimoCuotaFinal = 40;
            int porcentajeMaximoCuotaFinal = 50;

            if(calculoCronogramaDTO.getPorcentajeCuotaFinal()<porcentajeMinimoCuotaFinal || calculoCronogramaDTO.getPorcentajeCuotaFinal()>porcentajeMaximoCuotaFinal){
                //Si no se ingresa la cuota inicial, se devuelve un error
                throw new ValidationException("El porcentaje de la cuota final debe ser como mínimo "+porcentajeMinimoCuotaFinal+"% y como máximo "+porcentajeMaximoCuotaFinal+"%");
            }

    }








}
