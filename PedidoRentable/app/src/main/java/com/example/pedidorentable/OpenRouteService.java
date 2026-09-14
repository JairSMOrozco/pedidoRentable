package com.example.pedidorentable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class OpenRouteService {

    public interface Callback {

        void onResultado(
                double distanciaKm,
                double duracionMin
        );

        void onError(String mensaje);
    }

    public static void calcularRuta(
            double latitudOrigen,
            double longitudOrigen,
            double latitudDestino,
            double longitudDestino,
            String perfil,
            Callback callback
    ) {

        new Thread(() -> {

            try {

                String coordenadasInicio =
                        String.format(
                                Locale.US,
                                "%f,%f",
                                longitudOrigen,
                                latitudOrigen
                        );

                String coordenadasFin =
                        String.format(
                                Locale.US,
                                "%f,%f",
                                longitudDestino,
                                latitudDestino
                        );

                String urlTexto =
                        "https://api.heigit.org/openrouteservice/v2/directions/"
                                + perfil
                                + "?api_key="
                                + BuildConfig.ORS_API_KEY
                                + "&start="
                                + coordenadasInicio
                                + "&end="
                                + coordenadasFin;

                URL url = new URL(urlTexto);

                HttpURLConnection conexion =
                        (HttpURLConnection) url.openConnection();

                conexion.setRequestMethod("GET");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);

                int codigoRespuesta =
                        conexion.getResponseCode();

                if (codigoRespuesta != 200) {

                    callback.onError(
                            "Error HTTP: " + codigoRespuesta
                    );

                    conexion.disconnect();
                    return;
                }

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        conexion.getInputStream()
                                )
                        );

                StringBuilder respuesta =
                        new StringBuilder();

                String linea;

                while ((linea = reader.readLine()) != null) {
                    respuesta.append(linea);
                }

                reader.close();
                conexion.disconnect();

                JSONObject json =
                        new JSONObject(
                                respuesta.toString()
                        );

                JSONArray features =
                        json.getJSONArray("features");

                if (features.length() == 0) {

                    callback.onError(
                            "No se encontró una ruta"
                    );

                    return;
                }

                JSONObject properties =
                        features
                                .getJSONObject(0)
                                .getJSONObject("properties");

                JSONObject summary =
                        properties
                                .getJSONObject("summary");

                double distanciaMetros =
                        summary.getDouble("distance");

                double duracionSegundos =
                        summary.getDouble("duration");

                double distanciaKm =
                        distanciaMetros / 1000.0;

                double duracionMin =
                        duracionSegundos / 60.0;

                callback.onResultado(
                        distanciaKm,
                        duracionMin
                );

            } catch (Exception e) {

                callback.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Error desconocido"
                );
            }

        }).start();
    }
}