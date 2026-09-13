package com.example.pedidorentable;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuracion {

    private static final String PREFS = "configuracion_app";

    private static final String META = "meta_diaria";
    private static final String IMPUESTO = "impuesto";
    private static final String DISTANCIA_TIENDA = "distancia_tienda";
    private static final String MINIMO_KM = "minimo_km";
    private static final String HORA_INICIO = "hora_inicio";
    private static final String HORA_FIN = "hora_fin";

    private final SharedPreferences prefs;

    public Configuracion(Context context) {
        prefs = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
        );
    }

    public double getMetaDiaria() {
        return Double.longBitsToDouble(
                prefs.getLong(
                        META,
                        Double.doubleToLongBits(400.0)
                )
        );
    }

    public double getImpuesto() {
        return Double.longBitsToDouble(
                prefs.getLong(
                        IMPUESTO,
                        Double.doubleToLongBits(10.1)
                )
        );
    }

    public double getDistanciaTienda() {
        return Double.longBitsToDouble(
                prefs.getLong(
                        DISTANCIA_TIENDA,
                        Double.doubleToLongBits(2.0)
                )
        );
    }

    public double getMinimoKm() {
        return Double.longBitsToDouble(
                prefs.getLong(
                        MINIMO_KM,
                        Double.doubleToLongBits(7.0)
                )
        );
    }

    public int getHoraInicio() {
        return prefs.getInt(HORA_INICIO, 9);
    }

    public int getHoraFin() {
        return prefs.getInt(HORA_FIN, 16);
    }

    public void guardar(
            double meta,
            double impuesto,
            double distanciaTienda,
            double minimoKm,
            int horaInicio,
            int horaFin
    ) {

        prefs.edit()
                .putLong(
                        META,
                        Double.doubleToRawLongBits(meta)
                )
                .putLong(
                        IMPUESTO,
                        Double.doubleToRawLongBits(impuesto)
                )
                .putLong(
                        DISTANCIA_TIENDA,
                        Double.doubleToRawLongBits(distanciaTienda)
                )
                .putLong(
                        MINIMO_KM,
                        Double.doubleToRawLongBits(minimoKm)
                )
                .putInt(HORA_INICIO, horaInicio)
                .putInt(HORA_FIN, horaFin)
                .apply();
    }
}