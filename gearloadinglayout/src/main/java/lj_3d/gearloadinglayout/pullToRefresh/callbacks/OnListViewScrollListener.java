package lj_3d.gearloadinglayout.pullToRefresh.callbacks;

import android.widget.AbsListView;

/**
 * Created by LJ on 04.02.2017.
 */
public interface OnListViewScrollListener {

    void onScrollStateChanged(AbsListView view, int scrollState);

    void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount);

}
