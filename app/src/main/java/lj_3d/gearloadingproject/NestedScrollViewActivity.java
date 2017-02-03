package lj_3d.gearloadingproject;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import lj_3d.gearloadinglayout.gearViews.GearLoadingLayout;
import lj_3d.gearloadinglayout.pullToRefresh.PullToRefreshLayout;

/**
 * Created by liubomyr on 06.10.16.
 */

public class NestedScrollViewActivity extends ScrollViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested_scroll_view);
        super.initUI();
    }

}
