package id.kopas.berkarya.usbkaclient;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Timer;
import java.util.TimerTask;

import static id.kopas.berkarya.usbkaclient.DetectConnection.isNetworkStatusAvialable;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    private static int PAGE_LOAD_PROGRESS = 0;
    private ConnectionTimeoutHandler timeoutHandler = null;
    private ProgressBar progressBar;

    MyTimerTask myTimerTask;
    Timer timer;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        String alamat_server = getResources().getString(R.string.url_server);

        final WebView view = findViewById(R.id.view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //cekKebutuhanApp();

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setUseWideViewPort(true);
        view.getSettings().setLoadWithOverviewMode(true);
        view.getSettings().setSupportZoom(false);
        view.getSettings().setBuiltInZoomControls(false);
        view.getSettings().setDisplayZoomControls(false);

        view.getSettings().setLoadsImagesAutomatically(true);
        //view.clearCache(true);
        view.clearCache(true);
        view.clearHistory();
        view.setWebViewClient(new ExamWebView());
        //view.setWebChromeClient(new MyWebChromeClient(this));
        //view.setWebChromeClient(new WebChromeClient());

        view.loadUrl(alamat_server);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkStatusAvialable (MainActivity.this)) {
                    view.reload();
                    view.getSettings().setDomStorageEnabled(true);
                } else {
                    Intent i = new Intent(getBaseContext(), OfflineActivity.class);
                    i.putExtra("error", "Url tidak terhubung" );
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            windowCloseHandler.postDelayed(windowCloserRunnable, 250);
        }
    }

    private void toggleRecents() {
        Intent closeRecents = new Intent("com.android.systemui.recent.action.TOGGLE_RECENTS");
        closeRecents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ComponentName recents = new ComponentName("com.android.systemui", "com.android.systemui.recent.RecentsActivity");
        closeRecents.setComponent(recents);
        this.startActivity(closeRecents);
    }

    private Handler windowCloseHandler = new Handler();
    private Runnable windowCloserRunnable = new Runnable() {
        @Override
        public void run() {
            ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

            if (cn != null && cn.getClassName().equals("com.android.systemui.recent.RecentsActivity")) {
                toggleRecents();
            }
        }
    };

    private class ExamWebView extends WebViewClient {
        private ExamWebView() {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);


            timeoutHandler = new ConnectionTimeoutHandler(MainActivity.this, view);
            timeoutHandler.execute();

            progressBar.setVisibility(View.VISIBLE);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(isNetworkStatusAvialable (MainActivity.this)) {
                view.loadUrl(url);
            } else {
                Intent i = new Intent(getBaseContext(), OfflineActivity.class);
                i.putExtra("error", "Url tidak terhubung" );
                startActivity(i);
                finish();
            }
            return true;
        }

        public void onPageFinished(WebView view, String url) {
            //progressDialogModel.hideProgressDialog();
            swipeRefreshLayout.setRefreshing(false);
            if (timeoutHandler != null)
                timeoutHandler.cancel(true);

            progressBar.setVisibility(View.GONE);

            super.onPageFinished(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);

            Intent i = new Intent(getBaseContext(), OfflineActivity.class);
            i.putExtra("error", description );
            startActivity(i);
            System.exit(0);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            PAGE_LOAD_PROGRESS = newProgress;
            //Log.i(TAG, "Page progress [" + PAGE_LOAD_PROGRESS + "%]");
            super.onProgressChanged(view, newProgress);
        }
    }

    public class ConnectionTimeoutHandler extends AsyncTask<Void, Void, String> {

        private static final String PAGE_LOADED = "PAGE_LOADED";
        private static final String CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
        private static final long CONNECTION_TIMEOUT_UNIT = 120000L; //1 minute

        private Context mContext = null;
        private WebView webView;
        private Time startTime = new Time();
        private Time currentTime = new Time();
        private Boolean loaded = false;

        public ConnectionTimeoutHandler(Context mContext, WebView webView) {
            this.mContext = mContext;
            this.webView = webView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.startTime.setToNow();
            MainActivity.PAGE_LOAD_PROGRESS = 0;
        }

        @Override
        protected void onPostExecute(String result) {

            if (CONNECTION_TIMEOUT.equalsIgnoreCase(result)) {
                showError(this.mContext, WebViewClient.ERROR_TIMEOUT);

                this.webView.stopLoading();
            } else if (PAGE_LOADED.equalsIgnoreCase(result)) {
                //Toast.makeText(this.mContext, "Page load success", Toast.LENGTH_LONG).show();
            } else {
                //Handle unknown events here
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            while (! loaded) {
                currentTime.setToNow();
                if (MainActivity.PAGE_LOAD_PROGRESS != 100
                        && (currentTime.toMillis(true) - startTime.toMillis(true)) > CONNECTION_TIMEOUT_UNIT) {
                    return CONNECTION_TIMEOUT;
                } else if (MainActivity.PAGE_LOAD_PROGRESS == 100) {
                    loaded = true;
                }
            }
            return PAGE_LOADED;
        }
    }

    private void showError(Context mContext, int errorCode) {
        //Prepare message
        String message = null;
        String title = null;
        if (errorCode == WebViewClient.ERROR_AUTHENTICATION) {
            message = "User authentication failed on server";
            title = "Auth Error";
        } else if (errorCode == WebViewClient.ERROR_TIMEOUT) {
            message = "The server is taking too much time to communicate. Try again later.";
            title = "Connection Timeout";
        } else if (errorCode == WebViewClient.ERROR_TOO_MANY_REQUESTS) {
            message = "Too many requests during this load";
            title = "Too Many Requests";
        } else if (errorCode == WebViewClient.ERROR_UNKNOWN) {
            message = "Generic error";
            title = "Unknown Error";
        } else if (errorCode == WebViewClient.ERROR_BAD_URL) {
            message = "Check entered URL..";
            title = "Malformed URL";
        } else if (errorCode == WebViewClient.ERROR_CONNECT) {
            message = "Failed to connect to the server";
            title = "Connection";
        } else if (errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
            message = "Failed to perform SSL handshake";
            title = "SSL Handshake Failed";
        } else if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            message = "Server or proxy hostname lookup failed";
            title = "Host Lookup Error";
        } else if (errorCode == WebViewClient.ERROR_PROXY_AUTHENTICATION) {
            message = "User authentication failed on proxy";
            title = "Proxy Auth Error";
        } else if (errorCode == WebViewClient.ERROR_REDIRECT_LOOP) {
            message = "Too many redirects";
            title = "Redirect Loop Error";
        } else if (errorCode == WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME) {
            message = "Unsupported authentication scheme (not basic or digest)";
            title = "Auth Scheme Error";
        } else if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            message = "Unsupported URI scheme";
            title = "URI Scheme Error";
        } else if (errorCode == WebViewClient.ERROR_FILE) {
            message = "Generic file error";
            title = "File";
        } else if (errorCode == WebViewClient.ERROR_FILE_NOT_FOUND) {
            message = "File not found";
            title = "File";
        } else if (errorCode == WebViewClient.ERROR_IO) {
            message = "The server failed to communicate. Try again later.";
            title = "IO Error";
        }

        if (message != null) {
            new android.app.AlertDialog.Builder(mContext)
                    .setMessage(message)
                    .setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    setResult(RESULT_CANCELED);
                                    finish();
                                }
                            }).show();
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setTitle("Konfirmasi");
        alertDialogBuilder.setMessage("Yakin keluar dari aplikasi ?").setCancelable(false).setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //view.loadUrl(s + "/logout");

                CookieSyncManager.createInstance(getApplicationContext());
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

                    cookieManager.removeAllCookie();
                    cookieManager.removeSessionCookie();

                }
                cookieManager.setAcceptCookie(false);

                WebView webview = new WebView(getApplicationContext());
                webview.clearCache(true);
                webview.clearHistory();

                WebSettings ws = webview.getSettings();
                ws.setSaveFormData(false);
                ws.setSavePassword(false); // Not needed for API level 18 or greater (deprecated)


                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);

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

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    static int hitung = 0;
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


            if(hitung > 20 ){

                Thread timer = new Thread() {
                    public void run() {
                        try {
                            sleep(320);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    android.webkit.CookieManager cookieManager = CookieManager.getInstance();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                                            // a callback which is executed when the cookies have been removed
                                            @Override
                                            public void onReceiveValue(Boolean aBoolean) {
                                                Log.d(TAG, "Cookie removed: " + aBoolean);
                                            }
                                        });
                                    }
                                    else cookieManager.removeAllCookie();

                                    WebView webview = new WebView(getApplicationContext());
                                    webview.clearCache(true);
                                    webview.clearHistory();

                                    WebSettings ws = webview.getSettings();
                                    ws.setSaveFormData(false);
                                    ws.setSavePassword(false);

                                    finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            });
                        }
                    }
                };
                timer.start();
            }


            hitung++;


        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
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
