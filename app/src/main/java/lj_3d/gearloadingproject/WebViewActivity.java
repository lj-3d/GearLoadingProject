package lj_3d.gearloadingproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import lj_3d.gearloadinglayout.gearViews.GearLoadingLayout;
import lj_3d.gearloadinglayout.pullToRefresh.PullToRefreshLayout;

/**
 * Created by liubomyr on 06.10.16.
 */

public class WebViewActivity extends PullToRefreshHeaderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initHeaderUI();
        final WebView pullToRefreshGridVieWebView = (WebView) findViewById(R.id.wv_pull_to_refresh);
        final WebSettings webSettings = pullToRefreshGridVieWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        pullToRefreshGridVieWebView.loadUrl("http://rozetka.com.ua");
    }

}
