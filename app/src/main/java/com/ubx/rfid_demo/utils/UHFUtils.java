package com.ubx.rfid_demo.utils;

import android.content.Context;
import android.util.Log;

public class UHFUtils {

    static RFIDWithUHFUART C72_mReader = null;

   static int loopFlagInT = 1;
   static String C72_leerchips = "";
   static String C72_respuesta = "";
   static int C72_inventoryFlag = 1;
   static String C72_estado = "";
   static boolean C72_startInventoryTag = false;
   static int C72_contectado = 0;
   static boolean LeerRfid = callWLProcedure_boolean("C72_LeerRfid");
   static String C72_gatillo = "";

   static String C72_strTid;
   static String C72_strResult;

    public static void C72_leer_todo(Context context) {



//        UHFTAGInfo C72_readTagFromBuffer;

        try {


            C72_mReader = RFIDWithUHFUART.getInstance();
            if (C72_mReader != null) {
                C72_estado = "El reader está activado";
            } else {
                C72_estado = "El reader está desactivado";
            }
            callWLProcedure("readerestado", C72_estado);
        } catch (Exception ex) {
            C72_estado = "Desactivado";
            callWLProcedure("readerestado", C72_estado);
            C72_contectado = 0;
        }


        C72_mReader.setScanCallback(new RFIDWithUHFUART.ReadTagCallback() {
            @Override
            public void init(boolean initSuccess) {

                C72_mReader.startInventoryTag();


            }

            @Override
            public void readTag(String epc, String tid) {
                Log.e("uhf","epc:"+epc+"    tid:"+tid);
                UHFTAGInfo res = new UHFTAGInfo();
                res.setEPC(epc);
                res.setTid(tid);

                C72_inventoryFlag++;

                if (res != null) {
                    C72_respuesta = "Ok";
                    C72_strTid = res.getTid();
                    if (C72_strTid.length() != 0 && !C72_strTid.equals("0000000" +
                            "000000000") && !C72_strTid.equals("000000000000000000000000")) {
                        C72_strResult = "TID:" + C72_strTid + "\n";
                    } else {
                        C72_strResult = "";
                    }
                    C72_leerchips = res.getEPC();
                    callWLProcedure("leerchip", C72_leerchips, C72_respuesta, C72_inventoryFlag);

                } else {

                    C72_respuesta = "NULL";
                    C72_leerchips = "";
                    callWLProcedure("leerchip", C72_leerchips, C72_respuesta, C72_inventoryFlag);
                }

                LeerRfid = callWLProcedure_boolean("C72_LeerRfid");


            }
        });

        C72_mReader.init(context);





    }

    /**
     * stop Inventory
     */
    public static void stopInventory(){
        if (C72_mReader!=null) {
            C72_mReader.stopInventory();
        }
        C72_respuesta = "STOP";
        C72_leerchips = "STOP";

        callWLProcedure("leerchip", C72_leerchips, C72_respuesta, C72_inventoryFlag);
    }























    private static boolean callWLProcedure_boolean(String m1) {

        return false;
    }

    private static void callWLProcedure(String m1, String m2) {

    }

    private static void callWLProcedure(String m1, String m2, String m3, int i) {

    }


}
