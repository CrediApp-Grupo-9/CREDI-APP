package com.upc.crediApp.helpers;

import com.upc.crediApp.dto.CalculoCronogramaDTO;
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


    public static List<Cuota> obtenerListaCuotasMetodoSinPlazoGracia(CalculoCronogramaDTO calculoCronogramaDTO){

        //Variables Intermedias:
        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(calculoCronogramaDTO.getPorcentajeCuotaInicial(),calculoCronogramaDTO.getPorcentajeCuotaFinal());

        String fechaInicio = calculoCronogramaDTO.getFechaInicio();

        //sacar tasa efectiva de acuerdo a la frecuencia de pago
        double tasaEfectiva;
        //Si la tasa es efectiva, solo se pasa a la frecuencia de pago
        if(calculoCronogramaDTO.getTipoTasaInteres().equalsIgnoreCase("EFECTIVA")) {

            tasaEfectiva= CalculadoraTasaInteresEfectiva.convertirEfectivaAEfectivaDeAcuerdoALaFrecuenciaPago(calculoCronogramaDTO.getPorcentajeTasaInteres(),calculoCronogramaDTO.getPlazoTasaInteres(),calculoCronogramaDTO.getFrecuenciaPago());
        }else{
            //Si no es efectiva (osea es nominal), se convierte a efectiva
            //La tasa efectiva nominal debe ser pasada a una tasa efectiva de acuerdo a la frecuencia de pago
            tasaEfectiva= CalculadoraTasaInteresNominal.convertirATasaEfectivaDeAcuerdoALaFrecuenciaPago(calculoCronogramaDTO.getPlazoTasaInteres(),calculoCronogramaDTO.getPorcentajeTasaInteres(),calculoCronogramaDTO.getCapitalizacion(),calculoCronogramaDTO.getFrecuenciaPago());
        }


        //sacar tasa seguro desgravamen de acuerdo a la frecuencia de pago
        double tasaDesgravamen = CalculadoraSeguroDesgravamen.calcularTasaSeguroConFrecuenciaPago(calculoCronogramaDTO.getFrecuenciaPago(),calculoCronogramaDTO.getTiempoSeguroDesgravamen(),calculoCronogramaDTO.getPorcentajeSeguroDesgravamen());

        //sacar valor del seguro vehicular de acuerdo a la frecuencia de pago
        double seguroVehicular = CalculadoraSeguroVehicular.calcularTasaSeguroVehicularDadoFrecuenciaPago(calculoCronogramaDTO.getFrecuenciaPago(),calculoCronogramaDTO.getTiempoSeguroVehicular(),calculoCronogramaDTO.getPorcentajeSeguroVehicular());

        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //sacar cuota inicial
        double cuotaInicial = Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),calculoCronogramaDTO.getPorcentajeCuotaInicial());

        //sacar cuota final
        double cuotaFinal = Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),calculoCronogramaDTO.getPorcentajeCuotaFinal()) ;

        //calculamos cuotas totales
        double numeroCuotas= CalculadoraCuota.calcularNumeroCuotasTotales(calculoCronogramaDTO.getNumeroAnios(),calculoCronogramaDTO.getFrecuenciaPago());

        double montoPrestamo= montoAFinanciar;
        double amortizacion=0;
        double interes=0;
        double cuota= CalculadoraCuota.realizarCalculoCuotaMensual(montoPrestamo,tasaEfectiva,tasaDesgravamen, (int) numeroCuotas);
        double valorSeguroDesgravamen=0;

        double valorSeguroVehicular= Utilidades.redondear(CalculadoraSeguroVehicular.calculoSeguroVehicularDelVehiculo(calculoCronogramaDTO.getPrecioVehiculo(), seguroVehicular), 2);

        double cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales(),2);

        //Instanciamos lista de cuotas
        List<Cuota> listaCuotas = new ArrayList<>();

        //Cuotas del prestamo
        for(int cuotaActual=0;cuotaActual<=numeroCuotas;cuotaActual++){

            Cuota cuotaNueva = new Cuota();

            if(cuotaActual!=0){
                interes= Utilidades.redondear(montoPrestamo*(tasaEfectiva/100),2);
                valorSeguroDesgravamen=Utilidades.redondear(CalculadoraSeguroDesgravamen.calcularSeguroDesgravamenConPrestamo(montoPrestamo,tasaDesgravamen),2);
                amortizacion= Utilidades.redondear(cuotaTotal-interes-valorSeguroDesgravamen-valorSeguroVehicular-calculoCronogramaDTO.getPortes()-calculoCronogramaDTO.getCostosRegistrales()-calculoCronogramaDTO.getCostosNotariales(),2);
                montoPrestamo= Utilidades.redondear(montoPrestamo-amortizacion,2);
                String fechaPago= CalculadoraFechas.calcularFechaDePago(fechaInicio,cuotaActual,calculoCronogramaDTO.getFrecuenciaPago());

                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setAmortizacion(amortizacion);
                cuotaNueva.setInteres(interes);
                cuotaNueva.setSeguroDesgravamen(valorSeguroDesgravamen);
                cuotaNueva.setSeguroVehicular(valorSeguroVehicular);
                cuotaNueva.setPortes(calculoCronogramaDTO.getPortes());
                cuotaNueva.setCostosRegistrales(calculoCronogramaDTO.getCostosRegistrales());
                cuotaNueva.setCostosNotariales(calculoCronogramaDTO.getCostosNotariales());
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
        ultimaCuota.setPortes(calculoCronogramaDTO.getPortes());
        ultimaCuota.setCostosRegistrales(calculoCronogramaDTO.getCostosRegistrales());
        ultimaCuota.setCostosNotariales(calculoCronogramaDTO.getCostosNotariales());
        ultimaCuota.setCuotaTotal(cuotaFinal+amortizacion+interes+valorSeguroDesgravamen+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales());
        ultimaCuota.setFechaDePago(CalculadoraFechas.calcularFechaDePago(fechaInicio,(int) numeroCuotas+1, calculoCronogramaDTO.getFrecuenciaPago()));

        listaCuotas.add(ultimaCuota);

        return listaCuotas;
    }

    public static List<Cuota> obtenerListaCuotasMetodoConPlazoGraciaParcial(CalculoCronogramaDTO calculoCronogramaDTO){
        //Variables Intermedias:
        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(calculoCronogramaDTO.getPorcentajeCuotaInicial(),calculoCronogramaDTO.getPorcentajeCuotaFinal());

        String fechaInicio = calculoCronogramaDTO.getFechaInicio();

        //sacar tasa efectiva de acuerdo a la frecuencia de pago
        double tasaEfectiva;
        //Si la tasa es efectiva, solo se pasa a la frecuencia de pago
        if(calculoCronogramaDTO.getTipoTasaInteres().equalsIgnoreCase("EFECTIVA")) {

            tasaEfectiva= CalculadoraTasaInteresEfectiva.convertirEfectivaAEfectivaDeAcuerdoALaFrecuenciaPago(calculoCronogramaDTO.getPorcentajeTasaInteres(),calculoCronogramaDTO.getPlazoTasaInteres(),calculoCronogramaDTO.getFrecuenciaPago());
        }else{
            //Si no es efectiva (osea es nominal), se convierte a efectiva
            //La tasa efectiva nominal debe ser pasada a una tasa efectiva de acuerdo a la frecuencia de pago
            tasaEfectiva= CalculadoraTasaInteresNominal.convertirATasaEfectivaDeAcuerdoALaFrecuenciaPago(calculoCronogramaDTO.getPlazoTasaInteres(),calculoCronogramaDTO.getPorcentajeTasaInteres(),calculoCronogramaDTO.getCapitalizacion(),calculoCronogramaDTO.getFrecuenciaPago());
        }



        //sacar tasa seguro desgravamen de acuerdo a la frecuencia de pago
        double tasaDesgravamen = CalculadoraSeguroDesgravamen.calcularTasaSeguroConFrecuenciaPago(calculoCronogramaDTO.getFrecuenciaPago(),calculoCronogramaDTO.getTiempoSeguroDesgravamen(),calculoCronogramaDTO.getPorcentajeSeguroDesgravamen());

        //sacar valor del seguro vehicular de acuerdo a la frecuencia de pago
        double seguroVehicular = CalculadoraSeguroVehicular.calcularTasaSeguroVehicularDadoFrecuenciaPago(calculoCronogramaDTO.getFrecuenciaPago(),calculoCronogramaDTO.getTiempoSeguroVehicular(),calculoCronogramaDTO.getPorcentajeSeguroVehicular());

        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //sacar cuota inicial
        double cuotaInicial = Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),calculoCronogramaDTO.getPorcentajeCuotaInicial());

        //sacar cuota final
        double cuotaFinal = Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),calculoCronogramaDTO.getPorcentajeCuotaFinal()) ;

        //calculamos cuotas totales
        double numeroCuotas= CalculadoraCuota.calcularNumeroCuotasTotales(calculoCronogramaDTO.getNumeroAnios(), calculoCronogramaDTO.getFrecuenciaPago());
        //Calculamos cuotas de plazo de gracia
        double numeroCuotasParciales= (double) calculoCronogramaDTO.getTiempoPlazoDeGracia();

        double montoPrestamo= montoAFinanciar;
        double amortizacion=0;
        double interes=0;
        double cuota= 0;
        double valorSeguroDesgravamen=0;

        double valorSeguroVehicular= Utilidades.redondear(CalculadoraSeguroVehicular.calculoSeguroVehicularDelVehiculo(calculoCronogramaDTO.getPrecioVehiculo(), seguroVehicular), 2);

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
                    cuotaTotal=interes+valorSeguroDesgravamen;
                }else{
                    cuota= CalculadoraCuota.realizarCalculoCuotaMensualPlazoGraciaParcial(montoPrestamo,tasaEfectiva,tasaDesgravamen, (int) numeroCuotas,cuotaActual-1);
                    cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales(),2);
                    amortizacion= Utilidades.redondear(cuotaTotal-interes-valorSeguroDesgravamen-valorSeguroVehicular-calculoCronogramaDTO.getPortes()-calculoCronogramaDTO.getCostosRegistrales()-calculoCronogramaDTO.getCostosNotariales(),2);
                }

                montoPrestamo= Utilidades.redondear(montoPrestamo-amortizacion,2);

                String fechaPago= CalculadoraFechas.calcularFechaDePago(fechaInicio,cuotaActual,calculoCronogramaDTO.getFrecuenciaPago());

                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setAmortizacion(amortizacion);
                cuotaNueva.setInteres(interes);
                cuotaNueva.setSeguroDesgravamen(valorSeguroDesgravamen);
                cuotaNueva.setSeguroVehicular(valorSeguroVehicular);
                cuotaNueva.setPortes(calculoCronogramaDTO.getPortes());
                cuotaNueva.setCostosRegistrales(calculoCronogramaDTO.getCostosRegistrales());
                cuotaNueva.setCostosNotariales(calculoCronogramaDTO.getCostosNotariales());
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
        ultimaCuota.setPortes(calculoCronogramaDTO.getPortes());
        ultimaCuota.setCostosRegistrales(calculoCronogramaDTO.getCostosRegistrales());
        ultimaCuota.setCostosNotariales(calculoCronogramaDTO.getCostosNotariales());
        ultimaCuota.setCuotaTotal(cuotaFinal+amortizacion+interes+valorSeguroDesgravamen+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales());
        ultimaCuota.setFechaDePago(CalculadoraFechas.calcularFechaDePago(fechaInicio,(int) numeroCuotas+1, calculoCronogramaDTO.getFrecuenciaPago()));

        listaCuotas.add(ultimaCuota);

        return listaCuotas;
    }

    public static List<Cuota> obtenerListaCuotasMetodoConPlazoGraciaTotal(CalculoCronogramaDTO calculoCronogramaDTO){

        //Variables Intermedias:
        double porcentajePrestamoAFinanciar= Utilidades.calcularPorcentajePrestamoAFinanciar(calculoCronogramaDTO.getPorcentajeCuotaInicial(),calculoCronogramaDTO.getPorcentajeCuotaFinal());

        String fechaInicio = calculoCronogramaDTO.getFechaInicio();


        //sacar tasa efectiva de acuerdo a la frecuencia de pago
        double tasaEfectiva;
        //Si la tasa es efectiva, solo se pasa a la frecuencia de pago
        if(calculoCronogramaDTO.getTipoTasaInteres().equalsIgnoreCase("EFECTIVA")) {

            tasaEfectiva= CalculadoraTasaInteresEfectiva.convertirEfectivaAEfectivaDeAcuerdoALaFrecuenciaPago(calculoCronogramaDTO.getPorcentajeTasaInteres(),calculoCronogramaDTO.getPlazoTasaInteres(),calculoCronogramaDTO.getFrecuenciaPago());
        }else{
            //Si no es efectiva (osea es nominal), se convierte a efectiva
            //La tasa efectiva nominal debe ser pasada a una tasa efectiva de acuerdo a la frecuencia de pago
            tasaEfectiva= CalculadoraTasaInteresNominal.convertirATasaEfectivaDeAcuerdoALaFrecuenciaPago(calculoCronogramaDTO.getPlazoTasaInteres(),calculoCronogramaDTO.getPorcentajeTasaInteres(),calculoCronogramaDTO.getCapitalizacion(),calculoCronogramaDTO.getFrecuenciaPago());
        }

        //sacar tasa seguro desgravamen de acuerdo a la frecuencia de pago
        double tasaDesgravamen = CalculadoraSeguroDesgravamen.calcularTasaSeguroConFrecuenciaPago(calculoCronogramaDTO.getFrecuenciaPago(),calculoCronogramaDTO.getTiempoSeguroDesgravamen(),calculoCronogramaDTO.getPorcentajeSeguroDesgravamen());

        //sacar valor del seguro vehicular de acuerdo a la frecuencia de pago
        double seguroVehicular = CalculadoraSeguroVehicular.calcularTasaSeguroVehicularDadoFrecuenciaPago(calculoCronogramaDTO.getFrecuenciaPago(),calculoCronogramaDTO.getTiempoSeguroVehicular(),calculoCronogramaDTO.getPorcentajeSeguroVehicular());

        //sacar monto a financiar
        double montoAFinanciar= Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),porcentajePrestamoAFinanciar);

        //sacar cuota inicial
        double cuotaInicial = Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),calculoCronogramaDTO.getPorcentajeCuotaInicial());

        //sacar cuota final
        double cuotaFinal = Utilidades.calcularMontoAplicandoPorcentaje(calculoCronogramaDTO.getPrecioVehiculo(),calculoCronogramaDTO.getPorcentajeCuotaFinal()) ;

        //calculamos cuotas totales
        double numeroCuotas= CalculadoraCuota.calcularNumeroCuotasTotales(calculoCronogramaDTO.getNumeroAnios(),calculoCronogramaDTO.getFrecuenciaPago());
        //Calculamos cuotas de plazo de gracia
        double numeroCuotasPlazoTotal= (double) calculoCronogramaDTO.getTiempoPlazoDeGracia();

        double montoPrestamo= montoAFinanciar;
        double amortizacion=0;
        double interes=0;
        double cuota= 0;
        double valorSeguroDesgravamen=0;

        double valorSeguroVehicular= Utilidades.redondear(CalculadoraSeguroVehicular.calculoSeguroVehicularDelVehiculo(calculoCronogramaDTO.getPrecioVehiculo(), seguroVehicular), 2);

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
                    cuotaTotal=valorSeguroDesgravamen;
                }else{
                    cuota= CalculadoraCuota.realizarCalculoCuotaMensualPlazoGraciaParcial(montoPrestamo,tasaEfectiva,tasaDesgravamen, (int) numeroCuotas,cuotaActual-1);
                    cuotaTotal=Utilidades.redondear(cuota+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales(),2);
                    amortizacion= Utilidades.redondear(cuotaTotal-interes-valorSeguroDesgravamen-valorSeguroVehicular-calculoCronogramaDTO.getPortes()-calculoCronogramaDTO.getCostosRegistrales()-calculoCronogramaDTO.getCostosNotariales(),2);
                }

                montoPrestamo= Utilidades.redondear(montoPrestamo-amortizacion,2);

                String fechaPago= CalculadoraFechas.calcularFechaDePago(fechaInicio,cuotaActual,calculoCronogramaDTO.getFrecuenciaPago());

                cuotaNueva.setMontoDelPrestamo(montoPrestamo);
                cuotaNueva.setNumeroDeCuota(cuotaActual);
                cuotaNueva.setAmortizacion(amortizacion);
                cuotaNueva.setInteres(interes);
                cuotaNueva.setSeguroDesgravamen(valorSeguroDesgravamen);
                cuotaNueva.setSeguroVehicular(valorSeguroVehicular);
                cuotaNueva.setPortes(calculoCronogramaDTO.getPortes());
                cuotaNueva.setCostosRegistrales(calculoCronogramaDTO.getCostosRegistrales());
                cuotaNueva.setCostosNotariales(calculoCronogramaDTO.getCostosNotariales());
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
        ultimaCuota.setPortes(calculoCronogramaDTO.getPortes());
        ultimaCuota.setCostosRegistrales(calculoCronogramaDTO.getCostosRegistrales());
        ultimaCuota.setCostosNotariales(calculoCronogramaDTO.getCostosNotariales());
        ultimaCuota.setCuotaTotal(cuotaFinal+amortizacion+interes+valorSeguroDesgravamen+valorSeguroVehicular+calculoCronogramaDTO.getPortes()+calculoCronogramaDTO.getCostosRegistrales()+calculoCronogramaDTO.getCostosNotariales());
        ultimaCuota.setFechaDePago(CalculadoraFechas.calcularFechaDePago(fechaInicio,(int) numeroCuotas+1, calculoCronogramaDTO.getFrecuenciaPago()));

        listaCuotas.add(ultimaCuota);

        return listaCuotas;
    }

}
