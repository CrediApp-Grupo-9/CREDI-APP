package com.upc.crediApp.service.inter;

import com.upc.crediApp.dto.TasaInteresDTO;
import com.upc.crediApp.model.TasaInteres;

import java.util.List;

public interface TasaInteresService {

    List<TasaInteres> getAllTasaInteres();

     TasaInteres createTasaInteres(TasaInteresDTO tasaInteres);

        void deleteTasaInteres(Long id);

}
