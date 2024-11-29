package com.ubx.rfid_demo.ui.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.MainActivity;
import com.ubx.usdk.rfid.aidl.IRfidCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import java.io.BufferedReader;

import android.os.Environment;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.content.SharedPreferences;
import android.content.Context;
import org.json.JSONException;
import java.util.Map;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;

public class ConfFragment extends Fragment {
    private static  MainActivity mActivity;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private Spinner spinnerEmpresa, spinnerLocal;
    private Button btnSaveSite, btnSavePotencia, btnSaveDevice;
    private EditText editPotencia, editDevice;
    private JSONArray empresasJsonArray;
    private List<String> empresasList = new ArrayList<>();
    private Map<String, List<String>> anexosMap = new HashMap<>();

    private HashMap<String, List<String>> empresaLocalMap = new HashMap<>();
    private int esLocal = -1;
    private int esEmpresa = -1;

    private boolean isInitialSetup = true;

    public static ConfFragment newInstance(MainActivity activity) {
        mActivity = activity;
        return new ConfFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);
        // Inflar el diseño del fragmento
        // Verificar si la versión de Android es 6.0 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkStoragePermission();
        }

        spinnerEmpresa = view.findViewById(R.id.spinner_empresa);
        spinnerLocal = view.findViewById(R.id.spinner_local);
        btnSaveSite = view.findViewById(R.id.btn_save_site);
        editPotencia = view.findViewById(R.id.edit_potencia);
        editDevice = view.findViewById(R.id.edit_device);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);

        // Leer datos
        int outputPower = sharedPreferences.getInt("OUTPUT_POWER", 15);
        editPotencia.setText(String.valueOf(outputPower));
        loadJsonData();
        System.out.println("ES_LOCAL--->"+sharedPreferences.getInt("ES_LOCAL",-1));
        System.out.println("ES_Empresa--->"+sharedPreferences.getInt("ES_Empresa",-1));
        System.out.println("Potencia--->"+sharedPreferences.getInt("OUTPUT_POWER",-1));

        btnSaveSite.setOnClickListener(v -> saveEmpresaData());

        return view;
    }

    private void saveEmpresaData() {
        String selectedEmpresa = (String) spinnerEmpresa.getSelectedItem();
        Integer posiEmpresa = spinnerEmpresa.getSelectedItemPosition();

        String selectedLocal = (String) spinnerLocal.getSelectedItem();
        Integer posiLocal = spinnerLocal.getSelectedItemPosition();

        String potenciaString = editPotencia.getText().toString().trim();

        if (TextUtils.isEmpty(selectedEmpresa) || posiEmpresa < 0) {
            Toast.makeText(getActivity(), "Seleccione una empresa.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedLocal) || posiLocal < 0) {
            Toast.makeText(getActivity(), "Seleccione un local.", Toast.LENGTH_SHORT).show();
            return;
        }

        int potencia = Integer.parseInt(potenciaString);
        if (potencia < 1 || potencia > 30) {
            Toast.makeText(getActivity(), "La potencia debe estar entre 1 y 30.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar ES_LOCAL y OUTPUT_POWER en SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("ES_LOCAL", posiLocal);
        editor.putInt("ES_EMPRESA", posiEmpresa);
        editor.putInt("OUTPUT_POWER", potencia);
        editor.putString("EMPRESA",selectedEmpresa);
        editor.putString("ANEXO",selectedLocal);
        editor.apply();
        // Actualizar mRfidManager a través de MainActivity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && activity.mRfidManager != null) {
            int result = activity.mRfidManager.setOutputPower((byte) potencia);
            if (result == 0) {
                Toast.makeText(getActivity(), "Configuración guardada correctamente.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Error al actualizar la potencia.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "RFID Manager no inicializado.", Toast.LENGTH_SHORT).show();
        }

        System.out.println(" SAVE ---- La empresa ("+posiEmpresa+")-->"+selectedEmpresa+" El local es  ("+posiLocal+")-->"+selectedLocal);
    }

    private void loadJsonData() {
        File directory = new File(Environment.getExternalStorageDirectory(), "INI");
        File currentFile = new File(directory, "empresas.json");

        if (!currentFile.exists()) {
            Toast.makeText(getActivity(), "El archivo empresas.json no existe en el directorio INI.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder jsonData = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonData.append(line);
            }

            // Inicializar empresasJsonArray con los datos leídos
            empresasJsonArray = new JSONArray(jsonData.toString());

            // Construir listas de empresas y anexos
            for (int i = 0; i < empresasJsonArray.length(); i++) {
                JSONObject empresaObj = empresasJsonArray.getJSONObject(i);
                String empresa = empresaObj.getString("empresa");
                String anexo = empresaObj.getString("anexo");

                if (!empresasList.contains(empresa)) {
                    empresasList.add(empresa);
                }

                anexosMap.putIfAbsent(empresa, new ArrayList<>());
                anexosMap.get(empresa).add(anexo);
            }

            // Cargar spinner_empresa
            ArrayAdapter<String> empresaAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, empresasList);
            empresaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEmpresa.setAdapter(empresaAdapter);

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);

            // Leer datos
            int elLocal = sharedPreferences.getInt("ES_LOCAL",-1);
            int laEmpresa = sharedPreferences.getInt("ES_EMPRESA",-1);
            System.out.println("Empresa-->"+laEmpresa+" ES_LOCAL--->"+elLocal);
            if (laEmpresa >= 0 && laEmpresa < empresasList.size()) {
                spinnerEmpresa.setSelection(laEmpresa);
                String selectedEmpresa = empresasList.get(laEmpresa);
                System.out.println("Deberia estar seleccionado-->"+selectedEmpresa);
                List<String> anexos = anexosMap.get(selectedEmpresa);
                if (anexos != null && !anexos.isEmpty()) {
                    ArrayAdapter<String> localAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, anexos);
                    localAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLocal.setAdapter(localAdapter);
                    // Validar que el local seleccionado sea válido
                    if (elLocal >= 0 && elLocal < anexos.size()) {
                        spinnerLocal.setSelection(elLocal);
                        System.out.println("terror");
                    } else {
                        System.out.println("El local seleccionado no es válido o está fuera de rango.");
                    }
                } else {
                    System.out.println("No se encontraron anexos para la empresa seleccionada.");
                }
            } else {
                if(elLocal==-1) {
                    String selectedEmpresa = empresasList.get(0);
                    List<String> anexos = anexosMap.get(selectedEmpresa);
                    if (anexos != null && !anexos.isEmpty()) {
                        ArrayAdapter<String> localAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, anexos);
                        localAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerLocal.setAdapter(localAdapter);
                    }
                }
            }

            // Manejar selección de spinner_empresa para cargar anexos
            spinnerEmpresa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isInitialSetup) {
                        isInitialSetup = false;
                        return; // Salir sin ejecutar la lógica adicional
                    }
                    String selectedEmpresa = empresasList.get(position);
                    List<String> anexos = anexosMap.get(selectedEmpresa);
                    System.out.println("Cuando cargas-->");
                    // Cargar spinner_local
                    ArrayAdapter<String> localAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, anexos);
                    localAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLocal.setAdapter(localAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // No acción requerida
                }
            });

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al cargar datos de empresas.json.", Toast.LENGTH_SHORT).show();
        }
    }

    public void createJsonFile() {
        try {
            // Crear el objeto JSON principal
            JSONObject rootObject = new JSONObject();

            // Crear un JSONArray para las empresas
            JSONArray empresasArray = new JSONArray();

            // Crear los objetos JSON para las empresas
            JSONObject empresa1 = new JSONObject();
            empresa1.put("id", 1);
            empresa1.put("empresa", "Cofaco");
            empresa1.put("anexo", "San Andres");

            JSONObject empresa2 = new JSONObject();
            empresa2.put("id", 2);
            empresa2.put("empresa", "Cofaco");
            empresa2.put("anexo", "El estaños");

            JSONObject empresa3 = new JSONObject();
            empresa3.put("id", 3);
            empresa3.put("empresa", "Cititex");
            empresa3.put("anexo", "El estaños");

            JSONObject empresa4 = new JSONObject();
            empresa4.put("id", 4);
            empresa4.put("empresa", "Cititex");
            empresa4.put("anexo", "Crisolita");

            // Añadir las empresas al JSONArray
            empresasArray.put(empresa1);
            empresasArray.put(empresa2);
            empresasArray.put(empresa3);
            empresasArray.put(empresa4);

            // Añadir el JSONArray de empresas al objeto principal
            rootObject.put("empresas", empresasArray);

            // Añadir otros datos al objeto principal
            rootObject.put("potencia", 15);
            rootObject.put("dispositivo", "");
            rootObject.put("es_local", 2);

            // Convertir el objeto JSON a un string
            String jsonString = rootObject.toString(4); // '4' para la indentación (formato bonito)

            // Crear el directorio INI si no existe
            File directory = new File(Environment.getExternalStorageDirectory(), "INI");
            if (!directory.exists()) {
                Log.d("DirectoryCheck", "Carpeta INI no existe. Creándola...");
                directory.mkdirs();
            } else {
                Log.d("DirectoryCheck", "Carpeta INI ya existe.");
            }

            // Crear el archivo "empresas.json" dentro de la carpeta INI
            File currentFile = new File(directory, "empresas.json");

            // Escribir el contenido JSON en el archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, false))) {
                writer.write(jsonString);
                writer.flush();
            }

            // Confirmación visual
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Datos guardados en el archivo: " + currentFile.getName(), Toast.LENGTH_SHORT).show();
            }

            Log.d("FilePath", "Archivo guardado en: " + currentFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Error al crear el archivo JSON", Toast.LENGTH_SHORT).show();
            }

        }
    }
    public void readJsonFile() {
        try {
            // Ruta del archivo JSON
            File directory = new File(Environment.getExternalStorageDirectory(), "INI");
            File currentFile = new File(directory, "empresas.json");
            System.out.println("VAAAA");
            // Verificar si el archivo existe
            if (!currentFile.exists()) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "El archivo no existe", Toast.LENGTH_SHORT).show();
                }

                Log.d("FileRead", "El archivo empresas.json no existe");
                return;
            }

            // Leer el archivo y convertirlo a un String
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }

            // Convertir el contenido a un JSONObject
            String jsonString = stringBuilder.toString();
            JSONObject rootObject = new JSONObject(jsonString);

            // Obtener el JSONArray de empresas
            JSONArray empresasArray = rootObject.getJSONArray("empresas");

            // Recorrer el JSONArray y mostrar los datos de las empresas
            for (int i = 0; i < empresasArray.length(); i++) {
                JSONObject empresa = empresasArray.getJSONObject(i);
                int id = empresa.getInt("id");
                String nombreEmpresa = empresa.getString("empresa");
                String anexo = empresa.getString("anexo");

                Log.d("FileRead", "Empresa ID: " + id + ", Nombre: " + nombreEmpresa + ", Anexo: " + anexo);
            }

            // Obtener otros datos del JSON
            int potencia = rootObject.getInt("potencia");
            String dispositivo = rootObject.getString("dispositivo");
            int esLocal = rootObject.getInt("es_local");

            // Mostrar los otros datos
            Log.d("FileRead", "Potencia: " + potencia);
            Log.d("FileRead", "Dispositivo: " + dispositivo);
            Log.d("FileRead", "Es Local: " + esLocal);

            // Confirmación visual
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Datos leídos desde el archivo", Toast.LENGTH_SHORT).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Error al leer el archivo JSON", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Método para verificar si el permiso ha sido concedido
    private void checkStoragePermission() {
        if (getActivity() != null && getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // El permiso no ha sido concedido, solicita el permiso
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            // El permiso ya ha sido concedido, puedes realizar la operación
            //readJsonFile();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso ha sido concedido
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Permiso concedido para leer archivos", Toast.LENGTH_SHORT).show();
                }

                // Llamar a la función para leer el archivo JSON
                //readJsonFile();
            } else {
                // El permiso fue denegado
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Permiso denegado para leer archivos", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
}
