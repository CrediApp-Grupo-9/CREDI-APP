package com.upc.crediApp.controller;

import com.upc.crediApp.dto.DatosEntradaCronograma;
import com.upc.crediApp.model.Cronograma;
import com.upc.crediApp.service.inter.CronogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crediApp/v1/cronograma")
public class CronogramaController {

    @Autowired
    private CronogramaService cronogramaService;

    //URL: http://localhost:8080/api/crediApp/v1/cronograma
    //Method: GET

    @Transactional
    @GetMapping("/{customerId}/cronograma")
    public ResponseEntity<List<Cronograma>> getAllCronogramasByCustomerId(@PathVariable Long customerId){
        return new ResponseEntity<>(cronogramaService.getAllCronogramasByCustomerId(customerId), org.springframework.http.HttpStatus.OK);
    }
    @PostMapping("/{customerId}/calculoCronograma")
    public ResponseEntity<Cronograma> calcularCronograma(@PathVariable Long customerId, @RequestBody DatosEntradaCronograma datosEntradaCronograma){
        return new ResponseEntity<>(cronogramaService.saveCronograma(customerId,datosEntradaCronograma),org.springframework.http.HttpStatus.OK);
    }


}
