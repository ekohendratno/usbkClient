package id.kopas.berkarya.usbkclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs" ;
    static SharedPreferences sharedpreferences;

    private final Handler waitHandler = new Handler();
    private final Runnable waitCallback = new Runnable() {
        @Override
        public void run() {

            Intent intent = new Intent(Splash.this, MasukActivity.class);

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

        //Fake wait 2s to simulate some initialization on cold start (never do this in production!)
        waitHandler.postDelayed(waitCallback, 2000);
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
