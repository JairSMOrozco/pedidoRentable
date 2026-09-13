package com.example.pedidorentable;

public class EstimacionDuracion {

    public double duracionPromedio;
    public int cantidadMuestras;

    public EstimacionDuracion(
            double duracionPromedio,
            int cantidadMuestras
    ) {
        this.duracionPromedio = duracionPromedio;
        this.cantidadMuestras = cantidadMuestras;
    }
}