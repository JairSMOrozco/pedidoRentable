package com.example.pedidorentable;

public class ResumenDia {

    public String fecha;
    public int pedidos;
    public int rechazados;
    public double ganancia;
    public double duracionPromedio;
    public double promedioPedido;

    public ResumenDia(
            String fecha,
            int pedidos,
            int rechazados,
            double ganancia,
            double duracionPromedio,
            double promedioPedido
    ) {
        this.fecha = fecha;
        this.pedidos = pedidos;
        this.rechazados = rechazados;
        this.ganancia = ganancia;
        this.duracionPromedio = duracionPromedio;
        this.promedioPedido = promedioPedido;
    }
}