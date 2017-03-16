package lj_3d.gearloadingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by liubomyr on 06.10.16.
 */

public class PullToRefreshActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_to_refresh);

        final View simpleLayout = findViewById(R.id.btn_simple_layout);
        final View scrollView = findViewById(R.id.btn_scroll_view);
        final View nestedScrollView = findViewById(R.id.btn_nested_scroll_view);
        final View listView = findViewById(R.id.btn_list_view);
        final View gridView = findViewById(R.id.btn_grid_view);
        final View recyclerView = findViewById(R.id.btn_recycler_view);
        final View webView = findViewById(R.id.btn_web_view);

        simpleLayout.setOnClickListener(this);
        scrollView.setOnClickListener(this);
        nestedScrollView.setOnClickListener(this);
        listView.setOnClickListener(this);
        gridView.setOnClickListener(this);
        recyclerView.setOnClickListener(this);
        webView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Class clazz;
        switch (v.getId()) {
            case R.id.btn_scroll_view:
                clazz = ScrollViewActivity.class;
                break;
            case R.id.btn_nested_scroll_view:
                clazz = NestedScrollViewActivity.class;
                break;
            case R.id.btn_list_view:
                clazz = ListViewActivity.class;
                break;
            case R.id.btn_grid_view:
                clazz = GridViewActivity.class;
                break;
            case R.id.btn_recycler_view:
                clazz = RecyclerViewActivity.class;
                break;
            case R.id.btn_web_view:
                clazz = WebViewActivity.class;
                break;
            default:
                clazz = SimpleActivity.class;
                break;
        }
        startActivity(clazz);
    }

    private void startActivity(final Class clazz) {
        startActivity(new Intent(this, clazz));
    }
}
