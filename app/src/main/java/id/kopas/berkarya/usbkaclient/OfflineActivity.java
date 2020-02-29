package id.kopas.berkarya.usbkaclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

public class OfflineActivity extends AppCompatActivity {

    String TAG = "MasukActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


        AppCompatButton clickMasuk = findViewById(R.id.clickMasuk);
        clickMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialogModel.pdMenyiapkanDataLogin(OfflineActivity.this);

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                progressDialogModel.hideProgressDialog();
            }
        });


    }

}
