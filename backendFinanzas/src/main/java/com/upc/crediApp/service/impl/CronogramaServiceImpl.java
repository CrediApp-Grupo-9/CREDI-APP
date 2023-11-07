package com.upc.crediApp.service.impl;

import com.upc.crediApp.dto.DatosEntradaCronograma;
import com.upc.crediApp.exception.ValidationException;
import com.upc.crediApp.helpers.*;
import com.upc.crediApp.model.Cronograma;
import com.upc.crediApp.model.Cuota;
import com.upc.crediApp.model.Informacion;
import com.upc.crediApp.model.Vehiculo;
import com.upc.crediApp.repository.CronogramaRepository;
import com.upc.crediApp.repository.CustomerRepository;
import com.upc.crediApp.service.inter.CronogramaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CronogramaServiceImpl implements CronogramaService {


    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<Cronograma> getAllCronogramas() {
        return null;
    }

    @Override
    public Cronograma createCronogramaParaUsuario(Long id,Cronograma cronograma) {

        return null;

    }

    @Override
    public Cronograma saveCronogramaParaUsuario(Long idUsuario, Cronograma cronograma) {



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
    public Cronograma calculoCronogramaSinPlazoDeGracia(DatosEntradaCronograma datosEntradaCronograma) {

        validacionesSinPlazoGracia(datosEntradaCronograma);
        validacionesCasoTasaNominal(datosEntradaCronograma);
        validacionesCuotaInicial(datosEntradaCronograma);
        validacionesCuotaFinal(datosEntradaCronograma);
        //Aqui hay 2 opciones siempre, o se ingresa la tasa nominal o la tasa efectiva
        //En ese caso todos los calculos se hacen con la tasa efectiva , de no ser así se pasa a tasa efectiva

        Cronograma nuevoCronograma = new Cronograma();
        //Calculamos las cuotas
        List<Cuota> listaCuotas = CalculadoraCuota.obtenerListaCuotasMetodoSinPlazoGracia(datosEntradaCronograma);
        //Cambiamos el listado de cuotas del cronograma
        nuevoCronograma.setCuotas(listaCuotas);
        fillInformation(datosEntradaCronograma,nuevoCronograma);

        return nuevoCronograma;
    }

    @Override
    public Cronograma calculoCronogramaConPlazoDeGraciaParcial(DatosEntradaCronograma datosEntradaCronograma) {

        assert datosEntradaCronograma.getPlazoDeGracia() != null;
        if(datosEntradaCronograma.getPlazoDeGracia().equalsIgnoreCase("Total")){
            //Si el plazo de gracia es total, se devuelve un error
            throw new ValidationException("El plazo de gracia debe ser parcial");
        }

        validacionesCasoTasaNominal(datosEntradaCronograma);
        validacionesCuotaInicial(datosEntradaCronograma);
        validacionesCuotaFinal(datosEntradaCronograma);
        validacionesConPlazoGracia(datosEntradaCronograma);

        Cronograma nuevoCronograma= new Cronograma();

        //Calculamos las cuotas

        //Cuando el plazo de gracia es parcial:
        //1- se calculan los interes y el seguro de desgravamen y estos 2 son pagados en la cuota
        //2- el saldo no sufre variaciones y se queda igual hasta que termine el plazo

        List<Cuota> listaCuotas = CalculadoraCuota.obtenerListaCuotasMetodoConPlazoGraciaParcial(datosEntradaCronograma);
        nuevoCronograma.setCuotas(listaCuotas);
        fillInformation(datosEntradaCronograma,nuevoCronograma);

        return nuevoCronograma;
    }

    @Override
    public Cronograma calculoCronogramaConPlazoDeGraciaTotal(DatosEntradaCronograma datosEntradaCronograma) {

        assert datosEntradaCronograma.getPlazoDeGracia() != null;
        if(datosEntradaCronograma.getPlazoDeGracia().equalsIgnoreCase("Parcial")){
            //Si el plazo de gracia es total, se devuelve un error
            throw new ValidationException("El plazo de gracia debe ser total");
        }

        validacionesCasoTasaNominal(datosEntradaCronograma);
        validacionesCuotaInicial(datosEntradaCronograma);
        validacionesCuotaFinal(datosEntradaCronograma);
        validacionesConPlazoGracia(datosEntradaCronograma);

        Cronograma nuevoCronograma= new Cronograma();
        //Cuando el plazo de gracia es total:
        //1- se calculan los interes y estos son sumados al saldo final (osea el monto a financiar)
        //2- La cuota unicamente es el seguro de desgravamen
        List<Cuota> listaCuotas = CalculadoraCuota.obtenerListaCuotasMetodoConPlazoGraciaTotal(datosEntradaCronograma);
        nuevoCronograma.setCuotas(listaCuotas);
        fillInformation(datosEntradaCronograma,nuevoCronograma);
        return nuevoCronograma;
    }

    public void validacionesSinPlazoGracia(DatosEntradaCronograma datosEntradaCronograma){
        if(datosEntradaCronograma.getPlazoDeGracia()!=null){
            //Si se ingresa el plazo de gracia, se devuelve un error
            throw new ValidationException("No se puede ingresar el plazo de gracia en el presente endpoint");
        }
    }

    public void validacionesConPlazoGracia(DatosEntradaCronograma datosEntradaCronograma){

        double cuotasTotales=CalculadoraCuota.calcularNumeroCuotasTotales(datosEntradaCronograma.getNumeroAnios(), datosEntradaCronograma.getFrecuenciaPago());

        double mitadCuotasTotales= cuotasTotales/2;

        if(datosEntradaCronograma.getPlazoDeGracia()==null || datosEntradaCronograma.getPlazoDeGracia().isEmpty()){
            //Si no se ingresa el plazo de gracia, se devuelve un error
            throw new ValidationException("No se ingreso el plazo de gracia");
        }
        if(datosEntradaCronograma.getTiempoPlazoDeGracia()==null || datosEntradaCronograma.tiempoPlazoDeGracia==0){
            //Si no se ingresa el tiempo del plazo de gracia, se devuelve un error
            throw new ValidationException("No se ingreso el tiempo del plazo de gracia o es 0");
        }
        if(datosEntradaCronograma.getTiempoPlazoDeGracia()>mitadCuotasTotales){
            //Si el plazo de gracia es mayor a la mitad de las cuotas totales, se devuelve un error
            throw new ValidationException("" +
                    "El plazo de gracia no puede ser mayor a la mitad de las cuotas totales dado la frecuencia de pago" +
                    ". Cuotas totales: "+(int)cuotasTotales+" , mitad de cuotas totales: "+ (int)mitadCuotasTotales+", frecuencia de pago elegida: "+ datosEntradaCronograma.getFrecuenciaPago().toUpperCase());
        }

    }
    public void validacionesCasoTasaNominal(DatosEntradaCronograma datosEntradaCronograma){

        if(datosEntradaCronograma.getTipoTasaInteres().equalsIgnoreCase("NOMINAL")){
            if(datosEntradaCronograma.getPlazoTasaInteres()==null || datosEntradaCronograma.getPlazoTasaInteres().isEmpty()){
                //Si no se ingresa el plazo de la tasa nominal, se devuelve un error
                throw new ValidationException("No se ingreso el plazo de la tasa nominal");
            }

            if(datosEntradaCronograma.getCapitalizacion()==null || datosEntradaCronograma.getCapitalizacion().isEmpty()) {
                //Si no se ingresa la capitalizacion de la tasa nominal, se devuelve un error
                throw new ValidationException("No se ingreso la capitalizacion de la tasa nominal");
            }

        }
    }

    public void validacionesCuotaInicial(DatosEntradaCronograma datosEntradaCronograma){

        int porcentajeMinimoCuotaInicial = 20;
        int porcentajeMaximoCuotaInicial = 30;

        if(datosEntradaCronograma.getPorcentajeCuotaInicial()<porcentajeMinimoCuotaInicial || datosEntradaCronograma.getPorcentajeCuotaInicial()>porcentajeMaximoCuotaInicial){
            //Si no se ingresa la cuota inicial, se devuelve un error
            throw new ValidationException("El porcentaje de la cuota inicial debe ser como mínimo "+porcentajeMinimoCuotaInicial+"% y como máximo "+porcentajeMaximoCuotaInicial+"%");
        }
    }

    public void validacionesCuotaFinal(DatosEntradaCronograma datosEntradaCronograma){

            int porcentajeMinimoCuotaFinal = 40;
            int porcentajeMaximoCuotaFinal = 50;

            if(datosEntradaCronograma.getPorcentajeCuotaFinal()<porcentajeMinimoCuotaFinal || datosEntradaCronograma.getPorcentajeCuotaFinal()>porcentajeMaximoCuotaFinal){
                //Si no se ingresa la cuota inicial, se devuelve un error
                throw new ValidationException("El porcentaje de la cuota final debe ser como mínimo "+porcentajeMinimoCuotaFinal+"% y como máximo "+porcentajeMaximoCuotaFinal+"%");
            }

    }

    public void fillInformation(DatosEntradaCronograma datosEntradaCronograma,Cronograma cronograma){


        //Creamos un objeto Informacion y lo rellenamos con lo que se busca
        Informacion information = new Informacion();
        information.setNumeroAnios(datosEntradaCronograma.getNumeroAnios());
        information.setPorcentajeCuotaInicial(datosEntradaCronograma.getPorcentajeCuotaInicial());

        if(information.getTipoTasaInteres()!=null){
            information.setTipoTasaInteres(datosEntradaCronograma.getTipoTasaInteres());
        }else {
            information.setTipoTasaInteres(null);
        }
        information.setPlazoTasaInteres(datosEntradaCronograma.getPlazoTasaInteres().toUpperCase());

        //information.setAbreviaturaTasaInteres(datosEntradaCronograma.getAbreviaturaTasaInteres);
        information.setPorcentajeTasaInteres(datosEntradaCronograma.getPorcentajeTasaInteres());

        information.setCapitalizacion(datosEntradaCronograma.getCapitalizacion());
        if(datosEntradaCronograma.getCapitalizacion()!=null){
            information.setCapitalizacion(datosEntradaCronograma.getCapitalizacion().toUpperCase());
        }else {
            information.setCapitalizacion(null);
        }
        if(datosEntradaCronograma.getPlazoDeGracia()!=null) {
            information.setPlazoDeGracia(datosEntradaCronograma.getPlazoDeGracia().toUpperCase());
        }else {
            information.setPlazoDeGracia(null);
        }
        information.setTiempoPlazoDeGracia(datosEntradaCronograma.getTiempoPlazoDeGracia());
        information.setPorcentajeSeguroDesgravamen(datosEntradaCronograma.getPorcentajeSeguroDesgravamen());

        information.setTiempoSeguroDesgravamen(datosEntradaCronograma.getTiempoSeguroDesgravamen().toUpperCase());
        information.setPorcentajeSeguroVehicular(datosEntradaCronograma.getPorcentajeSeguroVehicular());
        information.setTiempoSeguroVehicular(datosEntradaCronograma.getTiempoSeguroVehicular());
        information.setPortes(datosEntradaCronograma.getPortes());
        information.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
        information.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
        information.setFechaInicio(datosEntradaCronograma.getFechaInicio());

        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(datosEntradaCronograma.getPorcentajeCuotaInicial(), datosEntradaCronograma.getPorcentajeCuotaFinal());
        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        information.setPorcentajePrestamoFinanciar(montoAFinanciar);
        information.setPorcentajeCuotaFinal(datosEntradaCronograma.getPorcentajeCuotaFinal());
        information.setMontoCuotaFinal(Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),datosEntradaCronograma.getPorcentajeCuotaFinal()));
        information.setFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago().toUpperCase());


        //Creamos un objeto vehiculo y lo rellenamos con lo que se busca
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPrecio(datosEntradaCronograma.getPrecioVehiculo());
        vehiculo.setMarca(datosEntradaCronograma.getMarcaVehiculo());
        vehiculo.setModelo(datosEntradaCronograma.getModeloVehiculo());

        //Se actualiza el cronograma
        cronograma.setInformacion(information);
        cronograma.setVehiculo(vehiculo);
    }








}
