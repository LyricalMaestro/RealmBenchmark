package com.lyricaloriginal.realmbenchmark;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * ベンチマーク用画面のActivityです。
 */
public class MainActivity extends AppCompatActivity {

    //  検証に使うデータ数
    private static final int DATA_NUM = 300;
    private static final String[] RECORD_NUM_LIST = {"300", "2000", "20000", "200000", "1000000"};
    private AsyncTask<Void, String, Void> mBenchmarkTask = null;
    private Spinner mSpinner;
    private Button mRealmBtn;
    private Button mSqliteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, RECORD_NUM_LIST);
        mSpinner = (Spinner) findViewById(R.id.record_num_spinner);
        mSpinner.setAdapter(adapter);

        mRealmBtn = (Button) findViewById(R.id.start_benchmark_realm_btn);
        mRealmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperatorUiEnable(false);
                int recordNum = Integer.valueOf(mSpinner.getSelectedItem().toString());
                startBenchmarkRealm(recordNum);
            }
        });
        mSqliteBtn = (Button) findViewById(R.id.start_benchmark_sqlite_btn);
        mSqliteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOperatorUiEnable(false);
                int recordNum = Integer.valueOf(mSpinner.getSelectedItem().toString());
                startSqliteBenchmark(recordNum);
            }
        });

        if (savedInstanceState != null) {
            mSpinner.setSelection(savedInstanceState.getInt("SELECT_POSITION"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("SELECT_POSITION", mSpinner.getSelectedItemPosition());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBenchmarkTask != null) {
            mBenchmarkTask.cancel(true);
        }
    }

    /**
     * Realmを使ったベンチマーク
     */
    private void startBenchmarkRealm(final int recordNum) {
        AsyncTask<Void, String, Void> task = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Realm realm = null;
                try {
                    //  データ挿入
                    publishProgress("Start benchmark with " + recordNum + " records. (Realm)");
                    long start = System.currentTimeMillis();
                    realm = Realm.getInstance(getApplicationContext());
                    insertData(realm, recordNum);
                    publishProgress("Insert Completed " + (System.currentTimeMillis() - start) + "ms");

                    //  データ読み込み
                    start = System.currentTimeMillis();
                    final RealmResults<Address> results = realm.where(Address.class).findAll();  //  クエリの取得
                    for (Address address : results) {                        //ここで全データ読み込んでみる
                    }
                    publishProgress("Query AllRecord Completed " + (System.currentTimeMillis() - start) + "ms");

                    //  データ読み込み2
                    start = System.currentTimeMillis();
                    loadFewData(realm, recordNum);
                    publishProgress("Query FewRecord by many Completed " + (System.currentTimeMillis() - start) + "ms");

                    //  削除
                    start = System.currentTimeMillis();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.clear(Address.class);
                        }
                    });
                    publishProgress("Clear Completed " + (System.currentTimeMillis() - start) + "ms");
                } finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                TextView textView = (TextView) findViewById(R.id.log_text_view);
                textView.append(values[0] + "\r\n");
            }

            @Override
            protected void onCancelled(Void aVoid) {
                super.onCancelled(aVoid);
                if (mBenchmarkTask != null) {
                    mBenchmarkTask = null;
                    setOperatorUiEnable(true);
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mBenchmarkTask != null) {
                    mBenchmarkTask = null;
                    setOperatorUiEnable(true);
                    Toast.makeText(getApplicationContext(), "完了", Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
        mBenchmarkTask = task;
    }

    /**
     * SQLiteを使ったベンチマーク
     */
    private void startSqliteBenchmark(final int recordNum) {
        AsyncTask<Void, String, Void> task = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase db = null;
                try {
                    //  データ挿入
                    publishProgress("Start benchmark with " + recordNum + " records. (SQLite)");
                    long start = System.currentTimeMillis();
                    AddressDbOpenHelper helper = new AddressDbOpenHelper(getApplicationContext());
                    db = helper.getWritableDatabase();
                    insertData(db, recordNum);
                    publishProgress("Insert Completed " + (System.currentTimeMillis() - start) + "ms");

                    //  データ読み込み
                    start = System.currentTimeMillis();
                    loadData(db);
                    publishProgress("Query AllRecord Completed " + (System.currentTimeMillis() - start) + "ms");

                    //  データ読み込み2
                    start = System.currentTimeMillis();
                    loadFewData(db, recordNum);
                    publishProgress("Query FewRecord by many Completed " + (System.currentTimeMillis() - start) + "ms");

                    //  削除
                    start = System.currentTimeMillis();
                    clearData(db);
                    publishProgress("Clear Completed " + (System.currentTimeMillis() - start) + "ms");
                } finally {
                    if (db != null) {
                        db.close();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                TextView textView = (TextView) findViewById(R.id.log_text_view);
                textView.append(values[0] + "\r\n");
            }

            @Override
            protected void onCancelled(Void aVoid) {
                super.onCancelled(aVoid);
                if (mBenchmarkTask != null) {
                    mBenchmarkTask = null;
                    setOperatorUiEnable(true);
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mBenchmarkTask != null) {
                    mBenchmarkTask = null;
                    setOperatorUiEnable(true);
                    Toast.makeText(getApplicationContext(), "完了", Toast.LENGTH_SHORT).show();
                }
            }
        };
        task.execute();
        mBenchmarkTask = task;

    }

    private void insertData(Realm realm, final int dataNum) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < dataNum; i++) {
                    Address address = realm.createObject(Address.class);
                    address.setId(i + 1);
                    address.setPostalCode("1111111");
                    address.setPref("Tokyo");
                    address.setCwtv("Shinagawa");
                    address.setTownArea("Osaki");
                }
            }
        });
    }

    private void loadFewData(Realm realm, int recordNum) {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int startId = random.nextInt(recordNum - 10) + 1;
            int endId = startId + 10;
            RealmResults<Address> results = realm.where(Address.class).
                    between("id", startId, endId).
                    findAll();
            for (Address address : results) {
            }
        }
    }

    private void insertData(SQLiteDatabase db, final int dataNum) {
        try {
            db.beginTransaction();
            for (int i = 0; i < dataNum; i++) {
                ContentValues cv = new ContentValues();
                cv.put("id", i + 1);
                cv.put("postalCode", "1111111");
                cv.put("pref", "Tokyo");
                cv.put("cwtv", "Shinagawa");
                cv.put("townArea", "Osaki");
                db.insert("ADDRESS", null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void loadData(SQLiteDatabase db) {
        Cursor cr = null;
        try {
            cr = db.rawQuery("SELECT * FROM ADDRESS", null);
            if (cr.moveToFirst()) {
                while (cr.moveToNext()) {
                    cr.getInt(0);
                    cr.getString(1);
                    cr.getString(2);
                    cr.getString(3);
                    cr.getString(4);
                }
            }
        } finally {
            if (cr != null) {
                cr.close();
            }
        }
    }

    private void loadFewData(SQLiteDatabase db, int recordNum) {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int startId = random.nextInt(recordNum - 10) + 1;
            int endId = startId + 10;
            String sql = "SELECT * FROM ADDRESS WHERE ID BETWEEN " + startId + " AND " + endId + ";";
            Cursor cr = null;
            try {
                cr = db.rawQuery(sql, null);
                if (cr.moveToFirst()) {
                    while (cr.moveToNext()) {
                        cr.getInt(0);
                        cr.getString(1);
                        cr.getString(2);
                        cr.getString(3);
                        cr.getString(4);
                    }
                }
            } finally {
                if (cr != null) {
                    cr.close();
                }
            }
        }
    }

    private void clearData(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM ADDRESS");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void setOperatorUiEnable(boolean enable) {
        mSpinner.setEnabled(enable);
        mRealmBtn.setEnabled(enable);
        mSqliteBtn.setEnabled(enable);
    }
}
