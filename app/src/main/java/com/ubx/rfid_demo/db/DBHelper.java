package com.ubx.rfid_demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "despacho.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_EMPRESAS="t_empresas";
    private static final String CREATE_TABLE_LOCALES="t_locales";

    // Constructor
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tablas aquí
        String CT_EMPRESAS = "CREATE TABLE "+CREATE_TABLE_EMPRESAS+" (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "ruta TEXT NOT NULL" +
                ");";
        db.execSQL(CT_EMPRESAS);


        String CT_LOCAL =
                "CREATE TABLE "+CREATE_TABLE_LOCALES+" (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre VARCHAR(50), " +
                        "empresa_id INTEGER, " +
                        "ruta TEXT, " +
                        "es_local INTEGER CHECK(es_local IN (0, 1)), " +
                        "FOREIGN KEY(empresa_id) REFERENCES \"+CREATE_TABLE_EMPRESAS+\"(id));";
        db.execSQL(CT_LOCAL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Actualizar la base de datos si cambia la versión
        db.execSQL("DROP TABLE IF EXISTS "+CREATE_TABLE_EMPRESAS);
        db.execSQL("DROP TABLE IF EXISTS "+CREATE_TABLE_LOCALES);
        onCreate(db);
    }
}
