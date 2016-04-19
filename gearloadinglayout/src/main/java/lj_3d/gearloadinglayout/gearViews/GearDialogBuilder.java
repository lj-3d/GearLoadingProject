package lj_3d.gearloadinglayout.gearViews;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;

import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Type;
import lj_3d.gearloadinglayout.utils.DeviceScreenHelper;

/**
 * Created by LJ on 21.03.2016.
 */
public class GearDialogBuilder {

    private static GearDialogBuilder mGearLoadingBuilder;

    private Activity mActivity;
    private ViewGroup mRootViewGroup;
    private GearLoadingLayout mGearLoadingLayout;

    public static GearDialogBuilder getInstance(final Activity activity) {
        if (mGearLoadingBuilder == null)
            mGearLoadingBuilder = new GearDialogBuilder();

        mGearLoadingBuilder.mActivity = activity;
        mGearLoadingBuilder.mRootViewGroup = ((ViewGroup) activity.findViewById(android.R.id.content));

        DeviceScreenHelper.init(activity);
        return mGearLoadingBuilder;
    }

    /**
     * Method that define type of GearLoadingLayout
     *
     * @param type is a type of child class that extend GearLoadingLayout
     * @return GearLoadingLayout that casted to child layout, in accordance with type of class
     */

    public <T extends GearLoadingLayout> T setType(Class<T> type) {

        switch (type.getSimpleName()) {
            case OneGearLayout.IDENTIFIER:
                mGearLoadingLayout = new OneGearLayout(mActivity);
                break;
            case TwoGearsLayout.IDENTIFIER:
                mGearLoadingLayout = new TwoGearsLayout(mActivity);
                break;
            case ThreeGearsLayout.IDENTIFIER:
                mGearLoadingLayout = new ThreeGearsLayout(mActivity);
                break;
            default:
                mGearLoadingLayout = new ThreeGearsLayout(mActivity);
                break;
        }
        mGearLoadingLayout.setActivityContentView(mRootViewGroup);
        return type.cast(mGearLoadingLayout);
    }

}
