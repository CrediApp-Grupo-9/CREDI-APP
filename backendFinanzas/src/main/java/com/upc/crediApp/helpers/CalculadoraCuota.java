package com.upc.crediApp.helpers;

import com.upc.crediApp.dto.DatosEntradaCronograma;
import com.upc.crediApp.model.Cuota;

import java.util.ArrayList;
import java.util.List;

public class CalculadoraCuota {

    public static double realizarCalculoCuotaMensual(double montoPrestamo, double tasaInteresMensual, double tasaDesgravamenMensual, int tiempoDeFinanciamiento) {

        //=+(montoPrestamos*(tasaInteresMensual+tasaDesgravamenMensual))/(1-(1+(tasaInteresMensual+tasaDesgravamenMensual))^-tiempoDeFinanciamiento)
        double tasaTotalMensual = (tasaInteresMensual / 100) + (tasaDesgravamenMensual / 100);
        double denominador = 1 - Math.pow((1 + tasaTotalMensual), -tiempoDeFinanciamiento);
        double cuotaMensual = (montoPrestamo * tasaTotalMensual) / denominador;

        return cuotaMensual;
    }

    public static double realizarCalculoCuotaMensualPlazoGraciaParcial(double montoPrestamo, double tasaInteresMensual, double tasaDesgravamenMensual, int tiempoDeFinanciamiento, int tiempoCuotaActual) {

        //=+(montoPrestamos*(tasaInteresMensual+tasaDesgravamenMensual))/(1-(1+(tasaInteresMensual+tasaDesgravamenMensual))^-tiempoDeFinanciamiento)
        double tasaTotalMensual = (tasaInteresMensual / 100) + (tasaDesgravamenMensual / 100);
        double denominador = 1 - Math.pow((1 + tasaTotalMensual), -(tiempoDeFinanciamiento-tiempoCuotaActual));
        double cuotaMensual = (montoPrestamo * tasaTotalMensual) / denominador;

        return cuotaMensual;
    }

    public static double calcularNumeroCuotasTotales(int tiempoAnios, String frecuenciaPago) {
        int pagosPorAnio = 0;
        String frecuenciaPagoMayuscula= frecuenciaPago.toUpperCase();

        switch (frecuenciaPagoMayuscula) {
            case "DIARIA":
                pagosPorAnio = 365;
                break;
            case "SEMANAL":
                pagosPorAnio = 52;
                break;
            case "QUINCENAL":
                pagosPorAnio = 26;
                break;
            case "MENSUAL":
                pagosPorAnio = 12;
                break;
            case "BIMESTRAL":
                pagosPorAnio = 6;
                break;
            case "TRIMESTRAL":
                pagosPorAnio = 4;
                break;
            case "CUATRIMESTRAL":
                pagosPorAnio = 3;
                break;
            case "SEMESTRAL":
                pagosPorAnio = 2;
                break;
            case "ANUAL":
                pagosPorAnio = 1;
                break;
            default:
                // Manejar un caso por defecto o lanzar una excepción si la frecuencia no es válida
                break;
        }

        return tiempoAnios * pagosPorAnio;
    }


    public static List<Cuota> obtenerListaCuotasMetodoSinPlazoGracia(DatosEntradaCronograma datosEntradaCronograma){

        //Variables Intermedias:
        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(datosEntradaCronograma.getPorcentajeCuotaInicial(), datosEntradaCronograma.getPorcentajeCuotaFinal());

        String fechaInicio = datosEntradaCronograma.getFechaInicio();

        //sacar tasa efectiva de acuerdo a la frecuencia de pago
        double tasaEfectiva;
        //Si la tasa es efectiva, solo se pasa a la frecuencia de pago
        if(datosEntradaCronograma.getTipoTasaInteres().equalsIgnoreCase("EFECTIVA")) {

            tasaEfectiva= CalculadoraTasaInteresEfectiva.convertirEfectivaAEfectivaDeAcuerdoALaFrecuenciaPago(datosEntradaCronograma.getPorcentajeTasaInteres(), datosEntradaCronograma.getPlazoTasaInteres(), datosEntradaCronograma.getFrecuenciaPago());
        }else{
            //Si no es efectiva (osea es nominal), se convierte a efectiva
            //La tasa efectiva nominal debe ser pasada a una tasa efectiva de acuerdo a la frecuencia de pago
            tasaEfectiva= CalculadoraTasaInteresNominal.convertirATasaEfectivaDeAcuerdoALaFrecuenciaPago(datosEntradaCronograma.getPlazoTasaInteres(), datosEntradaCronograma.getPorcentajeTasaInteres(), datosEntradaCronograma.getCapitalizacion(), datosEntradaCronograma.getFrecuenciaPago());
        }


        //sacar tasa seguro desgravamen de acuerdo a la frecuencia de pago
        double tasaDesgravamen = CalculadoraSeguroDesgravamen.calcularTasaSeguroConFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago(), datosEntradaCronograma.getTiempoSeguroDesgravamen(), datosEntradaCronograma.getPorcentajeSeguroDesgravamen());

