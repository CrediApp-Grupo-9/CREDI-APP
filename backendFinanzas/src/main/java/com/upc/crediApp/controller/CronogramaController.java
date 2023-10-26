package com.upc.crediApp.controller;

import com.upc.crediApp.dto.CalculoCronogramaDTO;
import com.upc.crediApp.model.Cronograma;
import com.upc.crediApp.service.inter.CronogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crediApp/v1/cronograma")
public class CronogramaController {

    @Autowired
    private CronogramaService cronogramaService;

    //URL: http://localhost:8080/api/crediApp/v1/cronograma
    //Method: GET
    @Transactional(readOnly = true)
    @PostMapping("/calculoCronogramaSinPlazoDeGracia")
    public ResponseEntity<Cronograma> calcularCronogramaSinPlazoDeGracia(@RequestBody CalculoCronogramaDTO calculoCronogramaDTO){
        return new ResponseEntity<>(cronogramaService.calculoCronogramaSinPlazoDeGracia(calculoCronogramaDTO),org.springframework.http.HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    @PostMapping("/calculoCronogramaConPlazoDeGraciaParcial")
    public ResponseEntity<Cronograma> calcularCronogramaConPlazoDeGraciaParcial(@RequestBody CalculoCronogramaDTO calculoCronogramaDTO){
        return new ResponseEntity<>(cronogramaService.calculoCronogramaConPlazoDeGraciaParcial(calculoCronogramaDTO),org.springframework.http.HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    @PostMapping("/calculoCronogramaConPlazoDeGraciaTotal")
    public ResponseEntity<Cronograma> calcularCronogramaConPlazoDeGraciaTotal(@RequestBody CalculoCronogramaDTO calculoCronogramaDTO){
        return new ResponseEntity<>(cronogramaService.calculoCronogramaConPlazoDeGraciaTotal(calculoCronogramaDTO),org.springframework.http.HttpStatus.OK);
    }
}
