package com.upc.crediApp.DataInitializer;

import com.upc.crediApp.model.TasaInteres;
import com.upc.crediApp.repository.TasaInteresRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer {
    private final TasaInteresRepository tasaInteresRepository;

    @Autowired
    public DataInitializer(TasaInteresRepository tasaInteresRepository) {
        this.tasaInteresRepository = tasaInteresRepository;
    }

    @PostConstruct
    public void initData() {
        List<TasaInteres> tasas = Arrays.asList(
                new TasaInteres(1L,"EFECTIVA", "DIARIA", "TED"),
                new TasaInteres(2L,"EFECTIVA", "MENSUAL", "TEM"),
                new TasaInteres(3L,"EFECTIVA", "BIMESTRAL", "TEB"),
                new TasaInteres(4L,"EFECTIVA", "TRIMESTRAL", "TET"),
                new TasaInteres(5L,"EFECTIVA", "CUATRIMESTRAL", "TEC"),
                new TasaInteres(6L,"EFECTIVA", "SEMESTRAL", "TES"),
                new TasaInteres(7L,"EFECTIVA", "ANUAL", "TEA"),
                new TasaInteres(8L,"NOMINAL", "DIARIA", "TND"),
                new TasaInteres(9L,"NOMINAL", "MENSUAL", "TNM"),
                new TasaInteres(10L,"NOMINAL", "BIMESTRAL", "TNB"),
                new TasaInteres(11L,"NOMINAL", "TRIMESTRAL", "TNT"),
                new TasaInteres(12L,"NOMINAL", "CUATRIMESTRAL", "TNC"),
                new TasaInteres(13L,"NOMINAL", "SEMESTRAL", "TNS"),
                new TasaInteres(14L,"NOMINAL", "ANUAL", "TNA")
        );

        tasaInteresRepository.saveAll(tasas);
    }
}