        //sacar valor del seguro vehicular de acuerdo a la frecuencia de pago
        double seguroVehicular = CalculadoraSeguroVehicular.calcularTasaSeguroVehicularDadoFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago(), datosEntradaCronograma.getTiempoSeguroVehicular(), datosEntradaCronograma.getPorcentajeSeguroVehicular());

        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //sacar cuota inicial
        double cuotaInicial = Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(), datosEntradaCronograma.getPorcentajeCuotaInicial());

        //sacar cuota final
        double cuotaFinal = Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(), datosEntradaCronograma.getPorcentajeCuotaFinal()) ;

        //calculamos cuotas totales
        double numeroCuotas= CalculadoraCuota.calcularNumeroCuotasTotales(datosEntradaCronograma.getNumeroAnios(), datosEntradaCronograma.getFrecuenciaPago());

        double montoPrestamo= montoAFinanciar;
        double amortizacion=0;
        double interes=0;
        double cuota= CalculadoraCuota.realizarCalculoCuotaMensual(montoPrestamo,tasaEfectiva,tasaDesgravamen, (int) numeroCuotas);
        double valorSeguroDesgravamen=0;

        double valorSeguroVehicular= Utilidades.redondear(CalculadoraSeguroVehicular.calculoSeguroVehicularDelVehiculo(datosEntradaCronograma.getPrecioVehiculo(), seguroVehicular), 2);

        double cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales(),2);

        //Instanciamos lista de cuotas
        List<Cuota> listaCuotas = new ArrayList<>();

        //Cuotas del prestamo
        for(int cuotaActual=0;cuotaActual<=numeroCuotas;cuotaActual++){

            Cuota cuotaNueva = new Cuota();

            if(cuotaActual!=0){
                interes= Utilidades.redondear(montoPrestamo*(tasaEfectiva/100),2);
                valorSeguroDesgravamen=Utilidades.redondear(CalculadoraSeguroDesgravamen.calcularSeguroDesgravamenConPrestamo(montoPrestamo,tasaDesgravamen),2);
                amortizacion= Utilidades.redondear(cuotaTotal-interes-valorSeguroDesgravamen-valorSeguroVehicular- datosEntradaCronograma.getPortes()- datosEntradaCronograma.getCostosRegistrales()- datosEntradaCronograma.getCostosNotariales(),2);
                montoPrestamo= Utilidades.redondear(montoPrestamo-amortizacion,2);
                String fechaPago= CalculadoraFechas.calcularFechaDePago(fechaInicio,cuotaActual, datosEntradaCronograma.getFrecuenciaPago());

                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setAmortizacion(amortizacion);
                cuotaNueva.setInteres(interes);
                cuotaNueva.setSeguroDesgravamen(valorSeguroDesgravamen);
                cuotaNueva.setSeguroVehicular(valorSeguroVehicular);
                cuotaNueva.setPortes(datosEntradaCronograma.getPortes());
                cuotaNueva.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
                cuotaNueva.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
                cuotaNueva.setCuotaTotal(cuotaTotal);
                cuotaNueva.setFechaDePago(fechaPago);

            }else{
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setAmortizacion(0);
                cuotaNueva.setInteres(0);
                cuotaNueva.setSeguroDesgravamen(0);
                cuotaNueva.setSeguroVehicular(0);
                cuotaNueva.setPortes(0);
                cuotaNueva.setCostosRegistrales(0);
                cuotaNueva.setCostosNotariales(0);
                cuotaNueva.setCuotaTotal(0);
                cuotaNueva.setFechaDePago(fechaInicio);
            }

            listaCuotas.add(cuotaNueva);

        }

        //Ultima cuota
        amortizacion=0;
        interes=cuotaFinal*(tasaEfectiva/100);
        valorSeguroDesgravamen=cuotaFinal*(tasaDesgravamen/100);
        montoPrestamo=0;

        Cuota ultimaCuota = new Cuota();

        ultimaCuota.setNumeroDeCuota((int) numeroCuotas+1);
        ultimaCuota.setMontoDelPrestamo(cuotaFinal);
        ultimaCuota.setAmortizacion(amortizacion);
        ultimaCuota.setInteres(interes);
        ultimaCuota.setSeguroDesgravamen(valorSeguroDesgravamen);
        ultimaCuota.setSeguroVehicular(valorSeguroVehicular);
        ultimaCuota.setPortes(datosEntradaCronograma.getPortes());
        ultimaCuota.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
        ultimaCuota.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
        ultimaCuota.setCuotaTotal(cuotaFinal+amortizacion+interes+valorSeguroDesgravamen+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales());
        ultimaCuota.setFechaDePago(CalculadoraFechas.calcularFechaDePago(fechaInicio,(int) numeroCuotas+1, datosEntradaCronograma.getFrecuenciaPago()));

        listaCuotas.add(ultimaCuota);

        return listaCuotas;
    }

    public static List<Cuota> obtenerListaCuotasMetodoConPlazoGraciaParcial(DatosEntradaCronograma datosEntradaCronograma){
        //Variables Intermedias:
        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(datosEntradaCronograma.getPorcentajeCuotaInicial(), datosEntradaCronograma.getPorcentajeCuotaFinal());

        String fechaInicio = datosEntradaCronograma.getFechaInicio();

        //sacar tasa efectiva de acuerdo a la frecuencia de pago
        double tasaEfectiva;
        //Si la tasa es efectiva, solo se pasa a la frecuencia de pago
        if(datosEntradaCronograma.getTipoTasaInteres().equalsIgnoreCase("EFECTIVA")) {

            tasaEfectiva= CalculadoraTasaInteresEfectiva.convertirEfectivaAEfectivaDeAcuerdoALaFrecuenciaPago(datosEntradaCronograma.getPorcentajeTasaInteres(), datosEntradaCronograma.getPlazoTasaInteres(), datosEntradaCronograma.getFrecuenciaPago());
        }else{
            //Si no es efectiva (osea es nominal), se convierte a efectiva
            //La tasa efectiva nominal debe ser pasada a una tasa efectiva de acuerdo a la frecuencia de pago
            tasaEfectiva= CalculadoraTasaInteresNominal.convertirATasaEfectivaDeAcuerdoALaFrecuenciaPago(datosEntradaCronograma.getPlazoTasaInteres(), datosEntradaCronograma.getPorcentajeTasaInteres(), datosEntradaCronograma.getCapitalizacion(), datosEntradaCronograma.getFrecuenciaPago());
        }



        //sacar tasa seguro desgravamen de acuerdo a la frecuencia de pago
        double tasaDesgravamen = CalculadoraSeguroDesgravamen.calcularTasaSeguroConFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago(), datosEntradaCronograma.getTiempoSeguroDesgravamen(), datosEntradaCronograma.getPorcentajeSeguroDesgravamen());

        //sacar valor del seguro vehicular de acuerdo a la frecuencia de pago
        double seguroVehicular = CalculadoraSeguroVehicular.calcularTasaSeguroVehicularDadoFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago(), datosEntradaCronograma.getTiempoSeguroVehicular(), datosEntradaCronograma.getPorcentajeSeguroVehicular());

        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //sacar cuota inicial
        double cuotaInicial = Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(), datosEntradaCronograma.getPorcentajeCuotaInicial());

        //sacar cuota final
        double cuotaFinal = Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(), datosEntradaCronograma.getPorcentajeCuotaFinal()) ;

        //calculamos cuotas totales
        double numeroCuotas= CalculadoraCuota.calcularNumeroCuotasTotales(datosEntradaCronograma.getNumeroAnios(), datosEntradaCronograma.getFrecuenciaPago());
        //Calculamos cuotas de plazo de gracia
        double numeroCuotasParciales= (double) datosEntradaCronograma.getTiempoPlazoDeGracia();

        double montoPrestamo= montoAFinanciar;
        double amortizacion=0;
        double interes=0;
        double cuota= 0;
        double valorSeguroDesgravamen=0;

        double valorSeguroVehicular= Utilidades.redondear(CalculadoraSeguroVehicular.calculoSeguroVehicularDelVehiculo(datosEntradaCronograma.getPrecioVehiculo(), seguroVehicular), 2);

        //double cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales(),2);
        double cuotaTotal=0;
        //Instanciamos lista de cuotas
        List<Cuota> listaCuotas = new ArrayList<>();

        //Cuotas del prestamo
        for(int cuotaActual=0;cuotaActual<=numeroCuotas;cuotaActual++){

            Cuota cuotaNueva = new Cuota();

            if(cuotaActual!=0){

                interes= Utilidades.redondear(montoPrestamo*(tasaEfectiva/100),2);
                valorSeguroDesgravamen=Utilidades.redondear(CalculadoraSeguroDesgravamen.calcularSeguroDesgravamenConPrestamo(montoPrestamo,tasaDesgravamen),2);

                if(cuotaActual<=numeroCuotasParciales){
                    amortizacion=0;
                    //La cuota total en el caso de plazo de gracia parcial la cuota es igual a los intereses, sin embargo sumando el seguro desgravamen y los costos periodicos...
                    cuotaTotal=interes+valorSeguroDesgravamen+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales();
                }else{
                    cuota= CalculadoraCuota.realizarCalculoCuotaMensualPlazoGraciaParcial(montoPrestamo,tasaEfectiva,tasaDesgravamen, (int) numeroCuotas,cuotaActual-1);
                    cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales(),2);
                    amortizacion= Utilidades.redondear(cuotaTotal-interes-valorSeguroDesgravamen-valorSeguroVehicular- datosEntradaCronograma.getPortes()- datosEntradaCronograma.getCostosRegistrales()- datosEntradaCronograma.getCostosNotariales(),2);
                }

                montoPrestamo= Utilidades.redondear(montoPrestamo-amortizacion,2);

                String fechaPago= CalculadoraFechas.calcularFechaDePago(fechaInicio,cuotaActual, datosEntradaCronograma.getFrecuenciaPago());

                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setAmortizacion(amortizacion);
                cuotaNueva.setInteres(interes);
                cuotaNueva.setSeguroDesgravamen(valorSeguroDesgravamen);
                cuotaNueva.setSeguroVehicular(valorSeguroVehicular);
                cuotaNueva.setPortes(datosEntradaCronograma.getPortes());
                cuotaNueva.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
                cuotaNueva.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
                cuotaNueva.setCuotaTotal(cuotaTotal);
                cuotaNueva.setFechaDePago(fechaPago);

            }else{
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setAmortizacion(0);
                cuotaNueva.setInteres(0);
                cuotaNueva.setSeguroDesgravamen(0);
                cuotaNueva.setSeguroVehicular(0);
                cuotaNueva.setPortes(0);
                cuotaNueva.setCostosRegistrales(0);
                cuotaNueva.setCostosNotariales(0);
                cuotaNueva.setCuotaTotal(0);
                cuotaNueva.setFechaDePago(fechaInicio);
            }

            listaCuotas.add(cuotaNueva);

        }

        //Ultima cuota
        amortizacion=0;
        interes=cuotaFinal*(tasaEfectiva/100);
        valorSeguroDesgravamen=cuotaFinal*(tasaDesgravamen/100);
        montoPrestamo=0;

        Cuota ultimaCuota = new Cuota();

        ultimaCuota.setNumeroDeCuota((int) numeroCuotas+1);
        ultimaCuota.setMontoDelPrestamo(cuotaFinal);
        ultimaCuota.setAmortizacion(amortizacion);
        ultimaCuota.setInteres(interes);
        ultimaCuota.setSeguroDesgravamen(valorSeguroDesgravamen);
        ultimaCuota.setSeguroVehicular(valorSeguroVehicular);
        ultimaCuota.setPortes(datosEntradaCronograma.getPortes());
        ultimaCuota.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
        ultimaCuota.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
        ultimaCuota.setCuotaTotal(cuotaFinal+amortizacion+interes+valorSeguroDesgravamen+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales());
        ultimaCuota.setFechaDePago(CalculadoraFechas.calcularFechaDePago(fechaInicio,(int) numeroCuotas+1, datosEntradaCronograma.getFrecuenciaPago()));

        listaCuotas.add(ultimaCuota);

        return listaCuotas;
    }

    public static List<Cuota> obtenerListaCuotasMetodoConPlazoGraciaTotal(DatosEntradaCronograma datosEntradaCronograma){

        //Variables Intermedias:
        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(datosEntradaCronograma.getPorcentajeCuotaInicial(), datosEntradaCronograma.getPorcentajeCuotaFinal());

        String fechaInicio = datosEntradaCronograma.getFechaInicio();


        //sacar tasa efectiva de acuerdo a la frecuencia de pago
        double tasaEfectiva;
        //Si la tasa es efectiva, solo se pasa a la frecuencia de pago
        if(datosEntradaCronograma.getTipoTasaInteres().equalsIgnoreCase("EFECTIVA")) {

            tasaEfectiva= CalculadoraTasaInteresEfectiva.convertirEfectivaAEfectivaDeAcuerdoALaFrecuenciaPago(datosEntradaCronograma.getPorcentajeTasaInteres(), datosEntradaCronograma.getPlazoTasaInteres(), datosEntradaCronograma.getFrecuenciaPago());
        }else{
            //Si no es efectiva (osea es nominal), se convierte a efectiva
            //La tasa efectiva nominal debe ser pasada a una tasa efectiva de acuerdo a la frecuencia de pago
            tasaEfectiva= CalculadoraTasaInteresNominal.convertirATasaEfectivaDeAcuerdoALaFrecuenciaPago(datosEntradaCronograma.getPlazoTasaInteres(), datosEntradaCronograma.getPorcentajeTasaInteres(), datosEntradaCronograma.getCapitalizacion(), datosEntradaCronograma.getFrecuenciaPago());
        }

        //sacar tasa seguro desgravamen de acuerdo a la frecuencia de pago
        double tasaDesgravamen = CalculadoraSeguroDesgravamen.calcularTasaSeguroConFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago(), datosEntradaCronograma.getTiempoSeguroDesgravamen(), datosEntradaCronograma.getPorcentajeSeguroDesgravamen());

        //sacar valor del seguro vehicular de acuerdo a la frecuencia de pago
        double seguroVehicular = CalculadoraSeguroVehicular.calcularTasaSeguroVehicularDadoFrecuenciaPago(datosEntradaCronograma.getFrecuenciaPago(), datosEntradaCronograma.getTiempoSeguroVehicular(), datosEntradaCronograma.getPorcentajeSeguroVehicular());

        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //sacar cuota inicial
        double cuotaInicial = Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(), datosEntradaCronograma.getPorcentajeCuotaInicial());

        //sacar cuota final
        double cuotaFinal = Utilidades.calcularMontoAplicandoPorcentaje(datosEntradaCronograma.getPrecioVehiculo(), datosEntradaCronograma.getPorcentajeCuotaFinal()) ;

        //calculamos cuotas totales
        double numeroCuotas= CalculadoraCuota.calcularNumeroCuotasTotales(datosEntradaCronograma.getNumeroAnios(), datosEntradaCronograma.getFrecuenciaPago());
        //Calculamos cuotas de plazo de gracia
        double numeroCuotasPlazoTotal= (double) datosEntradaCronograma.getTiempoPlazoDeGracia();

        double montoPrestamo= montoAFinanciar;
        double amortizacion=0;
        double interes=0;
        double cuota= 0;
        double valorSeguroDesgravamen=0;

        double valorSeguroVehicular= Utilidades.redondear(CalculadoraSeguroVehicular.calculoSeguroVehicularDelVehiculo(datosEntradaCronograma.getPrecioVehiculo(), seguroVehicular), 2);

        //double cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales(),2);
        double cuotaTotal=0;
        //Instanciamos lista de cuotas
        List<Cuota> listaCuotas = new ArrayList<>();

        //Cuotas del prestamo
        for(int cuotaActual=0;cuotaActual<=numeroCuotas;cuotaActual++){

            Cuota cuotaNueva = new Cuota();

            if(cuotaActual!=0){

                interes= Utilidades.redondear(montoPrestamo*(tasaEfectiva/100),2);
                valorSeguroDesgravamen=Utilidades.redondear(CalculadoraSeguroDesgravamen.calcularSeguroDesgravamenConPrestamo(montoPrestamo,tasaDesgravamen),2);

                if(cuotaActual<=numeroCuotasPlazoTotal){
                    amortizacion=0;
                    montoPrestamo += interes;
                    //La cuota total en el caso de plazo de gracia total debe ser 0 porque no se paga nada , sin embargo si se tiene en cuenta los costos periodicos...
                    cuotaTotal=valorSeguroDesgravamen+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales();
                }else{
                    cuota= CalculadoraCuota.realizarCalculoCuotaMensualPlazoGraciaParcial(montoPrestamo,tasaEfectiva,tasaDesgravamen, (int) numeroCuotas,cuotaActual-1);
                    cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales(),2);
                    amortizacion= Utilidades.redondear(cuotaTotal-interes-valorSeguroDesgravamen-valorSeguroVehicular- datosEntradaCronograma.getPortes()- datosEntradaCronograma.getCostosRegistrales()- datosEntradaCronograma.getCostosNotariales(),2);
                }

                montoPrestamo= Utilidades.redondear(montoPrestamo-amortizacion,2);

                String fechaPago= CalculadoraFechas.calcularFechaDePago(fechaInicio,cuotaActual, datosEntradaCronograma.getFrecuenciaPago());

                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setAmortizacion(amortizacion);
                cuotaNueva.setInteres(interes);
                cuotaNueva.setSeguroDesgravamen(valorSeguroDesgravamen);
                cuotaNueva.setSeguroVehicular(valorSeguroVehicular);
                cuotaNueva.setPortes(datosEntradaCronograma.getPortes());
                cuotaNueva.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
                cuotaNueva.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
                cuotaNueva.setCuotaTotal(cuotaTotal);
                cuotaNueva.setFechaDePago(fechaPago);


            }else{
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setAmortizacion(0);
                cuotaNueva.setInteres(0);
                cuotaNueva.setSeguroDesgravamen(0);
                cuotaNueva.setSeguroVehicular(0);
                cuotaNueva.setPortes(0);
                cuotaNueva.setCostosRegistrales(0);
                cuotaNueva.setCostosNotariales(0);
                cuotaNueva.setCuotaTotal(0);
                cuotaNueva.setFechaDePago(fechaInicio);
            }

            listaCuotas.add(cuotaNueva);

        }

        //Ultima cuota
        amortizacion=0;
        interes=cuotaFinal*(tasaEfectiva/100);
        valorSeguroDesgravamen=cuotaFinal*(tasaDesgravamen/100);
        montoPrestamo=0;

        Cuota ultimaCuota = new Cuota();

        ultimaCuota.setNumeroDeCuota((int) numeroCuotas+1);
        ultimaCuota.setMontoDelPrestamo(cuotaFinal);
        ultimaCuota.setAmortizacion(amortizacion);
        ultimaCuota.setInteres(interes);
        ultimaCuota.setSeguroDesgravamen(valorSeguroDesgravamen);
        ultimaCuota.setSeguroVehicular(valorSeguroVehicular);
        ultimaCuota.setPortes(datosEntradaCronograma.getPortes());
        ultimaCuota.setCostosRegistrales(datosEntradaCronograma.getCostosRegistrales());
        ultimaCuota.setCostosNotariales(datosEntradaCronograma.getCostosNotariales());
        ultimaCuota.setCuotaTotal(cuotaFinal+amortizacion+interes+valorSeguroDesgravamen+valorSeguroVehicular+ datosEntradaCronograma.getPortes()+ datosEntradaCronograma.getCostosRegistrales()+ datosEntradaCronograma.getCostosNotariales());
        ultimaCuota.setFechaDePago(CalculadoraFechas.calcularFechaDePago(fechaInicio,(int) numeroCuotas+1, datosEntradaCronograma.getFrecuenciaPago()));

        listaCuotas.add(ultimaCuota);

        return listaCuotas;
    }

}
