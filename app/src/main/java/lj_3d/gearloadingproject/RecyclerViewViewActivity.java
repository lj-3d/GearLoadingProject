package lj_3d.gearloadingproject;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lj_3d.gearloadinglayout.gearViews.GearLoadingLayout;
import lj_3d.gearloadinglayout.pullToRefresh.PullToRefreshLayout;
import lj_3d.gearloadinglayout.pullToRefresh.RefreshCallback;

/**
 * Created by liubomyr on 06.10.16.
 */

public class RecyclerViewViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        initUI();
    }

    void initUI() {
        final RecyclerView pullToRefreshRecyclerView = (RecyclerView) findViewById(R.id.rv_pull_to_refresh);
        pullToRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pullToRefreshRecyclerView.setAdapter(new PullToRefreshAdapter());

        final GearLoadingLayout gearLoadingLayout = (GearLoadingLayout) findViewById(R.id.gear_layout);
        final PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        pullToRefreshLayout.setFullExpandedCloseDuration(1000);
        pullToRefreshLayout.setRefreshCallback(new RefreshCallback() {
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

    class PullToRefreshAdapter extends RecyclerView.Adapter<PullToRefreshAdapter.ViewHolder> {


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View item = LayoutInflater.from(RecyclerViewViewActivity.this).inflate(R.layout.scrollable_item, null);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 100;
        }

        class ViewHolder extends RecyclerView.ViewHolder {


            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

    }

}
