package com.upc.crediApp.service.impl;

import com.upc.crediApp.dto.TasaInteresDTO;
import com.upc.crediApp.exception.ValidationException;
import com.upc.crediApp.model.TasaInteres;
import com.upc.crediApp.repository.TasaInteresRepository;
import com.upc.crediApp.service.inter.TasaInteresService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Service
public class TasaInteresServiceImpl implements TasaInteresService {

    @Autowired
    private TasaInteresRepository tasaInteresRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TasaInteresServiceImpl(){
        this.modelMapper = new ModelMapper();
    }

    private TasaInteresDTO EntityToDto(TasaInteres tasaInteres){
        return modelMapper.map(tasaInteres, TasaInteresDTO.class);
    }

    private TasaInteres DtoToEntity(TasaInteresDTO tasaInteresDTO){
        return modelMapper.map(tasaInteresDTO, TasaInteres.class);
    }

    @Override
    public List<TasaInteres> getAllTasaInteres() {

        return tasaInteresRepository.findAll();
    }

    @Override
    public TasaInteres createTasaInteres(TasaInteresDTO tasaInteresDTO) {
        validationTasaInteres(tasaInteresDTO);
        return tasaInteresRepository.save(DtoToEntity(tasaInteresDTO));
    }

    @Override
    public void deleteTasaInteres(Long id) {
        tasaInteresRepository.deleteById(id);
    }

    private void validationTasaInteres(TasaInteresDTO tasaInteresDTO){

        if (tasaInteresDTO.tipo == null || tasaInteresDTO.tipo.isEmpty()) {
            throw new ValidationException("El tipo de tasa de interes no puede ser nulo o vacio");
        }
        if (tasaInteresDTO.plazo == null || tasaInteresDTO.plazo.isEmpty()) {
            throw new ValidationException("El plazo de tasa de interes no puede ser nulo o vacio");
        }
        if (tasaInteresDTO.abreviatura == null || tasaInteresDTO.abreviatura.isEmpty()) {
            throw new ValidationException("La abreviatura de tasa de interes no puede ser nulo o vacio");
        }

    }
}
