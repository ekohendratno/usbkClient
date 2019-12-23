package id.kopas.berkarya.usbkclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MasukActivity extends AppCompatActivity {

    String TAG = "MasukActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masuk);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


        cekKebutuhanApp();


        final AppCompatEditText alamat = findViewById(R.id.alamat);

        AppCompatButton clickMasuk = findViewById(R.id.clickMasuk);
        clickMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( alamat.getText().toString().equals("") ) {
                    Toast.makeText(getApplicationContext(),"URL kosong!",Toast.LENGTH_SHORT).show();
                }else{

                    progressDialogModel.pdMenyiapkanDataLogin(MasukActivity.this);

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("alamat", alamat.getText().toString() );
                    startActivity(intent);
                    finish();

                    progressDialogModel.hideProgressDialog();
                }
            }
        });


    }

    private void cekKebutuhanApp() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo("com.google.android.webview", 0);
            Log.d(TAG, "version name: " + pi.versionName);
            Log.d(TAG, "version code: " + pi.versionCode);
            try {
                PackageInfo pi2 = pm.getPackageInfo("com.android.chrome", 0);
                Log.d(TAG, "version name: " + pi2.versionName);
                Log.d(TAG, "version code: " + pi2.versionCode);

                int firstDotIndex = pi2.versionName.indexOf(".");
                String majorVersion = pi2.versionName.substring(0, firstDotIndex);

                if( Integer.parseInt(majorVersion) < 50 ){
                    pesanDialog("Google Chrome versi lama, harap update dahulu?",getString(R.string.chrome) );
                }

            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Google Chrome is not found");

                pesanDialog("Google Chrome tidak tersedia, harap download dahulu?",getString(R.string.chrome) );

            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Android System WebView is not found");

            pesanDialog("Android System WebView tidak tersedia, harap download dahulu?",getString(R.string.webview) );

        }
    }

    private void pesanDialog(String pesan, final String link){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("App dibutuhkan");
        alertDialogBuilder.setMessage(pesan).setCancelable(false).setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link)));

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
    }

}
