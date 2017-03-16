package lj_3d.gearloadingproject;

import android.os.Bundle;

/**
 * Created by liubomyr on 06.10.16.
 */

public class SimpleActivity extends PullToRefreshHeaderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_layout);
        initHeaderUI();
    }

}
