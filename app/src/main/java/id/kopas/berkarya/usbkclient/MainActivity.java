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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import java.util.Timer;
import java.util.TimerTask;

import static id.kopas.berkarya.usbkclient.DetectConnection.isNetworkStatusAvialable;

public class MainActivity extends AppCompatActivity {

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
        String s = getResources().getString(R.string.url_server);

        final WebView view = findViewById(R.id.view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


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
        //view.clearCache(true);
        view.setWebViewClient(new ExamWebView());
        view.setWebChromeClient(new MyWebChromeClient(this));
        //view.setWebChromeClient(new WebChromeClient());

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
                    //progressDialogModel.hideProgressDialog();
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


            timeoutHandler = new ConnectionTimeoutHandler(MainActivity.this, view);
            timeoutHandler.execute();

            progressBar.setVisibility(View.VISIBLE);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(isNetworkStatusAvialable (MainActivity.this)) {
                view.loadUrl(url);
                //progressDialogModel.pdMenyiapkanDataLogin(MainActivity.this);
            } else {
                Toast.makeText(MainActivity.this, "Url tidak valid/offline", Toast.LENGTH_LONG).show();
                view.loadDataWithBaseURL(null, "<html><body><img width=\"100%\" height=\"100%\" src=\"file:///android_res/drawable/offline.png\"></body></html>", "text/html", "UTF-8", null);
                //progressDialogModel.hideProgressDialog();
                swipeRefreshLayout.setRefreshing(false);
                Intent i = new Intent(getBaseContext(), MasukActivity.class);
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

            Intent i = new Intent(getBaseContext(), MasukActivity.class);
            i.putExtra("valid", "offline");
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
