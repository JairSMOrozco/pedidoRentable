package com.example.pedidorentable;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class HistorialActivity extends AppCompatActivity {

    private LinearLayout contenedorHistorial;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        contenedorHistorial =
                findViewById(R.id.contenedorHistorial);

        databaseHelper =
                new DatabaseHelper(this);

        cargarHistorial();
    }

    private void cargarHistorial() {

        contenedorHistorial.removeAllViews();

        List<ResumenDia> historial =
                databaseHelper.obtenerResumenPorDias();

        if (historial.isEmpty()) {

            TextView vacio =
                    new TextView(this);

            vacio.setText(
                    "Todavía no hay jornadas registradas."
            );

            vacio.setTextSize(16);

            contenedorHistorial.addView(vacio);

            return;
        }

        for (ResumenDia dia : historial) {

            TextView tarjeta =
                    new TextView(this);

            String texto =
                    dia.fecha

                            + "\nGanancia: $"
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            dia.ganancia
                    )

                            + "\nPedidos: "
                            + dia.pedidos

                            + "   |   Rechazados: "
                            + dia.rechazados

                            + "\nPromedio/pedido: $"
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            dia.promedioPedido
                    )

                            + "\nDuración promedio: "
                            + String.format(
                            Locale.getDefault(),
                            "%.1f",
                            dia.duracionPromedio
                    )
                            + " min";

            tarjeta.setText(texto);
            tarjeta.setTextSize(16);
            tarjeta.setPadding(
                    24,
                    20,
                    24,
                    20
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(
                    0,
                    0,
                    0,
                    20
            );

            tarjeta.setLayoutParams(params);

            contenedorHistorial.addView(tarjeta);
        }
    }
}