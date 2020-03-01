package id.kopas.berkarya.usbkaclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class OfflineActivity extends AppCompatActivity {

    String TAG = "MasukActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


        Intent i = getIntent();
        String error = i.getStringExtra("error");

        TextView logError = findViewById(R.id.logError);
        logError.setText(error);


        String versionName = BuildConfig.VERSION_NAME;

        TextView tv_version = findViewById(R.id.tv_version);
        tv_version.setText(versionName);

        final AppCompatButton clickMasuk = findViewById(R.id.clickMasuk);
        clickMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickMasuk.setBackgroundResource(R.color.colorTextSecondary);
                clickMasuk.setEnabled(false);

                //progressDialogModel.pdMenyiapkanDataLogin(OfflineActivity.this);

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

               // progressDialogModel.hideProgressDialog();
            }
        });


    }

}
