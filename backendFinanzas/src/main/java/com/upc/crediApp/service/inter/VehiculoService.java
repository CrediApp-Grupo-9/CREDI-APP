package com.upc.crediApp.service.inter;

import com.upc.crediApp.model.Vehiculo;

import java.util.List;

public interface VehiculoService {

    List<Vehiculo> getAllVehiculos();
    Vehiculo createVehiculo(Vehiculo vehiculo);
    void deleteVehiculo(Long id);

}
