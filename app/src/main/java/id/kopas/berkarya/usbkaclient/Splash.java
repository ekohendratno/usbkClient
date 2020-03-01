package id.kopas.berkarya.usbkaclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs" ;
    static SharedPreferences sharedpreferences;

    private final Handler waitHandler = new Handler();
    private final Runnable waitCallback = new Runnable() {
        @Override
        public void run() {

            Intent intent = new Intent(Splash.this, MainActivity.class);

            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        String versionName = BuildConfig.VERSION_NAME;

        TextView tv_version = findViewById(R.id.tv_version);
        tv_version.setText(versionName);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        Boolean mobileDataEnabled = false; // Assume disabled
        if(mobileDataEnabled) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Perhatian");
            alertDialogBuilder.setMessage("Matikan data untuk menggunakan app ini").setCancelable(false).setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (android.os.Build.VERSION.SDK_INT > 15) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);//android.provider.Settings.ACTION_SETTINGS //Intent.ACTION_MAIN
                        //intent.setClassName("com.android.settings", "com.android.settings.Settings");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }else{
                        Intent intent=new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                        ComponentName cName = new ComponentName("com.android.phone","com.android.phone.Settings");
                        intent.setComponent(cName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }

                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();

                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            });
            alertDialogBuilder.create().show();
        }else{

            //Fake wait 2s to simulate some initialization on cold start (never do this in production!)
            waitHandler.postDelayed(waitCallback, 2000);
        }

    }

    @Override
    protected void onDestroy() {
        waitHandler.removeCallbacks(waitCallback);
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


}
