package com.lyricaloriginal.realmbenchmark;

import io.realm.RealmObject;

/**
 * Addressをモデル化したクラスです。<BR>
 * Realm用モデルクラス
 * <p/>
 * Created by LyricalMaestro on 2015/12/14.
 */
public class Address extends RealmObject {
    private int id;
    private String postalCode;
    private String pref;
    private String cwtv;
    private String townArea;

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPref() {
        return pref;
    }

    public void setPref(String pref) {
        this.pref = pref;
    }

    public String getCwtv() {
        return cwtv;
    }

    public void setCwtv(String cwtv) {
        this.cwtv = cwtv;
    }

    public String getTownArea() {
        return townArea;
    }

    public void setTownArea(String townArea) {
        this.townArea = townArea;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
