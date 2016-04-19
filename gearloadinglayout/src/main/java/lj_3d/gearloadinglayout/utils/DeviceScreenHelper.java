package lj_3d.gearloadinglayout.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by LJ on 18.04.2016.
 */
public class DeviceScreenHelper {

    public static int mDeviceWidth;
    public static int mDeviceHeight;

    public static void init(final Activity activity) {
        if (activity == null) return;
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mDeviceHeight = displaymetrics.heightPixels;
        mDeviceWidth = displaymetrics.widthPixels;
    }


}
