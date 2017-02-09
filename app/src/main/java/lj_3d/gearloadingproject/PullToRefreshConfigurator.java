package lj_3d.gearloadingproject;

import android.os.Handler;

import lj_3d.gearloadinglayout.gearViews.GearLoadingLayout;
import lj_3d.gearloadinglayout.pullToRefresh.PullToRefreshLayout;
import lj_3d.gearloadinglayout.pullToRefresh.RefreshCallback;

/**
 * Created by liubomyr on 08.02.17.
 */

public class PullToRefreshConfigurator {

    public static void setupPullToRefresh(final PullToRefreshLayout pullToRefreshLayout, final GearLoadingLayout gearLoadingLayout) {
        pullToRefreshLayout.setFullExpandedCloseDuration(3000);
        pullToRefreshLayout.setRefreshCallback(new RefreshCallback() {
            @Override
            public void onRefresh() {
//                gearLoadingLayout.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshLayout.finishRefresh();
                    }
                }, 2000);
            }

            @Override
            public void onDrag(float offset) {
//                gearLoadingLayout.rotateByValue(360f * offset);
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
