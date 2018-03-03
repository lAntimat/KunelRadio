package com.kunel.lantimat.kunelradio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

@SuppressLint("SetJavaScriptEnabled")
public class FullFeedActivity extends AppCompatActivity {

    WebView wv;
    Handler uiHandler = new Handler();
    String feedUrl = "";

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(getIntent().getStringExtra("title"));
            getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
            toolbar.setSubtitle(getIntent().getStringExtra("url"));
            toolbar.setNavigationIcon(R.drawable.ic_arrow_left_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        wv = (WebView) findViewById(R.id.webView);
        wv.setWebViewClient(new MyWebViewClient());

        // Инициализация WebView
        initWebView();


        feedUrl = getIntent().getStringExtra("url");
        new BackgroundWorker().execute();

    }

    // Change the layout algorithm used in the WebView
    private void initWebView() {
        WebSettings settings = wv.getSettings();
        //settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        // Enable Javascript
        settings.setJavaScriptEnabled(true);
        //settings.setMinimumFontSize(18);

        WebSettings.LayoutAlgorithm layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING;
        }

        settings.setLayoutAlgorithm(layoutAlgorithm);

        wv.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);


        // Use WideViewport and Zoom out if there is no viewport defined

        //settings.setDefaultFontSize(20);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
    }

    public void onBackPressed() {
        if (wv.canGoBack()) {
            wv.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // load links in WebView instead of default browser
    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @RequiresApi(21)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url != null && url.equalsIgnoreCase(feedUrl)) view.loadUrl(url);
            else if (url != null && url.startsWith("http://") || url.startsWith("https://" )) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            } else {
                return false;
            }
            view.loadUrl(request.getUrl().toString());
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.INVISIBLE);
            super.onPageFinished(view, url);
        }
    }

    private class BackgroundWorker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            getDarewod();
            return null;
        }

        public void getDarewod() {

            try {
                Document htmlDocument = Jsoup.connect(feedUrl).get();
                Elements element = htmlDocument.select("div.col-lg-16.col-md-16.col-sm-16.col-xs-16.column");
                element.select("div.comment-respond").remove();  //удаляем блок комментарией

                // replace body with selected element
                htmlDocument.body().empty().append(element.toString());
                final String html = htmlDocument.toString();

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        wv.loadData(html, "text/html", "UTF-8");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
