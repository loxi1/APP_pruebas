package com.ubx.rfid_demo.ui.main;


import android.os.Build;
import android.os.Bundle;
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
import com.ubx.rfid_demo.pojo.TempBean;
import com.ubx.rfid_demo.utils.SoundTool;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.util.QueryMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TagScanDuanjuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagScanDuanjuFragment extends Fragment {

    public static final String TAG = "usdk-"+TagScanDuanjuFragment.class.getSimpleName();
    private List<TagScan> data;
    private HashMap<String, TagScan> mapData;
    private ScanCallback callback  ;
    private ScanListAdapterRv scanListAdapterRv;
    private static  MainActivity mActivity;
    private int tagTotal = 0;
    private Map<String,TagScan> tempDate=new HashMap<>();
    private boolean workSymbol=false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagScanDuanjuFragment newInstance(MainActivity activity) {
        mActivity = activity;
        TagScanDuanjuFragment fragment = new TagScanDuanjuFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tag_scan, container, false);
    }

    private Button scanStartBtn;
    private CheckBox checkBox;
    private RecyclerView scanListRv;
    private TextView scanCountText,scanTotalText,textFirmware, nroOP;

    @Override
    public void onViewCreated( View view,   Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nroOP= view.findViewById(R.id.nropo);
        checkBox = view.findViewById(R.id.checkBox);
        scanStartBtn = view.findViewById(R.id.scan_start_btn);
        //scanSaveBtn = view.findViewById(R.id.scan_save_btn);
        scanListRv = view.findViewById(R.id.scan_list_rv);

        scanCountText= view.findViewById(R.id.scan_count_text);
        //scanTotalText= view.findViewById(R.id.scan_total_text);
        textFirmware = view.findViewById(R.id.text_firmware);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        scanStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    if (mActivity.RFID_INIT_STATUS) {
                        //writeEpcString("200341166142806876503249","00000000");
                        //Iniciar escaneo
                        if (scanStartBtn.getText().equals("开始扫描")) {
                            setCallback();
                            //Dejar de escanear
                            scanStartBtn.setText("停止扫描");
                            setScanStatus(true);
                        } else {
                            //Iniciar escaneo
                            scanStartBtn.setText("开始扫描");
                            setScanStatus(false);
                        }
                    }else {
                        //RFID no está inicializado
                        Log.d(TAG, "scanStartBtn  RFID未初始化 "  );
                        //RFID no está inicializado
                        Toast.makeText(getActivity(),"RFID未初始化",Toast.LENGTH_SHORT).show();
                    }
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

    @Override
    public void onStart() {
        super.onStart();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivity.mRfidManager!=null) {
                    Log.v(TAG,"--- getFirmwareVersion()   ----");
                    mActivity.RFID_INIT_STATUS = true;
                    String firmware =  mActivity.mRfidManager.getFirmwareVersion();
                    textFirmware.setText("固件：v"+firmware);
                }else {
                    Log.v(TAG,"onStart()  --- getFirmwareVersion()   ----  mActivity.mRfidManager == null");
                }
            }
        }, 5000);
    }

    private void setScanStatus(boolean isScan) {

        if (isScan) {
            workSymbol=true;
            tagTotal = 0;
            if(temp.size()>0){
                temp.clear();
            }
            if(rset.size()>0){
                for (String s : rset) {
                    Log.e(TAG, "setScanStatus: "+s );
                }
                rset.clear();
            }
            if(itemSymbol.size()>0){
                itemSymbol.clear();
            }
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

            Log.v(TAG,"--- customizedSessionTargetInventory()   ----");
            handler.sendEmptyMessage(START_INVENTORY);


        } else {
            workSymbol=false;
            Log.v(TAG,"--- stopInventory()   ----");
            handler.removeCallbacksAndMessages(null);
            mActivity.mRfidManager.stopInventory();

        }
    }

    private long time = 0l;

    /**
     * EPC o tid de lectura única
     */
    private void readTagOnce(){
        //Lee la dirección inicial de TID.读取TID的起始地址。
        //Lea la longitud del TID, si la longitud es 0, lea el número EPC
        String  data =  mActivity.mRfidManager.readTagOnce( (byte) 0, (byte) 0);
        //data 读到的标签值
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
            //El firmware no es compatible
            Toast.makeText(mActivity, "固件不支持", Toast.LENGTH_SHORT).show();
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


    private static final int START_INVENTORY=1;
    private static final int STOP_INVENTORY=2;
    private static final int UPDATE_DATA=3;
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_INVENTORY:
                    mActivity.mRfidManager.startInventory((byte) 1);
                    handler.sendEmptyMessageDelayed(STOP_INVENTORY,1000);
                    break;
                case STOP_INVENTORY:
                    mActivity.mRfidManager.stopInventory();
                    break;
                case UPDATE_DATA:
                    Bundle bundle = msg.getData();
                    String epc = bundle.getString("epc");
                    String tid = bundle.getString("tid");
                    String rssi = bundle.getString("rssi");
                    notiyDatas(epc,tid,rssi);
                    break;

            }

