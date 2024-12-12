package com.ubx.rfid_demo.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ubx.usdk.USDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.rfid.aidl.IRfidCallback;
import com.ubx.usdk.rfid.aidl.RfidDate;
import com.ubx.usdk.rfid.util.CMDCode;
import com.ubx.usdk.rfid.util.ErrorCode;


public class RFIDWithUHFUART {

    private static final String TAG = RFIDWithUHFUART.class.getSimpleName();

    private static RfidManager mRfidManager;
    private static RFIDWithUHFUART mRFIDWithUHFUART;
    private static ScanCallback scanCallback;
    private static RfidDate mRfidDate;
    private static ReadTagCallback mReadTagCallback;

    private final static int MSG_T = 0;

    public static RFIDWithUHFUART getInstance() {

        if (mRFIDWithUHFUART == null) {
            mRFIDWithUHFUART = new RFIDWithUHFUART();
            scanCallback = new ScanCallback();
        }
        return mRFIDWithUHFUART;
    }

    private static Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_T){
                if (mRfidManager!=null){
                    mRfidManager.setImpinjFastTid(mRfidManager.getReadId(), true, true);//设置是否盘存EPC+TID
                }
                if (mReadTagCallback!=null){
                    mReadTagCallback.init(true);
                }
            }
        }
    };


    public static void init(Context context) {
// Obtenga la instancia de RFID en la devolución de llamada asincrónica
        USDKManager.getInstance().init(context, new USDKManager.InitListener() {
            @Override
            public void onStatus(USDKManager.STATUS status) {
                if (status == USDKManager.STATUS.SUCCESS) {
                    Log.d(TAG, "initRfidService: Estado exitoso");
                    mRfidManager = USDKManager.getInstance().getRfidManager();
                    // Establecer velocidad en baudios
                    if (mRfidManager.connectCom("/dev/ttyHSL0", 115200)) {
//                        final String mf = mRfidManager.getModuleFirmware();
//                        Log.d(TAG, "initRfidService:   mf：" + mf);
                        mRfidManager.registerCallback(scanCallback);
                        mRfidManager.getFirmwareVersion(mRfidManager.getReadId());

                        handler.sendEmptyMessageDelayed(MSG_T,500);
//                    int outputPower = mRfidManager.getOutputPower(mRfidManager.getReadId());
//                    Log.d(TAG, "initRfidService: outputPower = " + outputPower);
                    }
                } else {
                    Log.d(TAG, "initRfidService: estado fallido。");
                    if (mReadTagCallback!=null){
                        mReadTagCallback.init(false);
                    }
                }
            }
        });


    }

    public static void startInventoryTag() {
        if (mRfidManager != null) {
            mRfidManager.customizedSessionTargetInventory(mRfidManager.getReadId(), (byte) 1, (byte) 0, (byte) 1);
        }
    }

    public static void stopInventory() {
        if (mRfidManager != null) {
            mRfidManager.stopInventory();
        }
    }

    /**
     * set power
     *
     * @param power Interger  10-30
     */
    public static void setPower(int power) {
        if (mRfidManager != null) {
            mRfidManager.setOutputPower(mRfidManager.getReadId(), (byte) power);
        }
    }

    /**
     * Resource recovery
     */
    public static void release() {
        if (mRfidManager != null) {
            mRfidManager.release();
        }
    }

    /**
     * Resource recovery
     */
    public static void setScanCallback(ReadTagCallback callback) {
        if (callback != null) {
            mReadTagCallback = callback;
        }
    }




    static class ScanCallback implements IRfidCallback {


        /**
         * Devolución de llamada de datos de inventario（Inventory TAG Callback）
         *
         * @param b   cmd
         * @param s   pc值
         * @param s1  CRC Check Value
         * @param epc EPC
         * @param b1  Ant
         * @param s3  RSSI
         * @param s4  Frequency
         * @param i
         * @param i1  Inventory Count
         * @param s5  Read id
         * @
         */
        @Override
        public void onInventoryTag(byte b, String s, String s1, String epc, byte b1, String s3, String s4, int i, int i1, String s5) {
//            Log.d(TAG, "onInventoryTag: EPC:" + epc );
//            if (mReadTagCallback!=null){
//                mReadTagCallback.readTag(epc,"");
//            }
        }

        @Override
        public void onInventory(String EPC, String TID, String strRSSI) {
            Log.d(TAG, "onInventoryTagTID: EPC:" + EPC+"     TID:"+TID);
            if (mReadTagCallback!=null){
                mReadTagCallback.readTag(EPC,TID);
            }
        }

        /**
         * 盘存结束回调(Inventory Command Operate End)
         *
         * @param i  ID de antena actual
         * @param i1 Cantidad de etiquetas de inventario del pedido actual
         * @param i2 Velocidad de lectura
         * @param i3 Lecturas totales
         * @param b  instrucción en cmd
         * @
         */
        @Override
        public void onInventoryTagEnd(int i, int i1, int i2, int i3, byte b) {
            Log.d(TAG, "onInventoryTag: # Etiquetas leidas" + i1);
        }

        @Override
        public void onOperationTag(String s, String s1, String s2, String s3, int i, byte b, byte b1) {
        }

        @Override
        public void onOperationTagEnd(int i) {

        }

        @Override
        public void refreshSetting(RfidDate rfidDate) {
            mRfidDate = rfidDate;
            final String power = String.valueOf(rfidDate.getbtAryOutputPower()[0] & 0xFF);
            Log.v(TAG, "power:" + power);
        }

        /**
         * (Devolución de llamada del estado de operación de instrucción)Command operate status
         *
         * @param b  Comando cmd correspondiente CMDCode.class
         * @param b1 Estado de ejecución correspondiente ErrorCode.class
         * @
         */
        @Override
        public void onExeCMDStatus(byte b, byte b1) {
            String format = CMDCode.format(b) + ErrorCode.format(b1);
            Log.v(TAG, "onExeCMDStatus format:" + format);
            if (b == CMDCode.SET_OUTPUT_POWER) {
                String message = b1 == 16 ? "Success" : "failed";
                Log.v(TAG, "Set OutputPower：" + message);
            } else if ((b == CMDCode.GET_OUTPUT_POWER) && b1 == ErrorCode.SUCCESS) {
                Log.v(TAG, "Module OutPutPower get success：" + String.valueOf(mRfidDate.getbtAryOutputPower()[0]));
            } else if (b == CMDCode.SET_ACCESS_EPC_MATCH) {
                Log.v(TAG, "Match EPC：" + (b1 == ErrorCode.SUCCESS ? "Success" : "failed"));
            }


        }
    }

    public interface ReadTagCallback {

        void init(boolean initSuccess);
        void readTag(String epc, String tid);

    }


}
