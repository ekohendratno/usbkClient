package id.kopas.berkarya.usbkclient;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.view.View;
import android.view.WindowManager;

public class MasukActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masuk);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        final AppCompatEditText alamat = findViewById(R.id.alamat);
        AppCompatButton clickMasuk = findViewById(R.id.clickMasuk);
        clickMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!alamat.getText().equals("") ){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("alamat", alamat.getText().toString() );
                    startActivity(intent);
                }
            }
        });


    }

}
