package com.ubx.rfid_demo.ui.main;


import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.ubx.usdk.USDKManager;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.rfid.aidl.RfidDate;
import com.ubx.usdk.rfid.util.CMDCode;
import com.ubx.usdk.rfid.util.ErrorCode;
import com.ubx.usdk.util.QueryMode;
import com.ubx.usdk.util.SoundTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Una simple {@link Fragment} subclass.
 * Usa el  {@link TagScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagScanFragment extends Fragment {

    public static final String TAG = "usdk-"+TagScanFragment.class.getSimpleName();
    private List<TagScan> data;
    private HashMap<String, TagScan> mapData;
    private ScanCallback callback  ;
    private ScanListAdapterRv scanListAdapterRv;
    private static  MainActivity mActivity;
    private int tagTotal = 0;
    private File currentFile, currentFile_KO;//Para crear un archivo
    private Map<String, String> TempPO = new HashMap<String, String>();
    private Map<String, String> TempPO_KO = new HashMap<String, String>();
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagScanFragment newInstance(MainActivity activity) {
        mActivity = activity;
        TagScanFragment fragment = new TagScanFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tag_scan, container, false);
    }

    private Button scanStartBtn,btnStartL,btnConnect,btnDisConnect,scanSaveBtn, scanIncidenceBtan;
    private CheckBox checkBox;
    private RecyclerView scanListRv;
    private TextView scanCountText,scanTotalText,textFirmware, nroPO;
    private String LastPO = null, DatePO = null, DateNow = null, HMSPO = null, HMSNow = null, LastFilePO = null, Empresa_ = null, Anexo_ = null;
    private String LastPO_KO = null, DatePO_KO = null, DateNow_KO = null, HMSPO_KO = null,HMSNow_KO = null, LastFilePO_KO =null;
    private static final String HEADER = "Empresa,Anexo,PO,Fecha,Hora,RFID";
    @Override
    public void onViewCreated( View view,   Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nroPO = view.findViewById(R.id.nropo);
        checkBox = view.findViewById(R.id.checkBox);
        scanStartBtn = view.findViewById(R.id.scan_start_btn);
        scanListRv = view.findViewById(R.id.scan_list_rv);
        scanSaveBtn= view.findViewById(R.id.scan_save_btn);
        scanIncidenceBtan = view.findViewById(R.id.stop_incidencia_btn);

        scanCountText= view.findViewById(R.id.scan_count_text);
        //scanTotalText= view.findViewById(R.id.scan_total_text);
        textFirmware = view.findViewById(R.id.text_firmware);
        System.out.println("<----Empezando--->");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        Empresa_ = sharedPreferences.getString("EMPRESA","");
        Anexo_ = sharedPreferences.getString("ANEXO","");
        System.out.println(Empresa_+" <---> "+Anexo_);
        scanIncidenceBtan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdded()) {
                    String nroPOValue_KO = nroPO.getText().toString().trim();

                    if (TextUtils.isEmpty(nroPOValue_KO)) {
                        Toast.makeText(mActivity,"El número de PO no puede estar vacío",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nroPOValue_KO.length() < 8) {
                        Toast.makeText(mActivity, "El número de PO debe tener más de 8 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    File directory_KO = new File(Environment.getExternalStorageDirectory(), "RFIDLogs/Incidencia");
                    if (!directory_KO.exists()) {
                        System.out.println("NOOOO Existe la carpeta");
                        directory_KO.mkdirs();
                    } else {
                        System.out.println("SIIII Existe la carpeta");
                    }

                    if (mActivity.RFID_INIT_STATUS) {
                        if (scanStartBtn.getText().equals(getString(R.string.btn_stop_Inventory))) {

                            scanIncidenceBtan.setVisibility(View.GONE);
                            scanStartBtn.setText(getString(R.string.btInventory));
                            setScanStatus(false );

                            if(!nroPOValue_KO.equals(LastPO_KO)) {
                                LastPO_KO = nroPOValue_KO;
                                DatePO_KO = new SimpleDateFormat("yyyyMMdd").format(new Date());
                                HMSPO_KO = new SimpleDateFormat("HHmm ss").format(new Date());
                                HMSPO_KO = HMSPO_KO.replaceAll("\\s+", "");
                                LastFilePO_KO = "KO_"+LastPO_KO + "_" + DatePO_KO + "_" + HMSPO_KO+ ".csv";

                                currentFile_KO = new File(directory_KO, LastFilePO_KO);
                                Log.d("FilePath", directory_KO.getAbsolutePath());
                            }

                            scanSaveBtn.setVisibility(View.GONE);

                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile_KO, true))) {
                                writer.write("\uFEFF"); // Escribe el BOM al inicio
                                if (currentFile_KO.length() == 0) {
                                    writer.write(HEADER);
                                    writer.newLine();
                                }
                                for (TagScan tag : mapData.values()) {
                                    if (TempPO_KO.containsKey(tag.getEpc())) {
                                        System.out.println("Si Existe");
                                    } else {
                                        //String line = tag.getEpc() + "," + tag.getTid() + "," + tag.getRssi() + "," + tag.getCount();
                                        DateNow_KO = new SimpleDateFormat("yyyyMMdd").format(new Date());
                                        HMSNow_KO = new SimpleDateFormat("HH:mm:ss").format(new Date());
                                        String line =  Empresa_+ "," +Anexo_+ ","+LastPO_KO + "," + DateNow_KO + "," + HMSNow_KO + "," + tag.getEpc();
                                        writer.write(line);
                                        writer.newLine();
                                    }
                                    TempPO_KO.put(tag.getEpc(), tag.getEpc());
                                }
                                Toast.makeText(mActivity, "Datos guardados en el archivo: " + currentFile_KO.getName(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(mActivity, "Error al guardar los datos.", Toast.LENGTH_SHORT).show();
                            }
                            clean_scan();
                        }
                    }
                }
            }
        });

        scanSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdded()) {
                    if (scanStartBtn.getText().equals(getString(R.string.btn_stop_Inventory))) {
                        Toast.makeText(mActivity, "Detener el escaneo.", Toast.LENGTH_SHORT).show();
                    } else {
                        nroPO.setText("");
                        scanCountText.setText("0");

                        // Limpia los datos de la lista y notifica al adaptador
                        if (mapData != null) {
                            mapData.clear();
                        }

                        if (data != null) {
                            data.clear();
                        }

                        if (scanListAdapterRv != null) {
                            scanListAdapterRv.setData(data);
                            scanListAdapterRv.notifyDataSetChanged();
                        }

                        DatePO = null;
                        HMSPO = null;
                        LastFilePO = null;
                        LastPO = null;

                        if (TempPO != null) {
                            TempPO.clear();
                        }

                        if (TempPO_KO != null) {
                            TempPO_KO.clear();
                        }

                        Toast.makeText(mActivity, "Datos de la lista limpiados.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Fragmento no adjunto al contexto. Operación abortada.");
                }
            }
        });

        scanStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdded()) {
                    String nroPOValue = nroPO.getText().toString().trim();

                    if (TextUtils.isEmpty(nroPOValue)) {
                        Toast.makeText(mActivity,"El número de PO no puede estar vacío",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (nroPOValue.length() < 8) {
                        Toast.makeText(mActivity, "El número de PO debe tener más de 8 caracteres", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    File directory = new File(Environment.getExternalStorageDirectory(), "RFIDLogs/Reportes");
                    if (!directory.exists()) {
                        System.out.println("NOOOO Existe la carpeta");
                        directory.mkdirs();
                    } else {
                        System.out.println("SIIII Existe la carpeta");
                    }

                    if (mActivity.RFID_INIT_STATUS) {
                        if (scanStartBtn.getText().equals(getString(R.string.btInventory))) {
                            scanIncidenceBtan.setVisibility(View.VISIBLE);
                            System.out.println("Btn era Incio");
                            setCallback();
                            scanStartBtn.setText(getString(R.string.btn_stop_Inventory));
                            setScanStatus(true);
                            scanSaveBtn.setVisibility(View.VISIBLE);
                            if(!nroPOValue.equals(LastPO)) {
                                LastPO = nroPOValue;
                                DatePO = new SimpleDateFormat("yyyyMMdd").format(new Date());
                                HMSPO = new SimpleDateFormat("HHmm ss").format(new Date());
                                HMSPO = HMSPO.replaceAll("\\s+", "");
                                LastFilePO = LastPO + "_" + DatePO + "_" + HMSPO+ ".csv";

                                currentFile = new File(directory, LastFilePO);
                                Log.d("FilePath", directory.getAbsolutePath());
                            }
                        } else {
                            System.out.println("Btn era Detener");
                            scanIncidenceBtan.setVisibility(View.GONE);
                            scanStartBtn.setText(getString(R.string.btInventory));
                            setScanStatus(false );
                            scanSaveBtn.setVisibility(View.GONE);
                            if (mapData.isEmpty()) {
                                Toast.makeText(mActivity, "No hay datos para guardar.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //PO, DateNow, HMSNow, tag.getEpc()
                            //Array<
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile, true))) {
                                writer.write("\uFEFF"); // Escribe el BOM al inicio
                                if (currentFile.length() == 0) {
                                    writer.write(HEADER);
                                    writer.newLine();
                                }
                                for (TagScan tag : mapData.values()) {
                                    if (TempPO.containsKey(tag.getEpc())) {
                                        System.out.println("Si Existe");
                                    } else {
                                        //String line = tag.getEpc() + "," + tag.getTid() + "," + tag.getRssi() + "," + tag.getCount();
                                        DateNow = new SimpleDateFormat("yyyyMMdd").format(new Date());
                                        HMSNow = new SimpleDateFormat("HH:mm:ss").format(new Date());
                                        String line =  Empresa_+ "," +Anexo_+ ","+LastPO + "," + DateNow + "," + HMSNow + "," + tag.getEpc();
                                        writer.write(line);
                                        writer.newLine();
                                    }
                                    TempPO.put(tag.getEpc(), tag.getEpc());
                                }
                                Toast.makeText(mActivity, "Datos guardados en el archivo: " + currentFile.getName(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(mActivity, "Error al guardar los datos.", Toast.LENGTH_SHORT).show();
                            }
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
            scanListAdapterRv.setData(data);
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
}