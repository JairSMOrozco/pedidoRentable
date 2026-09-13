package com.example.pedidorentable;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.Calendar;

import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private EditText etGanancia;
    private EditText etDistanciaCliente;

    private TextView tvResultado;
    private TextView tvDetalles;

    private TextView tvGananciaDia;
    private TextView tvProgresoMeta;

    private TextView tvRitmoNecesario;

    private TextView tvEstadisticasDia;

    private TextView tvDistanciaTienda;

    private Button btnAnalizar;
    private Button btnAceptar;
    private Button btnRechazar;
    private Button btnFinalizar;

    private Button btnHistorial;

    private Button btnConfiguracion;

    private Button btnCancelarPedido;

    private Button btnCerrarJornada;

    /*private Button btnBorrarPruebas;*/



    private DatabaseHelper databaseHelper;
    private Configuracion configuracion;


    private static final double GANANCIA_MINIMA = 1.0;
    private static final double DISTANCIA_MINIMA = 0.1;
    private static final int MIN_MUESTRAS_HISTORICAS = 3;
    private static final double DURACION_MAX_ACEPTABLE = 40.0;
    private static final double RITMO_MAXIMO_EXIGIBLE = 90.0;

    // Datos del pedido analizado
    private double gananciaMostradaActual = 0;
    private double gananciaNetaActual = 0;
    private double distanciaClienteActual = 0;
    private double distanciaTotalActual = 0;
    private double gananciaKmActual = 0;

    // Pedido en curso
    private long pedidoActualId = -1;
    private long tiempoInicioMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        configuracion = new Configuracion(this);

        etGanancia = findViewById(R.id.etGanancia);
        etDistanciaCliente =
                findViewById(R.id.etDistanciaCliente);

        tvResultado =
                findViewById(R.id.tvResultado);

        tvDetalles =
                findViewById(R.id.tvDetalles);

        tvGananciaDia =
                findViewById(R.id.tvGananciaDia);

        tvProgresoMeta =
                findViewById(R.id.tvProgresoMeta);

        tvRitmoNecesario =
                findViewById(R.id.tvRitmoNecesario);

        tvEstadisticasDia =
                findViewById(R.id.tvEstadisticasDia);

        tvDistanciaTienda =
                findViewById(R.id.tvDistanciaTienda);

        btnAnalizar =
                findViewById(R.id.btnAnalizar);

        btnAceptar =
                findViewById(R.id.btnAceptar);

        btnRechazar =
                findViewById(R.id.btnRechazar);

        btnFinalizar =
                findViewById(R.id.btnFinalizar);

        btnCancelarPedido =
                findViewById(R.id.btnCancelarPedido);

        btnHistorial =
                findViewById(R.id.btnHistorial);

        btnConfiguracion =
                findViewById(R.id.btnConfiguracion);

        btnCerrarJornada =
                findViewById(R.id.btnCerrarJornada);




        btnAnalizar.setOnClickListener(
                v -> analizarPedido()
        );

        btnAceptar.setOnClickListener(
                v -> aceptarPedido()
        );

        btnRechazar.setOnClickListener(
                v -> rechazarPedido()
        );

        btnFinalizar.setOnClickListener(
                v -> finalizarPedido()
        );

        btnCancelarPedido.setOnClickListener(
                v -> cancelarPedidoEnCurso()
        );

        btnHistorial.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HistorialActivity.class
                    );

            startActivity(intent);
        });

        btnConfiguracion.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            ConfiguracionActivity.class
                    );

            startActivity(intent);
        });

        btnCerrarJornada.setOnClickListener(
                v -> mostrarCierreJornada()
        );

    /*
        btnBorrarPruebas =
                findViewById(R.id.btnBorrarPruebas);

        btnBorrarPruebas.setOnClickListener(v -> {

            if (pedidoActualId != -1
                    || databaseHelper.existePedidoEnCurso()) {

                Toast.makeText(
                        this,
                        "Finaliza o cancela el pedido en curso primero",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Borrar datos de prueba")
                    .setMessage(
                            "Se eliminarán todos los pedidos de prueba."
                    )
                    .setNegativeButton("CANCELAR", null)
                    .setPositiveButton(
                            "BORRAR",
                            (dialog, which) -> {

                                databaseHelper.eliminarTodosLosPedidos();

                                actualizarGananciaDia();
                                actualizarEstadisticasDia();
                                limpiarParaNuevoPedido();

                                Toast.makeText(
                                        this,
                                        "Datos de prueba eliminados",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                    )
                    .show();
        });
    */


        etDistanciaCliente.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId
                            == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                        analizarPedido();
                        return true;
                    }

                    return false;
                }
        );


        /*
         * MUY IMPORTANTE:
         * Cada vez que abrimos la app comprobamos SQLite.
         */
        recuperarPedidoEnCurso();
        actualizarGananciaDia();
        actualizarEstadisticasDia();
        etGanancia.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null
                && configuracion != null) {

            actualizarConfiguracionVisual();
            actualizarGananciaDia();
            actualizarEstadisticasDia();
        }
    }

    private void analizarPedido() {

        if (pedidoActualId != -1) {
            Toast.makeText(
                    this,
                    "Ya hay un pedido en curso",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String gananciaTexto =
                etGanancia.getText().toString().trim();

        String distanciaTexto =
                etDistanciaCliente.getText().toString().trim();

        if (gananciaTexto.isEmpty()
                || distanciaTexto.isEmpty()) {

            Toast.makeText(
                    this,
                    "Ingresa ganancia y distancia",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            double gananciaMostrada =
                    Double.parseDouble(gananciaTexto);

            double distanciaCliente =
                    Double.parseDouble(distanciaTexto);

            if (gananciaMostrada < GANANCIA_MINIMA) {

                Toast.makeText(
                        this,
                        "La ganancia no es válida",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (distanciaCliente < DISTANCIA_MINIMA) {

                Toast.makeText(
                        this,
                        "La distancia no es válida",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            double impuestoPorcentaje =
                    configuracion.getImpuesto();

            double impuestoDecimal =
                    impuestoPorcentaje / 100.0;

            double distanciaTienda =
                    configuracion.getDistanciaTienda();

            double minimoPorKm =
                    configuracion.getMinimoKm();

            double gananciaNeta =
                    gananciaMostrada
                            * (1 - impuestoDecimal);

            double distanciaTotal =
                    distanciaTienda
                            + distanciaCliente;

            double gananciaPorKm =
                    gananciaNeta
                            / distanciaTotal;

            if (distanciaTotal <= 0) {
                Toast.makeText(
                        this,
                        "Distancia inválida",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            gananciaMostradaActual =
                    gananciaMostrada;

            gananciaNetaActual =
                    gananciaNeta;

            distanciaClienteActual =
                    distanciaCliente;

            distanciaTotalActual =
                    distanciaTotal;

            gananciaKmActual =
                    gananciaPorKm;

            EstimacionDuracion estimacion =
                    databaseHelper.obtenerEstimacionDuracion(
                            distanciaTotal
                    );

            double ritmoNecesario =
                    obtenerRitmoNecesario();

            boolean historialSuficiente =
                    estimacion.cantidadMuestras
                            >= MIN_MUESTRAS_HISTORICAS;

            boolean cumpleKm =
                    gananciaPorKm >= minimoPorKm;

            boolean cumpleTiempo = true;
            boolean cumpleRitmo = true;

            if (historialSuficiente
                    && estimacion.duracionPromedio > 0) {

                cumpleTiempo =
                        estimacion.duracionPromedio
                                <= DURACION_MAX_ACEPTABLE;

                double gananciaHoraEstimada =
                        gananciaNeta /
                                (estimacion.duracionPromedio / 60.0);

                if (ritmoNecesario > 0) {

                    double ritmoObjetivo =
                            Math.min(
                                    ritmoNecesario,
                                    RITMO_MAXIMO_EXIGIBLE
                            );

                    cumpleRitmo =
                            gananciaHoraEstimada
                                    >= ritmoObjetivo;
                }
            }

            boolean aceptar =
                    cumpleKm
                            && cumpleTiempo
                            && cumpleRitmo;

            if (aceptar) {
                tvResultado.setText("ACEPTAR");
            } else {
                tvResultado.setText("RECHAZAR");
            }

            /*
             * Ya no mostramos la explicación completa.
             */
            tvDetalles.setText("");

            btnAceptar.setEnabled(true);
            btnRechazar.setEnabled(true);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Ingresa valores numéricos válidos",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void aceptarPedido() {

        if (databaseHelper.existePedidoEnCurso()) {

            Toast.makeText(
                    this,
                    "Ya existe un pedido activo",
                    Toast.LENGTH_SHORT
            ).show();

            recuperarPedidoEnCurso();

            return;
        }

        if (pedidoActualId != -1) {

            Toast.makeText(
                    this,
                    "Ya existe un pedido en curso",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);

        long timestampInicio =
                System.currentTimeMillis();

        String horaInicio =
                new SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date(timestampInicio));

        String fecha =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date(timestampInicio));

        long resultado =
                databaseHelper.insertarPedido(
                        gananciaMostradaActual,
                        gananciaNetaActual,
                        distanciaClienteActual,
                        configuracion.getDistanciaTienda(),
                        distanciaTotalActual,
                        gananciaKmActual,
                        horaInicio,
                        timestampInicio,
                        fecha
                );

        if (resultado != -1) {

            pedidoActualId = resultado;
            tiempoInicioMillis = timestampInicio;

            mostrarPedidoEnCurso(
                    horaInicio,
                    gananciaNetaActual,
                    distanciaTotalActual
            );

        } else {

            btnAceptar.setEnabled(true);
            btnRechazar.setEnabled(true);

            Toast.makeText(
                    this,
                    "Error al guardar el pedido",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void rechazarPedido() {

        if (pedidoActualId != -1) {

            Toast.makeText(
                    this,
                    "Hay un pedido en curso",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);

        String hora =
                new SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

        String fecha =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

        long resultado =
                databaseHelper.insertarPedidoRechazado(
                        gananciaMostradaActual,
                        gananciaNetaActual,
                        distanciaClienteActual,
                        configuracion.getDistanciaTienda(),
                        distanciaTotalActual,
                        gananciaKmActual,
                        fecha,
                        hora
                );

        if (resultado != -1) {

            Toast.makeText(
                    this,
                    "Pedido rechazado guardado",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Error al guardar rechazo",
                    Toast.LENGTH_SHORT
            ).show();
        }

        actualizarEstadisticasDia();
        limpiarParaNuevoPedido();
    }

    private void finalizarPedido() {

        if (pedidoActualId == -1
                || tiempoInicioMillis <= 0) {

            Toast.makeText(
                    this,
                    "No hay ningún pedido en curso",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        long tiempoFinMillis =
                System.currentTimeMillis();

        long diferenciaMillis =
                tiempoFinMillis
                        - tiempoInicioMillis;

        int duracionMin =
                (int) Math.ceil(
                        diferenciaMillis
                                / 60000.0
                );

        String horaFin =
                new SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                ).format(
                        new Date(
                                tiempoFinMillis
                        )
                );

        String estadoTiempo;

        if (duracionMin <= 30) {

            estadoTiempo =
                    "FINALIZADO_BUEN_TIEMPO";

        } else if (duracionMin <= 40) {

            estadoTiempo =
                    "FINALIZADO_ACEPTABLE";

        } else {

            estadoTiempo =
                    "FINALIZADO_LENTO";
        }

        int filasActualizadas =
                databaseHelper.finalizarPedido(
                        pedidoActualId,
                        horaFin,
                        duracionMin,
                        estadoTiempo
                );

        if (filasActualizadas > 0) {

            String mensaje;

            if (duracionMin <= 30) {

                mensaje =
                        "BUENA INVERSIÓN DE TIEMPO";

            } else if (duracionMin <= 40) {

                mensaje =
                        "TIEMPO ACEPTABLE";

            } else {

                mensaje =
                        "PEDIDO LENTO";
            }

            tvResultado.setText(
                    mensaje
            );

            tvDetalles.setText(
                    "Duración real: "
                            + duracionMin
                            + " min"
                            + "\nHora de término: "
                            + horaFin
            );

            Toast.makeText(
                    this,
                    "Pedido finalizado",
                    Toast.LENGTH_SHORT
            ).show();

            actualizarGananciaDia();
            actualizarEstadisticasDia();

            pedidoActualId = -1;
            tiempoInicioMillis = 0;

            btnFinalizar.setEnabled(false);

            btnAnalizar.setEnabled(true);

            etGanancia.setEnabled(true);
            etDistanciaCliente.setEnabled(true);

            etGanancia.setText("");
            etDistanciaCliente.setText("");
            etGanancia.requestFocus();

            btnAceptar.setEnabled(false);
            btnRechazar.setEnabled(false);
            btnCancelarPedido.setVisibility(
                    android.view.View.GONE
            );

        } else {

            Toast.makeText(
                    this,
                    "Error al finalizar el pedido",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void cancelarPedidoEnCurso() {

        if (pedidoActualId == -1) {

            Toast.makeText(
                    this,
                    "No hay pedido en curso",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int filas =
                databaseHelper.cancelarPedidoEnCurso(
                        pedidoActualId
                );

        if (filas > 0) {

            pedidoActualId = -1;
            tiempoInicioMillis = 0;

            btnFinalizar.setEnabled(false);
            btnCancelarPedido.setVisibility(
                    android.view.View.GONE
            );

            limpiarParaNuevoPedido();

            Toast.makeText(
                    this,
                    "Pedido cancelado",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "No se pudo cancelar el pedido",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
     * Se ejecuta al abrir la aplicación.
     */
    private void recuperarPedidoEnCurso() {

        Cursor cursor =
                databaseHelper.obtenerPedidoEnCurso();

        if (cursor == null) {
            return;
        }

        if (cursor.moveToFirst()) {

            int indiceId =
                    cursor.getColumnIndex(
                            DatabaseHelper.COL_ID
                    );

            int indiceTimestamp =
                    cursor.getColumnIndex(
                            DatabaseHelper.COL_TIMESTAMP_INICIO
                    );

            int indiceHora =
                    cursor.getColumnIndex(
                            DatabaseHelper.COL_HORA_INICIO
                    );

            int indiceGanancia =
                    cursor.getColumnIndex(
                            DatabaseHelper.COL_GANANCIA_NETA
                    );

            int indiceDistancia =
                    cursor.getColumnIndex(
                            DatabaseHelper.COL_DISTANCIA_TOTAL
                    );

            long id =
                    cursor.getLong(indiceId);

            long timestamp =
                    cursor.getLong(indiceTimestamp);

            /*
             * Pedido antiguo o corrupto.
             * No puede considerarse realmente
             * un pedido en curso.
             */
            if (timestamp <= 0) {

                databaseHelper.cancelarPedidoEnCurso(id);

                pedidoActualId = -1;
                tiempoInicioMillis = 0;

                cursor.close();

                limpiarParaNuevoPedido();

                Toast.makeText(
                        this,
                        "Se corrigió un pedido antiguo que había quedado abierto",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            /*
             * Solo después de comprobar que
             * el registro es válido lo cargamos.
             */
            pedidoActualId = id;
            tiempoInicioMillis = timestamp;

            String horaInicio =
                    cursor.getString(indiceHora);

            double gananciaNeta =
                    cursor.getDouble(indiceGanancia);

            double distanciaTotal =
                    cursor.getDouble(indiceDistancia);

            mostrarPedidoEnCurso(
                    horaInicio,
                    gananciaNeta,
                    distanciaTotal
            );
        }

        cursor.close();
    }

    /*
     * Configura la interfaz cuando
     * existe un pedido activo.
     */
    private void mostrarPedidoEnCurso(
            String horaInicio,
            double gananciaNeta,
            double distanciaTotal
    ) {

        tvResultado.setText(
                "PEDIDO EN CURSO"
        );

        tvDetalles.setText(
                "Inicio: "
                        + horaInicio
                        + "\nGanancia neta: $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        gananciaNeta
                )
                        + "\nDistancia estimada: "
                        + String.format(
                        Locale.getDefault(),
                        "%.1f",
                        distanciaTotal
                )
                        + " km"
        );

        btnFinalizar.setEnabled(true);
        btnAnalizar.setEnabled(false);
        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);
        btnCancelarPedido.setVisibility(
                android.view.View.VISIBLE
        );

        etGanancia.setEnabled(false);
        etDistanciaCliente.setEnabled(false);
    }

    private void limpiarParaNuevoPedido() {

        pedidoActualId = -1;
        tiempoInicioMillis = 0;

        etGanancia.setText("");
        etDistanciaCliente.setText("");

        tvResultado.setText(
                "Esperando pedido..."
        );

        tvDetalles.setText(
                "Aquí aparecerán los cálculos"
        );

        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);
        btnFinalizar.setEnabled(false);

        btnAnalizar.setEnabled(true);
        btnCancelarPedido.setVisibility(
                android.view.View.GONE
        );

        etGanancia.setEnabled(true);
        etDistanciaCliente.setEnabled(true);

        etGanancia.requestFocus();
    }

    private void actualizarGananciaDia() {

        double metaDiaria =
                configuracion.getMetaDiaria();

        String fechaActual =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

        double gananciaDia =
                databaseHelper.obtenerGananciaDelDia(
                        fechaActual
                );

        double porcentaje =
                (gananciaDia / metaDiaria) * 100;

        if (porcentaje > 100) {
            porcentaje = 100;
        }

        tvGananciaDia.setText(
                "Hoy: $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        gananciaDia
                )
                        + " / $"
                        + String.format(
                        Locale.getDefault(),
                        "%.0f",
                        metaDiaria
                )
        );

        tvProgresoMeta.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f",
                        porcentaje
                )
                        + "% de la meta"
        );

        actualizarRitmoNecesario(gananciaDia);
    }

    private void actualizarRitmoNecesario(double gananciaDia) {

        double metaDiaria =
                configuracion.getMetaDiaria();

        int horaInicioJornada =
                configuracion.getHoraInicio();

        int horaFinJornada =
                configuracion.getHoraFin();

        double dineroFaltante =
                metaDiaria - gananciaDia;

        if (dineroFaltante <= 0) {

            tvRitmoNecesario.setText(
                    "META ALCANZADA ✓"
            );

            return;
        }

        Calendar ahora = Calendar.getInstance();

        Calendar finJornada = Calendar.getInstance();

        finJornada.set(
                Calendar.HOUR_OF_DAY,
                horaFinJornada
        );

        finJornada.set(
                Calendar.MINUTE,
                0
        );

        finJornada.set(
                Calendar.SECOND,
                0
        );

        finJornada.set(
                Calendar.MILLISECOND,
                0
        );

        long tiempoRestanteMillis =
                finJornada.getTimeInMillis()
                        - ahora.getTimeInMillis();

        /*
         * Antes del inicio de la jornada
         * usamos la duración completa configurada.
         */
        if (ahora.get(Calendar.HOUR_OF_DAY)
                < horaInicioJornada) {

            double horasJornada =
                    horaFinJornada - horaInicioJornada;

            double ritmo =
                    metaDiaria / horasJornada;

            tvRitmoNecesario.setText(
                    "Ritmo necesario: $"
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            ritmo
                    )
                            + "/h"
            );

            return;
        }

        /*
        Después del final de la jornada.
        */
        if (tiempoRestanteMillis <= 0) {

            tvRitmoNecesario.setText(
                    "Jornada terminada — faltaron $"
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            dineroFaltante
                    )
            );

            return;
        }

        double horasRestantes =
                tiempoRestanteMillis
                        / 3600000.0;

        double ritmoNecesario =
                dineroFaltante
                        / horasRestantes;

        tvRitmoNecesario.setText(
                "Faltan $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        dineroFaltante
                )
                        + " • Necesitas $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        ritmoNecesario
                )
                        + "/h"
        );
    }

    private void actualizarEstadisticasDia() {

        String fechaActual =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

        EstadisticasDia estadisticas =
                databaseHelper.obtenerEstadisticasDia(
                        fechaActual
                );

        String resumen =
                "Finalizados: "
                        + estadisticas.pedidosFinalizados

                        + "   |   Rechazados: "
                        + estadisticas.pedidosRechazados

                        + "\nPromedio/pedido: $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        estadisticas.promedioPorPedido
                )

                        + "   |   Duración prom.: "
                        + String.format(
                        Locale.getDefault(),
                        "%.1f",
                        estadisticas.duracionPromedio
                )
                        + " min"

                        + "\n≤30 min: "
                        + estadisticas.pedidosBuenTiempo

                        + "   |   >30 min: "
                        + estadisticas.pedidosLentos;

        tvEstadisticasDia.setText(resumen);
    }

    private double obtenerRitmoNecesario() {

        double metaDiaria =
                configuracion.getMetaDiaria();

        int horaInicioJornada =
                configuracion.getHoraInicio();

        int horaFinJornada =
                configuracion.getHoraFin();

        String fechaActual =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

        double gananciaDia =
                databaseHelper.obtenerGananciaDelDia(
                        fechaActual
                );

        double dineroFaltante =
                metaDiaria - gananciaDia;

        if (dineroFaltante <= 0) {
            return 0;
        }

        Calendar ahora =
                Calendar.getInstance();

        /*
         * Antes del inicio de la jornada
         * usamos la duración completa configurada.
         */
        if (ahora.get(Calendar.HOUR_OF_DAY)
                < horaInicioJornada) {

            double horasJornada =
                    horaFinJornada - horaInicioJornada;

            return metaDiaria / horasJornada;
        }

        Calendar finJornada =
                Calendar.getInstance();

        finJornada.set(
                Calendar.HOUR_OF_DAY,
                horaFinJornada
        );

        finJornada.set(
                Calendar.MINUTE,
                0
        );

        finJornada.set(
                Calendar.SECOND,
                0
        );

        finJornada.set(
                Calendar.MILLISECOND,
                0
        );

        long tiempoRestanteMillis =
                finJornada.getTimeInMillis()
                        - ahora.getTimeInMillis();

        if (tiempoRestanteMillis <= 0) {
            return RITMO_MAXIMO_EXIGIBLE;
        }

        double horasRestantes =
                tiempoRestanteMillis
                        / 3600000.0;

        return dineroFaltante
                / horasRestantes;
    }

    private void actualizarConfiguracionVisual() {

        double distancia =
                configuracion.getDistanciaTienda();

        tvDistanciaTienda.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f km",
                        distancia
                )
        );
    }

    private void mostrarCierreJornada() {

        String fechaActual =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());

        EstadisticasDia estadisticas =
                databaseHelper.obtenerEstadisticasDia(
                        fechaActual
                );

        double meta =
                configuracion.getMetaDiaria();

        double ganancia =
                estadisticas.gananciaTotal;

        double diferencia =
                ganancia - meta;

        String resultadoMeta;

        if (ganancia >= meta) {

            resultadoMeta =
                    "META ALCANZADA";

        } else {

            resultadoMeta =
                    "META NO ALCANZADA";
        }

        String mensaje =
                resultadoMeta

                        + "\n\nGanancia neta: $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        ganancia
                )

                        + "\nMeta: $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        meta
                )

                        + "\nPedidos finalizados: "
                        + estadisticas.pedidosFinalizados

                        + "\nPedidos rechazados: "
                        + estadisticas.pedidosRechazados

                        + "\nPromedio por pedido: $"
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        estadisticas.promedioPorPedido
                )

                        + "\nDuración promedio: "
                        + String.format(
                        Locale.getDefault(),
                        "%.1f",
                        estadisticas.duracionPromedio
                )
                        + " min";

        if (diferencia >= 0) {

            mensaje +=
                    "\nSuperaste la meta por: $"
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            diferencia
                    );

        } else {

            mensaje +=
                    "\nFaltaron: $"
                            + String.format(
                            Locale.getDefault(),
                            "%.2f",
                            Math.abs(diferencia)
                    );
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Resumen del día")
                .setMessage(mensaje)
                .setPositiveButton(
                        "CERRAR",
                        null
                )
                .show();
    }

    /*
    private void confirmarBorradoPruebas() {

        if (pedidoActualId != -1
                || databaseHelper.existePedidoEnCurso()) {

            Toast.makeText(
                    this,
                    "Finaliza o cancela el pedido en curso primero",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Borrar datos de prueba")
                .setMessage(
                        "Se eliminarán todos los pedidos y estadísticas guardados. "
                                + "La configuración se conservará."
                )
                .setNegativeButton(
                        "CANCELAR",
                        null
                )
                .setPositiveButton(
                        "BORRAR",
                        (dialog, which) -> borrarDatosPrueba()
                )
                .show();
    }
    private void borrarDatosPrueba() {

        int eliminados =
                databaseHelper.eliminarTodosLosPedidos();

        pedidoActualId = -1;
        tiempoInicioMillis = 0;

        limpiarParaNuevoPedido();

        actualizarGananciaDia();
        actualizarEstadisticasDia();

        Toast.makeText(
                this,
                "Datos eliminados: " + eliminados,
                Toast.LENGTH_SHORT
        ).show();
    }
    */

}