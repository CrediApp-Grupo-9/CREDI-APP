package com.upc.crediApp.service.impl;

import com.upc.crediApp.dto.DatosEntradaCronograma;
import com.upc.crediApp.exception.ValidationException;
import com.upc.crediApp.helpers.Calculadora.CalculadoraCuota;
import com.upc.crediApp.helpers.Utilidades.Utilidades;
import com.upc.crediApp.model.*;
import com.upc.crediApp.repository.CronogramaRepository;
import com.upc.crediApp.repository.CuotaRepository;
import com.upc.crediApp.repository.CustomerRepository;
import com.upc.crediApp.repository.TasaInteresRepository;
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

    @Autowired
    private TasaInteresRepository tasaInteresRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

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

        convertirTodosLosCamposAMayuscula(datosEntradaCronograma);

        Cronograma nuevoCronograma= Cronograma.builder().build();


        rellenarCuotas(calcularListaCuotasSegunPlazo(datosEntradaCronograma),nuevoCronograma);
        rellenarInformacion(datosEntradaCronograma,nuevoCronograma);
        rellenarVehiculo(datosEntradaCronograma,nuevoCronograma);
        rellenarCustomer(customerId,nuevoCronograma);

        //Primero se guarda el cronograma y luego se guardan las cuotas del cronograma
        cronogramaRepository.save(nuevoCronograma)
                .getCuotas()
                .forEach(cuota -> cuotaRepository.save(cuota));

        return nuevoCronograma;
    }

    @Override
    public List<Cronograma> getAllCronogramasByCustomerId(Long customerId) {
        if(!customerRepository.existsById(customerId)){
            throw new ValidationException("No existe el customer con el id: "+customerId);
        }
        return cronogramaRepository.findAllByCustomerId(customerId);
    }

    private String determinarAbreviaturaTasaInteres(DatosEntradaCronograma datosEntradaCronograma){

        //obtengo los datos del tipo de tasa y su plazo
        String tipoTasa=datosEntradaCronograma.getTipoTasaInteres();
        String plazoTasa=datosEntradaCronograma.getPlazoTasaInteres();

        //llamo al repositorio de tasa de interes para obtener la abreviatura
        TasaInteres tasaInteres=tasaInteresRepository.findByTipoAndPlazo(tipoTasa,plazoTasa);

        return tasaInteres.abreviatura;
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
    private void convertirTodosLosCamposAMayuscula(DatosEntradaCronograma datosEntradaCronograma){

        if(datosEntradaCronograma.getTipoTasaInteres()!=null){
            datosEntradaCronograma.setTipoTasaInteres(datosEntradaCronograma.getTipoTasaInteres().toUpperCase());
        }else {
            throw new ValidationException("No se ingreso el tipo de tasa de interes");
        }

        if(datosEntradaCronograma.getPlazoTasaInteres()!=null){
            datosEntradaCronograma.setPlazoTasaInteres(datosEntradaCronograma.getPlazoTasaInteres().toUpperCase());
        }else {
            throw new ValidationException("No se ingreso el plazo de la tasa de interes");
        }

        if(datosEntradaCronograma.getTiempoSeguroDesgravamen()!=null){
            datosEntradaCronograma.setTiempoSeguroDesgravamen(datosEntradaCronograma.getTiempoSeguroDesgravamen().toUpperCase());
        }else {
            throw new ValidationException("No se ingreso el tiempo del seguro de desgravamen");
        }

        if(datosEntradaCronograma.getTiempoSeguroVehicular()!=null){
            datosEntradaCronograma.setTiempoSeguroVehicular(datosEntradaCronograma.getTiempoSeguroVehicular().toUpperCase());
        }else{
            throw  new ValidationException("No se ingreso el tiempo del seguro vehicular");
        }

        //la capitalizacion si puede ser nula
        if(datosEntradaCronograma.getCapitalizacion()!=null){
            datosEntradaCronograma.setCapitalizacion(datosEntradaCronograma.getCapitalizacion().toUpperCase());
        }else {
            datosEntradaCronograma.setCapitalizacion(null);
        }

        //el plazo de gracia tambien puede ser nulo
        if(datosEntradaCronograma.getPlazoDeGracia()!=null) {
            datosEntradaCronograma.setPlazoDeGracia(datosEntradaCronograma.getPlazoDeGracia().toUpperCase());
        }else {
            datosEntradaCronograma.setPlazoDeGracia(null);
        }

        //la frecuencia de pago debe estar si o si
        if(datosEntradaCronograma.getFrecuenciaPago()!=null) {
            datosEntradaCronograma.setFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago().toUpperCase());
        }else {
            throw new ValidationException("No se ingreso la frecuencia de pago");
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
    private void rellenarCuotas(List<Cuota> listaCuotas, Cronograma cronograma){
        //Lista externa de cuotas
        List<Cuota> cuotas= new ArrayList<>();
        //Asociar cada cuota al cronograma
        for(Cuota cuota:listaCuotas){
            cuota.setCronograma(cronograma);
            cuotas.add(cuota);
        }
        cronograma.setCuotas(cuotas);
    }
    private void rellenarInformacion(DatosEntradaCronograma datosEntradaCronograma, Cronograma cronograma){


        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(datosEntradaCronograma.getPorcentajeCuotaInicial(), datosEntradaCronograma.getPorcentajeCuotaFinal());
        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //Creamos un objeto Informacion y lo rellenamos con lo que obtenemos mediante el builder
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
                .tipoTasaInteres(datosEntradaCronograma.tipoTasaInteres)
                .plazoDeGracia(datosEntradaCronograma.plazoDeGracia)
                .abreviaturaTasaInteres(determinarAbreviaturaTasaInteres(datosEntradaCronograma))
                .build();
        //Se actualiza el cronograma
        cronograma.setInformacion(information);

    }
    private void rellenarVehiculo(DatosEntradaCronograma datosEntradaCronograma, Cronograma cronograma){


        Vehiculo vehiculo = Vehiculo.builder()
                        .precio(datosEntradaCronograma.getPrecioVehiculo())
                        .marca(datosEntradaCronograma.getMarcaVehiculo())
                        .modelo(datosEntradaCronograma.getModeloVehiculo())
                        .build();
        //Se actualiza el cronograma
        cronograma.setVehiculo(vehiculo);
    }
    public void rellenarCustomer(Long idCustomer, Cronograma cronograma){
        //Se actualiza el customer
        //Encontramos al customer por su id
        customerRepository.findById(idCustomer).ifPresent(customer -> {
            //Se agrega el cronograma al customer
            cronograma.setCustomer(customer);
            //customer.getCronograma().add(cronograma);
            //Se actualiza el customer
            //customerRepository.save(customer);
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
