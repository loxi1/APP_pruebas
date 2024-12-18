package com.ubx.rfid_demo.pojo;

public class TempBean {
    String epc="";
    String tid="";
    String rssi="";
    boolean isGetTid;//Determinar si se ha obtenido tid
    int count=0;//Número de veces bañadas;

    public TempBean() {

    }

    public TempBean(String epc,String rssi){
        this.epc=epc;
        this.rssi=rssi;
    }

    public boolean isGetTid() {
        return isGetTid;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setGetTid(boolean getTid) {
        isGetTid = getTid;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}
