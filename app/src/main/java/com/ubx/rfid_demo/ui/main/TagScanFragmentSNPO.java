package com.ubx.rfid_demo.ui.main;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.rfid_demo.BaseApplication;
import com.ubx.rfid_demo.MainActivity;
import com.ubx.rfid_demo.R;
import com.ubx.rfid_demo.pojo.TagScan;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.util.QueryMode;
import com.ubx.usdk.util.SoundTool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Una simple {@link Fragment} subclass.
 * Usa el  {@link TagScanFragmentSNPO#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagScanFragmentSNPO extends Fragment {

    public static final String TAG = "usdk-"+ TagScanFragmentSNPO.class.getSimpleName();
    private List<TagScan> data;
    private HashMap<String, TagScan> mapData;
    private ScanCallback callback  ;
    private ScanListAdapterRv scanListAdapterRv;
    private static  MainActivity mActivity;
    private int tagTotal = 0;
    private File currentFile = null, currentFile_KO = null, directory_KO = null;//Para crear un archivo
    private Map<String, String> TempPO = new HashMap<String, String>();
    private Map<String, String> TempPO_KO = new HashMap<String, String>();

    private boolean isScanning = false; // Nuevo estado global para manejar el escaneo
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagScanFragmentSNPO newInstance(MainActivity activity) {
        mActivity = activity;
        TagScanFragmentSNPO fragment = new TagScanFragmentSNPO();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tag_scan_sn_po, container, false);
    }

    private Button scanStartBtn;
    private CheckBox checkBox;
    private RecyclerView scanListRv;
    private TextView scanCountText,scanTotalText,textFirmware;
    private static final String HEADER = "Empresa,Anexo,PO,Fecha,Hora,RFID";
    @Override
    public void onViewCreated( View view,   Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkBox = view.findViewById(R.id.checkBox_sn_po);
        scanStartBtn = view.findViewById(R.id.scan_start_btn_sn_po);
        scanListRv = view.findViewById(R.id.scan_list_rv_sn_po);

        scanCountText= view.findViewById(R.id.scan_count_text_sn_po);
        //scanTotalText= view.findViewById(R.id.scan_total_text);
        textFirmware = view.findViewById(R.id.text_firmware_sn_po);

        scanStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdded()) {

                    if (mActivity.RFID_INIT_STATUS) {
                        if (scanStartBtn.getText().equals(getString(R.string.btInventory))) {
                            System.out.println("Btn era Incio");
                            setCallback();
                            scanStartBtn.setText(getString(R.string.btn_stop_Inventory));
                            setScanStatus(true);
                        } else {
                            System.out.println("Btn era Detener");
                            scanStartBtn.setText(getString(R.string.btInventory));
                            setScanStatus(false );
                            clean_scan();
                        }
                    }else {
                        Log.d(TAG, "scanStartBtn  RFID no está inicializado "  );
                        Toast.makeText(getActivity(),"RFID Not initialized",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Fragmento no adjunto al contexto. Operación abortada.");
                }
            }
        });

        mapData = new HashMap<>();


        scanListRv.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        scanListRv.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        scanListAdapterRv = new ScanListAdapterRv(null, getActivity());
        scanListRv.setAdapter(scanListAdapterRv);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mActivity.mRfidManager !=null  ) {
                    if (b) {//Habilitar lectura TID
                        mActivity.mRfidManager.setQueryMode(QueryMode.EPC_TID);
                    } else {//Desactivar lectura TID
                        mActivity.mRfidManager.setQueryMode(QueryMode.EPC);
                    }
                }
            }
        });
    }
    private void clean_scan() {
        scanCountText.setText("0");

        // Limpia los datos de la lista y notifica al adaptador
        if (mapData != null) {
            mapData.clear();
        }

        if (data != null) {
            data.clear();
        }

        if (scanListAdapterRv != null) {
            scanListAdapterRv.setData(new ArrayList<>());
            scanListAdapterRv.notifyDataSetChanged();
        }
    }
    private final int MSG_UPDATE_UI = 0;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_UI:
                    scanListAdapterRv.notifyDataSetChanged();
                    handlerUpdateUI();
                    break;
            }

        }
    };




    @Override
    public void onStart() {
        super.onStart();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && mActivity.mRfidManager!=null) {
                    Log.v(TAG,"--- getFirmwareVersion()   ----");
                    mActivity.RFID_INIT_STATUS = true;
                    String firmware =  mActivity.mRfidManager.getFirmwareVersion();
                    textFirmware.setText(getString(R.string.firmware)+firmware);
                }else {
                    Log.v(TAG,"onStart()  --- getFirmwareVersion()   ----  mActivity.mRfidManager == null");
                }
            }
        }, 5000);
    }

    private void setScanStatus(boolean isScan) {

        if (isScan) {
            tagTotal = 0;
            if (mapData!=null){
                mapData.clear();
            }
            if (mActivity.mDataParents != null){
                mActivity.mDataParents.clear();
            }
            if (mActivity.tagScanSpinner != null){
                mActivity.tagScanSpinner.clear();
            }
            if (data!=null) {
                data.clear();
                scanListAdapterRv.setData(data);
            }

            Log.v(TAG,"--- startInventory() Empezo el inventario 1  ----");
            handlerUpdateUI();
            mActivity.mRfidManager.startInventory( (byte) 0);//Se recomienda utilizar: 0 cuando se cuenta una pequeña cantidad de etiquetas; cuando se cuentan más de 100-200 etiquetas, se recomienda utilizar: 1.
        } else {
            Log.v(TAG,"--- stopInventory()   ----");
            mActivity.mRfidManager.stopInventory();
            handlerStopUI();
        }
    }

    private void handlerUpdateUI(){
        if (mHandler!=null){
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_UI,1000);
        }
    }
    private void handlerStopUI(){
        if (mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }


    private long time = 0l;

    /**
     * EPC o tid de lectura única
     */
    private void readTagOnce(){
        //Lea la dirección inicial del TID.
        //Lea la longitud del TID, si la longitud es 0, lea el número EPC
        String  data =  mActivity.mRfidManager.readTagOnce( (byte) 0, (byte) 0);
        //data leer el valor de la etiqueta
    }

    /**
     * Establecer máscara (inventario de filtro de etiquetas)
     */
    private void setTagMask(){
        mActivity.mRfidManager.setTagMask(2,24,16,"7020");
    }


    /**
     * Escribir datos de etiquetas a través de TID
     * @param TID   TID seleccionado
     * @param Mem   Área de etiqueta: 0-Área de contraseña, los primeros 2 caracteres son la contraseña de destrucción, los últimos 2 caracteres son la contraseña de acceso 1-Área EPC 2-Área TID 3-Área de usuario
     * @param WordPtr  Dirección de palabra inicial para escribir.
     * @param pwd   contraseña
     * @param  datas  Datos a escribir
     */
    private void writeTagByTid(String TID,byte Mem,byte WordPtr,byte[] pwd,String datas){
//                String TID = "E280110C20007642903D094D";
//                 byte[] pwd = hexStringToBytes("00000000");
//                 String datas = "1111111111111111";
        int ret =  mActivity.mRfidManager.writeTagByTid(TID,(byte) 1,(byte) 2, pwd,datas);
        if (ret == -6){
            Toast.makeText(mActivity, getContext().getString(R.string.gj_no_support), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Escribe EPC en una etiqueta al azar
     * @param epc   El valor EPC se escribirá en una cadena hexadecimal
     * @param password  Contraseña de acceso a la etiqueta
     */
    private void writeEpcString(String epc,String password){
        mActivity.mRfidManager.writeEpcString( epc, password);
    }




    class ScanCallback implements IRfidCallback  {



        @Override
        public void onInventoryTag(String EPC, final String TID, final String strRSSI) {

            notiyDatas(EPC,TID,strRSSI);

        }

        /**
         * Devolución de llamada de fin de inventario(Inventory Command Operate End)
         */
        @Override
        public void onInventoryTagEnd()  {
            Log.d(TAG, "onInventoryTag()");
        }
    }


    private void notiyDatas(final String s2, final String TID, final String strRSSI){
        String mapContainStr = null;
        if (!TextUtils.isEmpty(TID)){
            mapContainStr = TID;
        }else {
            mapContainStr = s2;
        }
        final String mapContainStrFinal = mapContainStr;
        Log.d(TAG, "onInventoryTag: EPC: aaaa" + s2);
        SoundTool.getInstance(BaseApplication.getContext()).playBeep(2);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    if (mapData.containsKey(mapContainStrFinal)) {
                        TagScan tagScan = mapData.get(mapContainStrFinal);
                        //tagScan.setCount(mapData.get(mapContainStrFinal).getCount() + 1);
                        //tagScan.setTid(TID);
                        //tagScan.setRssi(strRSSI);
                        System.out.println("Muaa");
                        mapData.put(mapContainStrFinal, tagScan);
                    } else {
                        mActivity.mDataParents.add(s2);

                        TagScan tagScan = new TagScan(s2, TID,strRSSI, 1);
                        mapData.put(mapContainStrFinal, tagScan);
                        mActivity.tagScanSpinner.add(tagScan);
                        System.out.println("Meee");
                    }

                    //scanTotalText.setText(++tagTotal + "");

                    long nowTime = System.currentTimeMillis();
                    if ((nowTime - time)>1000){
                        time = nowTime;
                        data = new ArrayList<>(mapData.values());
                        Log.d(TAG, "onInventoryTag: data = " + Arrays.toString(data.toArray()));
                        scanListAdapterRv.setData(data);
                        scanCountText.setText(mapData.keySet().size() + "");

                    }
                }
            }
        });
    }




    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setCallback();
        }
    }



    public void setCallback(){
        if (mActivity.mRfidManager!=null) {

            if (callback == null){
                callback = new ScanCallback();
            }
            mActivity.mRfidManager.registerCallback(callback);
        }
    }
    /**
     * Convertir cadena hexadecimal en matriz de bytes
     *
     * @param hexString the hex string
     * @return the byte [ ]
     */
    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }

    public void toggleScan() {
        System.out.println("Llego--> toggleScan");
        if (scanStartBtn.getText().equals(getString(R.string.btInventory))) {
            // Simula el clic para iniciar el escaneo

            scanStartBtn.performClick();
            System.out.println("Iniciando escaneo...");
        } else if (scanStartBtn.getText().equals(getString(R.string.btn_stop_Inventory))) {
            // Simula el clic para detener el escaneo
            scanStartBtn.performClick();
            System.out.println("Deteniendo escaneo...");
        }
    }
}