//            byte[] pwd = hexStringToBytes("00000000");
//            long startDate = System.currentTimeMillis();
//
//         String TID = mActivity.mRfidManager.readTag(epc,(byte) 2,(byte) 0,(byte) 6,pwd);
//
//         long endDate = System.currentTimeMillis();
//            Log.e(TAG," ************** 读取TID失败:  "+(endDate-startDate)+" ms");
//         if (!TextUtils.isEmpty(TID)){
//             notiyDatas(epc,TID,rssi);
//         }else {
//             Log.e(TAG,"---------------------读取TID失败-----------------");
//         }

        }
    };

    private   byte[] pwd = hexStringToBytes("00000000");

    private boolean isReadTid = false;
    //Etiquetas almacenadas en cada intervalo.
    private List<TempBean> temp=new ArrayList<>();

    //Se utiliza para determinar si se encuentra tid
    private Map<String,Boolean> itemSymbol=new HashMap<>();

    private Set<String> rset=new HashSet<>();

    class ScanCallback implements IRfidCallback  {



        @Override
        public void onInventoryTag(String EPC, final String TID, final String strRSSI) {
            final String s2 = EPC.replace(" ","");
            Log.e(TAG, "onInventoryTag: xxxx"+EPC );
            temp.add(new TempBean(EPC,strRSSI));
            /*if (TextUtils.isEmpty(TID) && !isReadTid) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isReadTid = true;
                        long startDate = System.currentTimeMillis();
                        String TID = mActivity.mRfidManager.readTag(s2,(byte) 2,(byte) 0,(byte) 6,pwd);
                        long endDate = System.currentTimeMillis();
                        Log.e(TAG," ************** No se pudo leer TID:  "+(endDate-startDate)+" ms");
                        if (!TextUtils.isEmpty(TID)){
                            Message message = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("epc",s2);
                            bundle.putString("tid",TID);
                            bundle.putString("rssi",strRSSI);
                            message.setData(bundle);
                            handler.sendMessage(message);

                        }else {
                            Log.e(TAG,"---------------------No se pudo leer TID-----------------");
                        }
                        isReadTid = false;
                    }
                }).start();*/
        }

        @Override
        public void onInventoryTagEnd() {
            if(workSymbol){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] strs={"E280","E2806995200040031123345D","E28069952000","E2806995200040031123345D030A","E280689400005010A218A905","101048001032"};
                        for (int i = 0; i < strs.length; i++) {
                            for (int j = 0; j < 10; j++) {
                                String tid = mActivity.mRfidManager.readTag(strs[i], (byte) 2, (byte) 0, (byte) 6, pwd);
                                if(tid!=null&&!tid.equals("")){
                                    rset.add(strs[i]+" "+tid);
                                }

                            }


                        }
                        /*for (TempBean tempBean : temp) {
                            String tid="";
//                            E280,E2806995200040031123345D,E28069952000,E2806995200040031123345D030A,E280689400005010A218A905,101048001032

                            tid = mActivity.mRfidManager.readTag(tempBean.getEpc(), (byte) 2, (byte) 0, (byte) 6, pwd);

                            if(tid==null||tid.equals("")){
                                tid="";
                            }else{
                                rset.add("E280689400004010A218B905"+" "+tid);
                            }

                            Log.e(TAG, "run: "+tempBean.getEpc() );
                            *//*if(itemSymbol.get(tempBean.getEpc())==null|| Boolean.FALSE.equals(itemSymbol.get(tempBean.getEpc()))){
                                tid = mActivity.mRfidManager.readTag(tempBean.getEpc(), (byte) 2, (byte) 0, (byte) 6, pwd);
                                if(tid==null||tid.equals("")){
                                    itemSymbol.put(tempBean.getEpc(),false);
                                    tid="";
                                }else{
                                    itemSymbol.put(tempBean.getEpc(),true);
                                }
                            }*//*
                            Message message = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            message.what=UPDATE_DATA;
                            bundle.putString("epc",tempBean.getEpc());
                            bundle.putString("tid",tid);
                            bundle.putString("rssi",tempBean.getRssi());
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                        temp.clear();
                        handler.sendEmptyMessage(START_INVENTORY);*/
                    }
                }).start();
            }else{
                /*Set<String> strings = mapData.keySet();
                for (String string : strings) {
                    Log.e(TAG, "onInventoryTagEnd: "+string );
                }
                Log.e(TAG, "onInventoryTagEnd: ......................." );
                Collection<TagScan> values = mapData.values();
                Set<String>set=new HashSet<>();
                for (TagScan value : values) {
                   set.add(value.getTid());
                }
                for (String s : set) {
                    Log.e(TAG, "onInventoryTagEnd: "+s );
                }*/
                for (String s : rset) {
                    Log.e(TAG, "onInventoryTagEnd: "+s );
                }
            }


        }

    }





    private void notiyDatas(final String s2, final String TID, final String strRSSI){
       /* String mapContainStr = null;
        if (!TextUtils.isEmpty(TID)){
            mapContainStr = TID;
        }else {
            mapContainStr = s2;
        }
        final String mapContainStrFinal = mapContainStr;
        Log.d(TAG, "onInventoryTag: EPC: " + s2);*/
        SoundTool.getInstance(BaseApplication.getContext()).playBeep(2);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                {
                    if(!TID.equals("")){
                        String key=s2+" "+TID;
                        if (mapData.containsKey(key)) {
                            TagScan tagScan = mapData.get(key);
//                            TagScan tagScan = mapData.get(s2);
//                            tagScan.setCount(mapData.get(s2).getCount() + 1);
                            tagScan.setCount(mapData.get(key).getCount() + 1);
//                    tagScan.setTid(TID);
                            tagScan.setRssi(strRSSI);
//                            mapData.put(s2, tagScan);
                            mapData.put(key, tagScan);
                        } else {
                            mActivity.mDataParents.add(s2);
                            TagScan tagScan = new TagScan(s2, TID,strRSSI, 1);
                            mapData.put(key, tagScan);
                            mActivity.tagScanSpinner.add(tagScan);
                        }
                    }
                    /*else{
                        if (mapData.containsKey(s2)) {
                            TagScan tagScan = mapData.get(s2);
                            tagScan.setCount(mapData.get(s2).getCount() + 1);
//                    tagScan.setTid(TID);
                            tagScan.setRssi(strRSSI);
                            mapData.put(s2, tagScan);
                        }
                    }*/

                    long nowTime = System.currentTimeMillis();
                    if ((nowTime - time)>1){
                        time = nowTime;
                        data = new ArrayList<>(mapData.values());
                        Log.d(TAG, "onInventoryTag: data = " + Arrays.toString(data.toArray()));
                        scanListAdapterRv.setData(data);
                        scanCountText.setText(mapData.keySet().size() + "");
                        scanTotalText.setText(++tagTotal + "");
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