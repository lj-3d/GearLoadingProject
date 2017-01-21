package lj_3d.gearloadingproject;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import lj_3d.gearloadinglayout.gearViews.GearLoadingLayout;
import lj_3d.gearloadinglayout.pullToRefresh.PullToRefreshLayout;

/**
 * Created by liubomyr on 06.10.16.
 */

public class ScrollViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);

        final GearLoadingLayout gearLoadingLayout = (GearLoadingLayout) findViewById(R.id.gear_layout);
        final PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        pullToRefreshLayout.setFullExpandedCloseDuration(1000);
        pullToRefreshLayout.setRefreshCallback(new PullToRefreshLayout.RefreshCallback() {
            @Override
            public void onRefresh() {
                gearLoadingLayout.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshLayout.finishRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onDrag(float offset) {
                gearLoadingLayout.rotateByValue(360f * offset);
            }

            @Override
            public void onStartClose() {

            }

            @Override
            public void onFinishClose() {
                gearLoadingLayout.stop();
            }
        });
    }

}
