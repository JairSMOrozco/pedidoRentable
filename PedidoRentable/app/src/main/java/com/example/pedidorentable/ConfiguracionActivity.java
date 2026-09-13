package com.example.pedidorentable;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfiguracionActivity extends AppCompatActivity {

    private EditText etMeta;
    private EditText etImpuesto;
    private EditText etDistanciaTienda;
    private EditText etMinimoKm;
    private EditText etHoraInicio;
    private EditText etHoraFin;

    private Configuracion configuracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        configuracion = new Configuracion(this);

        etMeta = findViewById(R.id.etMeta);
        etImpuesto = findViewById(R.id.etImpuesto);
        etDistanciaTienda =
                findViewById(R.id.etDistanciaTienda);
        etMinimoKm =
                findViewById(R.id.etMinimoKm);
        etHoraInicio =
                findViewById(R.id.etHoraInicio);
        etHoraFin =
                findViewById(R.id.etHoraFin);

        Button btnGuardar =
                findViewById(
                        R.id.btnGuardarConfiguracion
                );

        cargarConfiguracion();

        btnGuardar.setOnClickListener(
                v -> guardarConfiguracion()
        );
    }

    private void cargarConfiguracion() {

        etMeta.setText(
                String.valueOf(
                        configuracion.getMetaDiaria()
                )
        );

        etImpuesto.setText(
                String.valueOf(
                        configuracion.getImpuesto()
                )
        );

        etDistanciaTienda.setText(
                String.valueOf(
                        configuracion.getDistanciaTienda()
                )
        );

        etMinimoKm.setText(
                String.valueOf(
                        configuracion.getMinimoKm()
                )
        );

        etHoraInicio.setText(
                String.valueOf(
                        configuracion.getHoraInicio()
                )
        );

        etHoraFin.setText(
                String.valueOf(
                        configuracion.getHoraFin()
                )
        );
    }

    private void guardarConfiguracion() {

        try {

            double meta =
                    Double.parseDouble(
                            etMeta.getText()
                                    .toString()
                                    .trim()
                    );

            double impuesto =
                    Double.parseDouble(
                            etImpuesto.getText()
                                    .toString()
                                    .trim()
                    );

            double distancia =
                    Double.parseDouble(
                            etDistanciaTienda.getText()
                                    .toString()
                                    .trim()
                    );

            double minimoKm =
                    Double.parseDouble(
                            etMinimoKm.getText()
                                    .toString()
                                    .trim()
                    );

            int horaInicio =
                    Integer.parseInt(
                            etHoraInicio.getText()
                                    .toString()
                                    .trim()
                    );

            int horaFin =
                    Integer.parseInt(
                            etHoraFin.getText()
                                    .toString()
                                    .trim()
                    );

            if (meta <= 0
                    || impuesto < 0
                    || impuesto >= 100
                    || distancia <= 0
                    || minimoKm <= 0
                    || horaInicio < 0
                    || horaInicio > 23
                    || horaFin < 1
                    || horaFin > 23
                    || horaFin <= horaInicio) {

                Toast.makeText(
                        this,
                        "Revisa los valores ingresados",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            configuracion.guardar(
                    meta,
                    impuesto,
                    distancia,
                    minimoKm,
                    horaInicio,
                    horaFin
            );

            Toast.makeText(
                    this,
                    "Configuración guardada",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Completa todos los campos correctamente",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}