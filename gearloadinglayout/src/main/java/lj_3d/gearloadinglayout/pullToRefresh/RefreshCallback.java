package lj_3d.gearloadinglayout.pullToRefresh;

/**
 * Created by LJ on 04.02.2017.
 */
public interface RefreshCallback {

    void onRefresh();

    void onDrag(float offset);

    void onTension(float offset);

    void onBackDrag(float offset);

    void onStartClose();

    void onFinishClose();

    void onTensionComplete();

}
