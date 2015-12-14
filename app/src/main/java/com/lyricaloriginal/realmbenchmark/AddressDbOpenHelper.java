package com.lyricaloriginal.realmbenchmark;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Address.dbを開くためのヘルパークラスです。
 * Created by LyricalMaestro on 2015/12/15.
 */
public class AddressDbOpenHelper extends SQLiteOpenHelper {

    public AddressDbOpenHelper(Context context) {
        super(context, "address.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ADDRESS(");
        sb.append("postalCode TEXT,");
        sb.append("pref TEXT,");
        sb.append("cwtv TEXT,");
        sb.append("townArea TEXT);");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
