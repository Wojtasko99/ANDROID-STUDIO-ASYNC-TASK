package com.example.apka3_dw;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.net.ContentHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class ZadanieAsynchroniczne extends AsyncTask<String,String,String[]> {
    private String adres_url,mTyp;
    private int mRozmiar;
    TextView rozmiar_text,typ_text;
    Context mcontext;
    public AsyncResponse delegate = null;
    ProgressDialog dialog;
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    HttpsURLConnection polaczenie;
    public ZadanieAsynchroniczne(Context context){
        this.mcontext=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog= new ProgressDialog(mcontext);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Retrieving...");
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(String... strings) {
        super.onProgressUpdate(strings);

    }

    @Override
    protected void onPostExecute(String s[]) {
        super.onPostExecute(s);
        delegate.processFinish(s);
        dialog.dismiss();
    }

    @Override
    protected String[] doInBackground(String... strings) {
        String stringUrl = strings[0];
        String[] result = new String[2];

        try {
            URL myUrl = new URL(stringUrl);
            polaczenie = (HttpsURLConnection) myUrl.openConnection();
            polaczenie.setRequestMethod("GET");
            mRozmiar = polaczenie.getContentLength();
            mTyp = polaczenie.getContentType();
            result[0] = String.valueOf(mRozmiar);
            result[1] = mTyp;

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(polaczenie != null){
                polaczenie.disconnect();
            }
        }
        return result;
    }

    public String getmTyp() {
        return mTyp;
    }

    public int getmRozmiar() {
        return mRozmiar;
    }
}
