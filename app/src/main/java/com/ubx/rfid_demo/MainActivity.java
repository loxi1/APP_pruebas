package com.ubx.rfid_demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ubx.rfid_demo.pojo.TagScan;

import com.ubx.rfid_demo.ui.main.*;

import com.ubx.usdk.USDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.util.QueryMode;
import com.ubx.usdk.util.SoundTool;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ListView;

import com.ubx.rfid_demo.db.DBHelper;
import android.content.ContentValues;

import android.os.Environment;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private String[] menuItems = {"Escanear", "Configurar", "Salir"};
    private int[] menuIcons = {R.drawable.icon1, R.drawable.icon3, R.drawable.icon4};  // Aquí se pasan los iconos
    public static final String TAG = "usdk";

    public  boolean RFID_INIT_STATUS = false;
    public RfidManager mRfidManager;
    public List<String> mDataParents;
    public List<TagScan> tagScanSpinner;
    private List<Fragment> fragments ;
    private ViewPager viewPager ;
    private TabLayout tabs;
    public int readerType = 0;

    private int power_val = 0;
    private int _esEmpresa = -1;
    private int _esLocal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        mDataParents = new ArrayList<>();
        tagScanSpinner = new ArrayList<>();

        initRfid();
//        initRfidService();

        // Configuración del menú lateral
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.drawer_list);

        // Configurar el adaptador del menú lateral
        DrawerAdapter adapter = new DrawerAdapter(this, menuItems, menuIcons);
        drawerList.setAdapter(adapter);

        // Configurar el ActionBarDrawerToggle para abrir/cerrar el Drawer
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Configurar el ActionBar para que muestre el ícono del menú
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        power_val = sharedPreferences.getInt("OUTPUT_POWER", 15);
        _esEmpresa = sharedPreferences.getInt("ES_EMPRESA", -1);
        _esLocal = sharedPreferences.getInt("ES_LOCAL", -1);

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            if(_esLocal == -1 || _esEmpresa == -1) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ConfFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TagScanFragment.newInstance(this))
                        .commit();
            }
        }

        // Agregar manejo de clics en los elementos del menú
        drawerList.setOnItemClickListener((parent, view, position, id) -> handleMenuClick(position));
    }

    private void handleMenuClick(int position) {
        switch (position) {
            case 0: // Escanear
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, TagScanFragment.newInstance(this))
                        .commit();
                break;

            /*case 1: // Site (anteriormente Configurar)
                SaveData();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SiteFragment())
                        .commit();
                break;*/

            case 1: // Configuracion
                // Aquí puedes cargar otro fragmento o actividad
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ConfFragment())
                        .commit();
                break;

            /*case 3:// Pendientes
                // Aquí puedes cargar otro fragmento o actividad
                break;*/

            case 2: // Salir
                finish();
                break;
        }
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initRfid() {
        // Obtenga la instancia de RFID en la devolución de llamada asincrónica
        USDKManager.getInstance().init(BaseApplication.getContext(),new USDKManager.InitListener() {
            @Override
            public void onStatus(USDKManager.STATUS status) {
                if ( status == USDKManager.STATUS.SUCCESS) {
                    Log.d(TAG, "initRfid()  success.");
                    mRfidManager =   USDKManager.getInstance().getRfidManager();
                    ((TagScanFragment)fragments.get(0)).setCallback();

                    SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);

                    // Leer datos
                    byte outputPower = (byte) power_val;

                    mRfidManager.setOutputPower(outputPower);
                    Log.d(TAG,"Que valor es "+power_val);
                    Log.d(TAG, "initRfid: getDeviceId() = " +mRfidManager.getDeviceId());

                    readerType =  mRfidManager.getReaderType();//80es de corta distancia, otros son de larga distancia


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"module："+readerType,Toast.LENGTH_LONG).show();
                        }
                    });

                    Log.d(TAG, "initRfid: GetReaderType() = " +readerType );
                }else {
                    Log.d(TAG, "initRfid  fail.");
                }
            }
        });

    }

    /**
     *
     * Establecer modo de consulta
     * @param mode
     */
    private void setQueryMode(int mode){
        mRfidManager.setQueryMode(QueryMode.EPC_TID);
    }

    /**
     * Escribir etiquetas por TID
     */
    private void writeTagByTid(){
        //Método de escritura (no es necesario seleccionar primero)
        String tid = "24 length TID";
        String writeData = "need write EPC datas ";
        mRfidManager.writeTagByTid(tid,(byte) 0,(byte) 2,"00000000".getBytes(),writeData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        SoundTool.getInstance(BaseApplication.getContext()).release();
        RFID_INIT_STATUS = false;
        if (mRfidManager != null) {
            mRfidManager.disConnect();
            mRfidManager.release();

            Log.d(TAG, "onDestroyView: rfid close");
//            System.exit(0);
        }
    }

    /**
     * Establecer tiempo de inventario
     * @param interal 0-200 ms
     */
    private void setScanInteral(int interal){
        int setScanInterval =   mRfidManager.setScanInterval( interal);
        Log.v(TAG,"--- setScanInterval()   ----"+setScanInterval);
    }

    /**
     * Obtener tiempo de inventario
     */
    private void getScanInteral(){
        int getScanInterval =   mRfidManager.getScanInterval( );
        Log.v(TAG,"--- getScanInterval()   ----"+getScanInterval);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);//Método llamado antes de que aparezca el menú
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_rate_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == 523 &&  event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0){
            //TODO prensa



            return true;
        }else if (event.getKeyCode() == 523 &&  event.getAction() == KeyEvent.ACTION_UP && event.getRepeatCount() == 0){
            //TODO elevar



            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public static class LocalFragment {
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_local, container, false);
        }
    }

    public void SaveData() {
        DBHelper dbHelper = new DBHelper(this); // Cambiado 'context' por 'this'
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", "Cofaco");
        values.put("ruta", "\\cofaco.com\\DFS\\control_despacho\\cofaco");
        db.insert("t_empresas", null, values);

        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
    }
    public int setRfidOutputPower(int power) {
        if (mRfidManager != null) {
            return mRfidManager.setOutputPower((byte) power);
        }
        return -1; // Indica error si el mRfidManager no está disponible
    }
}