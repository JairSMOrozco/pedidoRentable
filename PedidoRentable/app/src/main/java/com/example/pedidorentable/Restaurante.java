package com.example.pedidorentable;

public class Restaurante {

    private long id;
    private String nombre;
    private double latitud;
    private double longitud;
    private String fechaAlta;

    public Restaurante(
            long id,
            String nombre,
            double latitud,
            double longitud,
            String fechaAlta
    ) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaAlta = fechaAlta;
    }

    public long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    @Override
    public String toString() {
        return nombre;
    }
}