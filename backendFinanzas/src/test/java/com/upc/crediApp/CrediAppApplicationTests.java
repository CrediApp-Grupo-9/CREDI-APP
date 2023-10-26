package com.upc.crediApp;

import com.upc.crediApp.helpers.CalculadoraTasaInteresNominal;
import com.upc.crediApp.helpers.Utilidades;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CrediAppApplicationTests {

	@Test
	void Test1() {
		double tasaEfectivaMensual= CalculadoraTasaInteresNominal.convertirATasaEfectivaMensual("Bimestral",1.4,"Anual");
		System.out.println(tasaEfectivaMensual);
	}

}
