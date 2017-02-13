package lj_3d.gearloadingproject;

import android.os.Bundle;

/**
 * Created by liubomyr on 06.10.16.
 */

public class ScrollViewActivity extends PullToRefreshHeaderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);
        initHeaderUI();
    }

}
