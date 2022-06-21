package com.example.apka3_dw;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    ProgressDialog mProgressDialog;
    Button pobierzDane,pobierzPlik;
    EditText adres_url;
    TextView typ, rozmiar,pobrano;
    String url;
    ProgressBar progressBar;
    private NotificationManagerCompat notificationManager;
    boolean flaga = false;
    private static final String typ_STR = "typ";
    private static final String rozmiar_STR = "rozmiar";
    private static final String pobrano_STR = "pobrano";
    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    Notification notification;
    int progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ZadanieAsynchroniczne task = new ZadanieAsynchroniczne(this);
        task.delegate=this;
        progressBar = findViewById(R.id.progress);
        adres_url = findViewById(R.id.url);
        typ = findViewById(R.id.typ);
        rozmiar = findViewById(R.id.rozmiar);
        pobierzDane = findViewById(R.id.info);
        pobrano = findViewById(R.id.pobrano_bajtow);
        pobierzDane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = adres_url.getText().toString();
                if (!url.startsWith("https://"))
                    url = "https://" + url;
                task.execute(url);
                System.out.println(task.getmRozmiar());


            }
        });


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            if(!Environment.isExternalStorageManager()){
                try{
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent,101);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent,101);
                }
            }
        }
            pobierzPlik = findViewById(R.id.pobierz);
        pobierzPlik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("A message");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);

                    url = adres_url.getText().toString();
                    notification(MainActivity.this,url);
                    final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                    downloadTask.execute(url);
                    flaga=true;
                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            downloadTask.cancel(true); //cancel the task

                        }
                    });

            }


        });
    }
    public void notification(Context mContext,String url) {


        mBuilder =
                new NotificationCompat.Builder(mContext.getApplicationContext(), "notify_001");
        Intent ii = new Intent(mContext.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(url);
        bigText.setBigContentTitle("Download");
        bigText.setSummaryText("Downloaded");

        mBuilder.setOngoing(true)
                .setContentTitle("Download")
                .setContentText("Dowload of the file")
                .setProgress(100,0,false);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Starting download");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);


        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }



    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        typ = findViewById(R.id.typ);
        outState.putString(typ_STR, typ.getText().toString());
        rozmiar = findViewById(R.id.rozmiar);
        outState.putString(rozmiar_STR, rozmiar.getText().toString());
        pobrano = findViewById(R.id.pobrano_bajtow);
        outState.putString(pobrano_STR, pobrano.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        typ = findViewById(R.id.typ);
        typ.setText(savedInstanceState.getString(typ_STR));
        rozmiar = findViewById(R.id.rozmiar);
        pobrano = findViewById(R.id.pobrano_bajtow);
        rozmiar.setText(savedInstanceState.getString(rozmiar_STR));
        pobrano.setText(savedInstanceState.getString(pobrano_STR));
    }


    @Override
    public void processFinish(String[] output) {
        rozmiar.setText(output[0]);
        typ.setText(output[1]);

    }
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                output = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/pobrany.jpg");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0){
                        publishProgress((int) (total * 100 / fileLength));
                        pobrano.setText(String.valueOf(total));
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mBuilder.setProgress(100,progress[0], false);
            notification = mBuilder.build();
            mNotificationManager.notify(1, notification);
            progressBar.setProgress(progress[0]);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);

        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
        }
    }

}