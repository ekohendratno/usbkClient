package id.kopas.berkarya.usbkclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static id.kopas.berkarya.usbkclient.DetectConnection.isNetworkStatusAvialable;

public class MainActivity extends AppCompatActivity {

    MyTimerTask myTimerTask;
    Timer timer;
    private SwipeRefreshLayout swipeRefreshLayout;
    WebView view;
    String s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        String s = getResources().getString(R.string.url_server);
        view = findViewById(R.id.view);


        Intent i = getIntent();
        String alamat = i.getStringExtra("alamat");
        if(!alamat.equals("")){
            s = alamat;
        }

        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setUseWideViewPort(true);
        view.getSettings().setLoadWithOverviewMode(true);
        //view.getSettings().setSupportZoom(true);
        //view.getSettings().setBuiltInZoomControls(true);
        //view.getSettings().setDisplayZoomControls(false);

        view.getSettings().setLoadsImagesAutomatically(true);
        //view.clearCache();
        view.setWebViewClient(new ExamWebView());
        view.setWebChromeClient(new WebChromeClient());

        view.loadUrl(s);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkStatusAvialable (MainActivity.this)) {
                    view.reload();
                    view.getSettings().setDomStorageEnabled(true);
                } else {
                    Toast.makeText(MainActivity.this, "Url tidak valid/offline", Toast.LENGTH_LONG).show();
                    view.loadDataWithBaseURL(null, "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>", "text/html", "UTF-8", null);
                    progressDialogModel.hideProgressDialog();
                    swipeRefreshLayout.setRefreshing(false);
                    Intent i = new Intent(getBaseContext(), MasukActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            bringApplicationToFront();
        }
    }

    private class ExamWebView extends WebViewClient {
        private ExamWebView() {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(isNetworkStatusAvialable (MainActivity.this)) {
                view.loadUrl(url);
                progressDialogModel.pdMenyiapkanDataLogin(MainActivity.this);
            } else {
                Toast.makeText(MainActivity.this, "Url tidak valid/offline", Toast.LENGTH_LONG).show();
                view.loadDataWithBaseURL(null, "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>", "text/html", "UTF-8", null);progressDialogModel.hideProgressDialog();
                swipeRefreshLayout.setRefreshing(false);
                Intent i = new Intent(getBaseContext(), MasukActivity.class);
                startActivity(i);
                finish();
            }
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            progressDialogModel.hideProgressDialog();
            swipeRefreshLayout.setRefreshing(false);
            super.onPageFinished(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            swipeRefreshLayout.setRefreshing(false);
            Intent i = new Intent(getBaseContext(), MasukActivity.class);
            i.putExtra("valid", "offline");
            startActivity(i);
            System.exit(0);
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setTitle("Konfirmasi");
        alertDialogBuilder.setMessage("Yakin keluar dari aplikasi ?").setCancelable(false).setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                view.loadUrl(s + "/logout");

                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getApplicationContext());
                CookieManager cookieManager = CookieManager.getInstance();
                if(Build.VERSION.SDK_INT >= 21) {
                    cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                        @Override
                        public void onReceiveValue(Boolean aBoolean) {

                        }
                    });
                    cookieManager.flush();
                }
                else{
                    cookieSyncMngr.startSync();

                    cookieManager.removeAllCookie();
                    cookieManager.removeSessionCookie();


                    cookieSyncMngr.stopSync();
                    cookieSyncMngr.sync();
                }
                cookieManager.setAcceptCookie(false);

                WebView webview = new WebView(getApplicationContext());
                webview.clearCache(true);
                webview.clearHistory();

                WebSettings ws = webview.getSettings();
                ws.setSaveFormData(false);
                ws.setSavePassword(false); // Not needed for API level 18 or greater (deprecated)


                System.exit(0);
            }
        }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onPause() {
        if (timer == null) {
            myTimerTask = new MyTimerTask();
            timer = new Timer();
            timer.schedule(myTimerTask, 100, 100);
        }
        super.onPause();
    }

    private void bringApplicationToFront() {
        KeyguardManager myKeyManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (myKeyManager.inKeyguardRestrictedInputMode())
            return;
        Log.d("TAG", "====Bringging Application to Front====");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
