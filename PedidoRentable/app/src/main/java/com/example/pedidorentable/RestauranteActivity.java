package com.example.pedidorentable;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.widget.LinearLayout;

public class RestauranteActivity extends AppCompatActivity {

    private static final int PERMISO_UBICACION = 100;

    private EditText etNombreRestaurante;
    private TextView tvEstadoUbicacion;
    private Button btnGuardarRestaurante;



    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private LinearLayout contenedorRestaurantes;

    private String nombrePendiente = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurante);

        databaseHelper = new DatabaseHelper(this);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        etNombreRestaurante =
                findViewById(R.id.etNombreRestaurante);

        tvEstadoUbicacion =
                findViewById(R.id.tvEstadoUbicacion);

        btnGuardarRestaurante =
                findViewById(R.id.btnGuardarRestaurante);

        contenedorRestaurantes =
                findViewById(R.id.contenedorRestaurantes);

        btnGuardarRestaurante.setOnClickListener(
                v -> iniciarGuardadoRestaurante()
        );

        mostrarRestaurantes();

    }

    private void iniciarGuardadoRestaurante() {

        String nombre =
                etNombreRestaurante
                        .getText()
                        .toString()
                        .trim();

        if (nombre.isEmpty()) {

            Toast.makeText(
                    this,
                    "Escribe el nombre del restaurante",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        nombrePendiente = nombre;

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISO_UBICACION
            );

            return;
        }

        obtenerUbicacionYGuardar();
    }

    private void obtenerUbicacionYGuardar() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        tvEstadoUbicacion.setText(
                "Obteniendo ubicación..."
        );

        btnGuardarRestaurante.setEnabled(false);

        CancellationTokenSource cancellationTokenSource =
                new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.getToken()
        ).addOnSuccessListener(location -> {

            btnGuardarRestaurante.setEnabled(true);

            if (location == null) {

                tvEstadoUbicacion.setText(
                        "No se pudo obtener la ubicación"
                );

                Toast.makeText(
                        this,
                        "Activa la ubicación e intenta nuevamente",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            guardarRestaurante(location);

        }).addOnFailureListener(e -> {

            btnGuardarRestaurante.setEnabled(true);

            tvEstadoUbicacion.setText(
                    "Error al obtener ubicación"
            );

            Toast.makeText(
                    this,
                    "No se pudo obtener la ubicación",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void guardarRestaurante(Location location) {

        double latitud = location.getLatitude();
        double longitud = location.getLongitude();

        String fechaAlta =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

        long id =
                databaseHelper.insertarRestaurante(
                        nombrePendiente,
                        latitud,
                        longitud,
                        fechaAlta
                );

        if (id != -1) {

            tvEstadoUbicacion.setText(
                    "Ubicación guardada correctamente"
            );

            Toast.makeText(
                    this,
                    "Restaurante guardado",
                    Toast.LENGTH_SHORT
            ).show();

            etNombreRestaurante.setText("");
            etNombreRestaurante.requestFocus();

            nombrePendiente = "";
            mostrarRestaurantes();

        } else {

            tvEstadoUbicacion.setText(
                    "No se pudo guardar el restaurante"
            );

            Toast.makeText(
                    this,
                    "No se pudo guardar",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISO_UBICACION) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                obtenerUbicacionYGuardar();

            } else {

                Toast.makeText(
                        this,
                        "Se necesita permiso de ubicación",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void mostrarRestaurantes() {

        contenedorRestaurantes.removeAllViews();

        java.util.List<Restaurante> restaurantes =
                databaseHelper.obtenerRestaurantes();

        if (restaurantes.isEmpty()) {

            TextView tvVacio = new TextView(this);

            tvVacio.setText("No hay restaurantes registrados");
            tvVacio.setPadding(0, 12, 0, 12);

            contenedorRestaurantes.addView(tvVacio);

            return;
        }

        for (Restaurante restaurante : restaurantes) {

            TextView tvRestaurante = new TextView(this);

            tvRestaurante.setText(restaurante.getNombre());
            tvRestaurante.setTextSize(18);
            tvRestaurante.setPadding(0, 16, 0, 16);

            tvRestaurante.setOnClickListener(
                    v -> probarRutaRestaurante(restaurante)
            );

            contenedorRestaurantes.addView(tvRestaurante);
        }
    }

    private void probarRutaRestaurante(Restaurante restaurante) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    this,
                    "Se necesita permiso de ubicación",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        tvEstadoUbicacion.setText(
                "Calculando ruta a " + restaurante.getNombre() + "..."
        );

        CancellationTokenSource cancellationTokenSource =
                new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.getToken()
        ).addOnSuccessListener(location -> {

            if (location == null) {

                tvEstadoUbicacion.setText(
                        "No se pudo obtener la ubicación actual"
                );

                return;
            }

            OpenRouteService.calcularRuta(
                    location.getLatitude(),
                    location.getLongitude(),

                    restaurante.getLatitud(),
                    restaurante.getLongitud(),

                    "cycling-regular",

                    new OpenRouteService.Callback() {

                        @Override
                        public void onResultado(
                                double distanciaKm,
                                double duracionMin
                        ) {

                            runOnUiThread(() -> {

                                String resultado =
                                        String.format(
                                                Locale.getDefault(),
                                                "%s: %.2f km - %.1f min",
                                                restaurante.getNombre(),
                                                distanciaKm,
                                                duracionMin
                                        );

                                tvEstadoUbicacion.setText(resultado);

                                Toast.makeText(
                                        RestauranteActivity.this,
                                        resultado,
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                        }

                        @Override
                        public void onError(String mensaje) {

                            runOnUiThread(() -> {

                                tvEstadoUbicacion.setText(
                                        "Error al calcular ruta"
                                );

                                Toast.makeText(
                                        RestauranteActivity.this,
                                        mensaje,
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                        }
                    }
            );

        }).addOnFailureListener(e -> {

            tvEstadoUbicacion.setText(
                    "Error al obtener ubicación"
            );
        });
    }
}