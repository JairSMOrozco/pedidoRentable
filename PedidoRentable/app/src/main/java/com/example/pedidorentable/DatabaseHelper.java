package com.example.pedidorentable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pedidos.db";

    // Cambiamos de versión 1 a versión 2
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_PEDIDOS = "pedidos";

    // TABLA DE RESTAURANTES
    public static final String TABLE_RESTAURANTES = "restaurantes";

    public static final String COL_RESTAURANTE_ID = "id";
    public static final String COL_RESTAURANTE_NOMBRE = "nombre";
    public static final String COL_RESTAURANTE_LATITUD = "latitud";
    public static final String COL_RESTAURANTE_LONGITUD = "longitud";
    public static final String COL_RESTAURANTE_FECHA_ALTA = "fecha_alta";

    public static final String COL_ID = "id";
    public static final String COL_GANANCIA_MOSTRADA = "ganancia_mostrada";
    public static final String COL_GANANCIA_NETA = "ganancia_neta";
    public static final String COL_DISTANCIA_CLIENTE = "distancia_cliente";
    public static final String COL_DISTANCIA_TIENDA = "distancia_tienda";
    public static final String COL_DISTANCIA_TOTAL = "distancia_total";
    public static final String COL_GANANCIA_KM = "ganancia_km";
    public static final String COL_HORA_INICIO = "hora_inicio";

    // NUEVO
    public static final String COL_TIMESTAMP_INICIO = "timestamp_inicio";

    public static final String COL_HORA_FIN = "hora_fin";
    public static final String COL_DURACION_MIN = "duracion_min";
    public static final String COL_ESTADO = "estado";
    public static final String COL_FECHA = "fecha";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String crearTabla =
                "CREATE TABLE " + TABLE_PEDIDOS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_GANANCIA_MOSTRADA + " REAL, " +
                        COL_GANANCIA_NETA + " REAL, " +
                        COL_DISTANCIA_CLIENTE + " REAL, " +
                        COL_DISTANCIA_TIENDA + " REAL, " +
                        COL_DISTANCIA_TOTAL + " REAL, " +
                        COL_GANANCIA_KM + " REAL, " +
                        COL_HORA_INICIO + " TEXT, " +
                        COL_TIMESTAMP_INICIO + " INTEGER, " +
                        COL_HORA_FIN + " TEXT, " +
                        COL_DURACION_MIN + " INTEGER, " +
                        COL_ESTADO + " TEXT, " +
                        COL_FECHA + " TEXT" +
                        ")";

        db.execSQL(crearTabla);

        String crearTablaRestaurantes =
                "CREATE TABLE " + TABLE_RESTAURANTES + " (" +
                        COL_RESTAURANTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RESTAURANTE_NOMBRE + " TEXT NOT NULL, " +
                        COL_RESTAURANTE_LATITUD + " REAL NOT NULL, " +
                        COL_RESTAURANTE_LONGITUD + " REAL NOT NULL, " +
                        COL_RESTAURANTE_FECHA_ALTA + " TEXT" +
                        ")";

        db.execSQL(crearTablaRestaurantes);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        // Versión 1 -> 2
        // Añadimos timestamp de inicio sin borrar pedidos.
        if (oldVersion < 2) {

            db.execSQL(
                    "ALTER TABLE " +
                            TABLE_PEDIDOS +
                            " ADD COLUMN " +
                            COL_TIMESTAMP_INICIO +
                            " INTEGER DEFAULT 0"
            );
        }

        // Versión 2 -> 3
        // Creamos la tabla de restaurantes sin modificar la tabla de pedidos.
        if (oldVersion < 3) {

            String crearTablaRestaurantes =
                    "CREATE TABLE IF NOT EXISTS " +
                            TABLE_RESTAURANTES + " (" +
                            COL_RESTAURANTE_ID +
                            " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COL_RESTAURANTE_NOMBRE +
                            " TEXT NOT NULL, " +
                            COL_RESTAURANTE_LATITUD +
                            " REAL NOT NULL, " +
                            COL_RESTAURANTE_LONGITUD +
                            " REAL NOT NULL, " +
                            COL_RESTAURANTE_FECHA_ALTA +
                            " TEXT" +
                            ")";

            db.execSQL(crearTablaRestaurantes);
        }
    }

    public long insertarPedido(
            double gananciaMostrada,
            double gananciaNeta,
            double distanciaCliente,
            double distanciaTienda,
            double distanciaTotal,
            double gananciaKm,
            String horaInicio,
            long timestampInicio,
            String fecha
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_GANANCIA_MOSTRADA, gananciaMostrada);
        values.put(COL_GANANCIA_NETA, gananciaNeta);
        values.put(COL_DISTANCIA_CLIENTE, distanciaCliente);
        values.put(COL_DISTANCIA_TIENDA, distanciaTienda);
        values.put(COL_DISTANCIA_TOTAL, distanciaTotal);
        values.put(COL_GANANCIA_KM, gananciaKm);

        values.put(COL_HORA_INICIO, horaInicio);

        // Guardamos el momento exacto
        values.put(COL_TIMESTAMP_INICIO, timestampInicio);

        values.put(COL_ESTADO, "EN_CURSO");
        values.put(COL_FECHA, fecha);

        return db.insert(
                TABLE_PEDIDOS,
                null,
                values
        );
    }

    public int finalizarPedido(
            long idPedido,
            String horaFin,
            int duracionMin,
            String estado
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_HORA_FIN, horaFin);
        values.put(COL_DURACION_MIN, duracionMin);
        values.put(COL_ESTADO, estado);

        return db.update(
                TABLE_PEDIDOS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(idPedido)}
        );
    }

    /*
     * Busca si existe un pedido que quedó EN_CURSO.
     */
    public Cursor obtenerPedidoEnCurso() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_PEDIDOS,
                null,
                COL_ESTADO + " = ?",
                new String[]{"EN_CURSO"},
                null,
                null,
                COL_ID + " DESC",
                "1"
        );
    }

    public double obtenerGananciaDelDia(String fecha) {

        SQLiteDatabase db = this.getReadableDatabase();

        double total = 0;

        String consulta =
                "SELECT SUM(" + COL_GANANCIA_NETA + ") " +
                        "FROM " + TABLE_PEDIDOS +
                        " WHERE " + COL_FECHA + " = ?" +
                        " AND " + COL_ESTADO + " LIKE 'FINALIZADO_%'";

        Cursor cursor = db.rawQuery(
                consulta,
                new String[]{fecha}
        );

        if (cursor.moveToFirst()) {

            if (!cursor.isNull(0)) {
                total = cursor.getDouble(0);
            }
        }

        cursor.close();

        return total;
    }

    public long insertarPedidoRechazado(
            double gananciaMostrada,
            double gananciaNeta,
            double distanciaCliente,
            double distanciaTienda,
            double distanciaTotal,
            double gananciaKm,
            String fecha,
            String hora
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_GANANCIA_MOSTRADA, gananciaMostrada);
        values.put(COL_GANANCIA_NETA, gananciaNeta);
        values.put(COL_DISTANCIA_CLIENTE, distanciaCliente);
        values.put(COL_DISTANCIA_TIENDA, distanciaTienda);
        values.put(COL_DISTANCIA_TOTAL, distanciaTotal);
        values.put(COL_GANANCIA_KM, gananciaKm);

        values.put(COL_HORA_INICIO, hora);
        values.put(COL_TIMESTAMP_INICIO, 0);

        values.put(COL_ESTADO, "RECHAZADO");
        values.put(COL_FECHA, fecha);

        return db.insert(
                TABLE_PEDIDOS,
                null,
                values
        );
    }

    public EstadisticasDia obtenerEstadisticasDia(String fecha) {

        SQLiteDatabase db = this.getReadableDatabase();

        EstadisticasDia estadisticas = new EstadisticasDia();

        /*
         * PEDIDOS FINALIZADOS
         */
        String consultaFinalizados =
                "SELECT " +
                        "COUNT(*), " +
                        "SUM(" + COL_GANANCIA_NETA + "), " +
                        "AVG(" + COL_GANANCIA_NETA + "), " +
                        "AVG(" + COL_DURACION_MIN + ") " +
                        "FROM " + TABLE_PEDIDOS +
                        " WHERE " + COL_FECHA + " = ?" +
                        " AND " + COL_ESTADO + " LIKE 'FINALIZADO_%'";

        Cursor cursorFinalizados =
                db.rawQuery(
                        consultaFinalizados,
                        new String[]{fecha}
                );

        if (cursorFinalizados.moveToFirst()) {

            estadisticas.pedidosFinalizados =
                    cursorFinalizados.getInt(0);

            if (!cursorFinalizados.isNull(1)) {
                estadisticas.gananciaTotal =
                        cursorFinalizados.getDouble(1);
            }

            if (!cursorFinalizados.isNull(2)) {
                estadisticas.promedioPorPedido =
                        cursorFinalizados.getDouble(2);
            }

            if (!cursorFinalizados.isNull(3)) {
                estadisticas.duracionPromedio =
                        cursorFinalizados.getDouble(3);
            }
        }

        cursorFinalizados.close();

        /*
         * PEDIDOS RECHAZADOS
         */
        String consultaRechazados =
                "SELECT COUNT(*) " +
                        "FROM " + TABLE_PEDIDOS +
                        " WHERE " + COL_FECHA + " = ?" +
                        " AND " + COL_ESTADO + " = 'RECHAZADO'";

        Cursor cursorRechazados =
                db.rawQuery(
                        consultaRechazados,
                        new String[]{fecha}
                );

        if (cursorRechazados.moveToFirst()) {

            estadisticas.pedidosRechazados =
                    cursorRechazados.getInt(0);
        }

        cursorRechazados.close();

        /*
         * PEDIDOS ≤ 30 MIN
         */
        String consultaBuenTiempo =
                "SELECT COUNT(*) " +
                        "FROM " + TABLE_PEDIDOS +
                        " WHERE " + COL_FECHA + " = ?" +
                        " AND " + COL_ESTADO + " LIKE 'FINALIZADO_%'" +
                        " AND " + COL_DURACION_MIN + " <= 30";

        Cursor cursorBuenTiempo =
                db.rawQuery(
                        consultaBuenTiempo,
                        new String[]{fecha}
                );

        if (cursorBuenTiempo.moveToFirst()) {

            estadisticas.pedidosBuenTiempo =
                    cursorBuenTiempo.getInt(0);
        }

        cursorBuenTiempo.close();

        /*
         * PEDIDOS > 30 MIN
         */
        String consultaLentos =
                "SELECT COUNT(*) " +
                        "FROM " + TABLE_PEDIDOS +
                        " WHERE " + COL_FECHA + " = ?" +
                        " AND " + COL_ESTADO + " LIKE 'FINALIZADO_%'" +
                        " AND " + COL_DURACION_MIN + " > 30";

        Cursor cursorLentos =
                db.rawQuery(
                        consultaLentos,
                        new String[]{fecha}
                );

        if (cursorLentos.moveToFirst()) {

            estadisticas.pedidosLentos =
                    cursorLentos.getInt(0);
        }

        cursorLentos.close();

        return estadisticas;
    }

    public EstimacionDuracion obtenerEstimacionDuracion(
            double distanciaTotal
    ) {

        SQLiteDatabase db = this.getReadableDatabase();

        /*
         * Construimos una franja de 1 km.
         *
         * Ejemplo:
         * 3.7 km -> buscamos pedidos entre 3.0 y 4.0 km.
         * 4.4 km -> buscamos pedidos entre 4.0 y 5.0 km.
         */
        double distanciaMinima =
                Math.floor(distanciaTotal);

        double distanciaMaxima =
                distanciaMinima + 1.0;

        String consulta =
                "SELECT COUNT(*), AVG(" +
                        COL_DURACION_MIN +
                        ") FROM " +
                        TABLE_PEDIDOS +
                        " WHERE " +
                        COL_DISTANCIA_TOTAL + " >= ?" +
                        " AND " +
                        COL_DISTANCIA_TOTAL + " < ?" +
                        " AND " +
                        COL_ESTADO +
                        " LIKE 'FINALIZADO_%'" +
                        " AND " +
                        COL_DURACION_MIN +
                        " IS NOT NULL";

        Cursor cursor = db.rawQuery(
                consulta,
                new String[]{
                        String.valueOf(distanciaMinima),
                        String.valueOf(distanciaMaxima)
                }
        );

        int cantidad = 0;
        double promedio = 0;

        if (cursor.moveToFirst()) {

            cantidad = cursor.getInt(0);

            if (!cursor.isNull(1)) {
                promedio = cursor.getDouble(1);
            }
        }

        cursor.close();

        return new EstimacionDuracion(
                promedio,
                cantidad
        );
    }


    public boolean existePedidoEnCurso() {

        SQLiteDatabase db =
                this.getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM "
                                + TABLE_PEDIDOS
                                + " WHERE "
                                + COL_ESTADO
                                + " = 'EN_CURSO'",
                        null
                );

        boolean existe = false;

        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0;
        }

        cursor.close();

        return existe;
    }

    public List<ResumenDia> obtenerResumenPorDias() {

        SQLiteDatabase db = this.getReadableDatabase();

        List<ResumenDia> lista = new ArrayList<>();

        String consulta =
                "SELECT " + COL_FECHA + ", " +

                        "SUM(CASE WHEN " +
                        COL_ESTADO +
                        " LIKE 'FINALIZADO_%' THEN 1 ELSE 0 END), " +

                        "SUM(CASE WHEN " +
                        COL_ESTADO +
                        " = 'RECHAZADO' THEN 1 ELSE 0 END), " +

                        "SUM(CASE WHEN " +
                        COL_ESTADO +
                        " LIKE 'FINALIZADO_%' THEN " +
                        COL_GANANCIA_NETA +
                        " ELSE 0 END), " +

                        "AVG(CASE WHEN " +
                        COL_ESTADO +
                        " LIKE 'FINALIZADO_%' THEN " +
                        COL_DURACION_MIN +
                        " END), " +

                        "AVG(CASE WHEN " +
                        COL_ESTADO +
                        " LIKE 'FINALIZADO_%' THEN " +
                        COL_GANANCIA_NETA +
                        " END) " +

                        "FROM " + TABLE_PEDIDOS +

                        " WHERE " + COL_FECHA +
                        " IS NOT NULL " +

                        "GROUP BY " + COL_FECHA +

                        " ORDER BY " + COL_FECHA +
                        " DESC";

        Cursor cursor = db.rawQuery(
                consulta,
                null
        );

        while (cursor.moveToNext()) {

            String fecha =
                    cursor.getString(0);

            int pedidos =
                    cursor.getInt(1);

            int rechazados =
                    cursor.getInt(2);

            double ganancia =
                    cursor.isNull(3)
                            ? 0
                            : cursor.getDouble(3);

            double duracion =
                    cursor.isNull(4)
                            ? 0
                            : cursor.getDouble(4);

            double promedio =
                    cursor.isNull(5)
                            ? 0
                            : cursor.getDouble(5);

            lista.add(
                    new ResumenDia(
                            fecha,
                            pedidos,
                            rechazados,
                            ganancia,
                            duracion,
                            promedio
                    )
            );
        }

        cursor.close();

        return lista;
    }

    public int cancelarPedidoEnCurso(long idPedido) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                COL_ESTADO,
                "CANCELADO"
        );

        return db.update(
                TABLE_PEDIDOS,
                values,
                COL_ID + " = ?",
                new String[]{
                        String.valueOf(idPedido)
                }
        );
    }

    public int eliminarTodosLosPedidos() {

        SQLiteDatabase db =
                this.getWritableDatabase();

        return db.delete(
                TABLE_PEDIDOS,
                null,
                null
        );
    }

    public long insertarRestaurante(
            String nombre,
            double latitud,
            double longitud,
            String fechaAlta
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COL_RESTAURANTE_NOMBRE, nombre);
        values.put(COL_RESTAURANTE_LATITUD, latitud);
        values.put(COL_RESTAURANTE_LONGITUD, longitud);
        values.put(COL_RESTAURANTE_FECHA_ALTA, fechaAlta);

        return db.insert(
                TABLE_RESTAURANTES,
                null,
                values
        );
    }


    public List<Restaurante> obtenerRestaurantes() {

        SQLiteDatabase db = this.getReadableDatabase();

        List<Restaurante> lista = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE_RESTAURANTES,
                null,
                null,
                null,
                null,
                null,
                COL_RESTAURANTE_NOMBRE + " ASC"
        );

        while (cursor.moveToNext()) {

            long id =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    COL_RESTAURANTE_ID
                            )
                    );

            String nombre =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    COL_RESTAURANTE_NOMBRE
                            )
                    );

            double latitud =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    COL_RESTAURANTE_LATITUD
                            )
                    );

            double longitud =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    COL_RESTAURANTE_LONGITUD
                            )
                    );

            String fechaAlta =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    COL_RESTAURANTE_FECHA_ALTA
                            )
                    );

            lista.add(
                    new Restaurante(
                            id,
                            nombre,
                            latitud,
                            longitud,
                            fechaAlta
                    )
            );
        }

        cursor.close();

        return lista;
    }


}