package com.upc.crediApp.service.impl;

import com.upc.crediApp.dto.DatosEntradaCronograma;
import com.upc.crediApp.exception.ValidationException;
import com.upc.crediApp.helpers.Calculadora.CalculadoraCuota;
import com.upc.crediApp.helpers.Utilidades.Utilidades;
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

import java.util.ArrayList;
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
    public Cronograma saveCronograma(Long customerId, DatosEntradaCronograma datosEntradaCronograma) {

        validacionCustomer(customerId);

        validarDatosEntrada(datosEntradaCronograma);

        Cronograma nuevoCronograma= new Cronograma();
        nuevoCronograma.setCuotas(calcularListaCuotasSegunPlazo(datosEntradaCronograma));
        fillInformation(customerId,datosEntradaCronograma,nuevoCronograma);

        return nuevoCronograma;
    }

    private List<Cuota> calcularListaCuotasSegunPlazo(DatosEntradaCronograma datosEntradaCronograma){

        if(datosEntradaCronograma.getPlazoDeGracia()==null)return CalculadoraCuota.obtenerListaCuotasMetodoSinPlazoGracia(datosEntradaCronograma);

        switch (datosEntradaCronograma.getPlazoDeGracia().toUpperCase()){
            case "PARCIAL":
                return CalculadoraCuota.obtenerListaCuotasMetodoConPlazoGraciaParcial(datosEntradaCronograma);
            case "TOTAL":
                return CalculadoraCuota.obtenerListaCuotasMetodoConPlazoGraciaTotal(datosEntradaCronograma);
            default:
                return CalculadoraCuota.obtenerListaCuotasMetodoSinPlazoGracia(datosEntradaCronograma);
        }
    }

    private void validarDatosEntrada(DatosEntradaCronograma datosEntradaCronograma){

        if(datosEntradaCronograma.getPlazoDeGracia()!=null){
            validacionesConPlazoGracia(datosEntradaCronograma);
        }
        validacionesCasoTasaNominal(datosEntradaCronograma);
        validacionesCuotaInicial(datosEntradaCronograma);
        validacionesCuotaFinal(datosEntradaCronograma);
    }
    public void fillInformation(Long idCustomer,DatosEntradaCronograma datosEntradaCronograma,Cronograma cronograma){


        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(datosEntradaCronograma.getPorcentajeCuotaInicial(), datosEntradaCronograma.getPorcentajeCuotaFinal());
        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),porcentajePrestamoAFinanciar);


        //Creamos un objeto Informacion y lo rellenamos con lo que se busca
        Informacion information = Informacion.builder()
                .numeroAnios(datosEntradaCronograma.numeroAnios)
                .porcentajeCuotaInicial(datosEntradaCronograma.porcentajeCuotaInicial)
                .plazoTasaInteres(datosEntradaCronograma.plazoTasaInteres)
                .porcentajeTasaInteres(datosEntradaCronograma.porcentajeTasaInteres)
                .capitalizacion(datosEntradaCronograma.capitalizacion)
                .tiempoPlazoDeGracia(datosEntradaCronograma.tiempoPlazoDeGracia)
                .porcentajeSeguroDesgravamen(datosEntradaCronograma.porcentajeSeguroDesgravamen)
                .tiempoSeguroDesgravamen(datosEntradaCronograma.tiempoSeguroDesgravamen)
                .porcentajeSeguroVehicular(datosEntradaCronograma.porcentajeSeguroVehicular)
                .tiempoSeguroVehicular(datosEntradaCronograma.tiempoSeguroVehicular)
                .portes(datosEntradaCronograma.portes)
                .costosRegistrales(datosEntradaCronograma.costosRegistrales)
                .costosNotariales(datosEntradaCronograma.costosNotariales)
                .fechaInicio(datosEntradaCronograma.fechaInicio)
                .porcentajePrestamoFinanciar(porcentajePrestamoAFinanciar)
                .porcentajeCuotaFinal(datosEntradaCronograma.porcentajeCuotaFinal)
                .montoCuotaFinal(Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),datosEntradaCronograma.getPorcentajeCuotaFinal()))
                .frecuenciaPago(datosEntradaCronograma.frecuenciaPago)
                .montoPrestamoFinanciar(montoAFinanciar)
                .build();







        //information.setNumeroAnios(datosEntradaCronograma.getNumeroAnios());
        //information.setPorcentajeCuotaInicial(datosEntradaCronograma.getPorcentajeCuotaInicial());

        if(datosEntradaCronograma.getTipoTasaInteres()!=null){
            information.setTipoTasaInteres(datosEntradaCronograma.getTipoTasaInteres().toUpperCase());
        }else {
            information.setTipoTasaInteres(null);
        }
        //information.setPlazoTasaInteres(datosEntradaCronograma.getPlazoTasaInteres().toUpperCase());

        //information.setAbreviaturaTasaInteres(datosEntradaCronograma.getAbreviaturaTasaInteres);
        //information.setPorcentajeTasaInteres(datosEntradaCronograma.getPorcentajeTasaInteres());

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
        //information.setTiempoPlazoDeGracia(datosEntradaCronograma.getTiempoPlazoDeGracia());
        //information.setPorcentajeSeguroDesgravamen(datosEntradaCronograma.getPorcentajeSeguroDesgravamen());

        //information.setTiempoSeguroDesgravamen(datosEntradaCronograma.getTiempoSeguroDesgravamen().toUpperCase());
        //information.setPorcentajeSeguroVehicular(datosEntradaCronograma.getPorcentajeSeguroVehicular());
        //information.setTiempoSeguroVehicular(datosEntradaCronograma.getTiempoSeguroVehicular());
        //information.setPortes(datosEntradaCronograma.getPortes());
        //information.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
        //information.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
        //information.setFechaInicio(datosEntradaCronograma.getFechaInicio());

        //information.setPorcentajePrestamoFinanciar(montoAFinanciar);
        //information.setPorcentajeCuotaFinal(datosEntradaCronograma.getPorcentajeCuotaFinal());
        //information.setMontoCuotaFinal(Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),datosEntradaCronograma.getPorcentajeCuotaFinal()));
        //information.setFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago().toUpperCase());

        //Creamos un objeto vehiculo y lo rellenamos con lo que se busca
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPrecio(datosEntradaCronograma.getPrecioVehiculo());
        vehiculo.setMarca(datosEntradaCronograma.getMarcaVehiculo());
        vehiculo.setModelo(datosEntradaCronograma.getModeloVehiculo());

        //Se actualiza el cronograma
        cronograma.setInformacion(information);
        cronograma.setVehiculo(vehiculo);

        //Se actualiza el customer
        //Encontramos al customer por su id
        customerRepository.findById(idCustomer).ifPresent(customer -> {
            //Se agrega el cronograma al customer
            cronograma.setCustomer(customer);
            //customer.getCronograma().add(cronograma);
            //Se actualiza el customer
            customerRepository.save(customer);
        });

    }

    private void validacionesConPlazoGracia(DatosEntradaCronograma datosEntradaCronograma){

        double cuotasTotales=CalculadoraCuota.calcularNumeroCuotasTotales(datosEntradaCronograma.getNumeroAnios(), datosEntradaCronograma.getFrecuenciaPago());

        double mitadCuotasTotales= cuotasTotales/2;

        if(datosEntradaCronograma.getPlazoDeGracia().isEmpty()){
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
    private void validacionesCasoTasaNominal(DatosEntradaCronograma datosEntradaCronograma){

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

    private void validacionesCuotaInicial(DatosEntradaCronograma datosEntradaCronograma){

        int porcentajeMinimoCuotaInicial = 20;
        int porcentajeMaximoCuotaInicial = 30;

        if(datosEntradaCronograma.getPorcentajeCuotaInicial()<porcentajeMinimoCuotaInicial || datosEntradaCronograma.getPorcentajeCuotaInicial()>porcentajeMaximoCuotaInicial){
            //Si no se ingresa la cuota inicial, se devuelve un error
            throw new ValidationException("El porcentaje de la cuota inicial debe ser como mínimo "+porcentajeMinimoCuotaInicial+"% y como máximo "+porcentajeMaximoCuotaInicial+"%");
        }
    }

    private void validacionesCuotaFinal(DatosEntradaCronograma datosEntradaCronograma){

        int porcentajeMinimoCuotaFinal = 40;
        int porcentajeMaximoCuotaFinal = 50;

        if(datosEntradaCronograma.getPorcentajeCuotaFinal()<porcentajeMinimoCuotaFinal || datosEntradaCronograma.getPorcentajeCuotaFinal()>porcentajeMaximoCuotaFinal){
            //Si no se ingresa la cuota inicial, se devuelve un error
            throw new ValidationException("El porcentaje de la cuota final debe ser como mínimo "+porcentajeMinimoCuotaFinal+"% y como máximo "+porcentajeMaximoCuotaFinal+"%");
        }

    }

    private void validacionCustomer(Long idCustomer){
        if(!customerRepository.existsById(idCustomer)){
            throw new ValidationException("No existe el customer con el id: "+idCustomer);
        }
    }








}
