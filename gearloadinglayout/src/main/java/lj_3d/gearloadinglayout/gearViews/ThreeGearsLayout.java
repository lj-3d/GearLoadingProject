package lj_3d.gearloadinglayout.gearViews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import lj_3d.gearloadinglayout.R;
import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Style;
import lj_3d.gearloadinglayout.utils.DeviceScreenHelper;
import lj_3d.gearloadinglayout.utils.FastBlur;

/**
 * Created by LJ on 23.03.2016.
 */
public class ThreeGearsLayout extends GearLoadingLayout {

    public static final String IDENTIFIER = "ThreeGearsLayout";

    private GearView mFirstGearView;
    private GearView mSecondGearView;
    private GearView mThirdGearView;

    public ThreeGearsLayout(Context context) {
        this(context, null);
    }

    public ThreeGearsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThreeGearsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addChildView();
        parseAttributes(attrs);
    }

    private void addChildView() {
        final View childView = inflate(getContext(), R.layout.layout_three_gears, null);
        initUI(childView);
        addView(childView);
    }

    protected void initUI(View rootView) {
        super.initUI(rootView);
        mFirstGearView = (GearView) rootView.findViewById(R.id.gear_view);
        mSecondGearView = (GearView) rootView.findViewById(R.id.gear_view_second);
        mThirdGearView = (GearView) rootView.findViewById(R.id.gear_view_third);
    }


    public void start() {
        mFirstGearView.startSpinning(false);
        mSecondGearView.startSpinning(true);
        mThirdGearView.startSpinning(false);
    }

    public void stop() {
        mFirstGearView.stopSpinning();
        mSecondGearView.stopSpinning();
        mThirdGearView.stopSpinning();
    }

    public void rotateByValue(float rotateOffset) {
        mFirstGearView.rotateByValue(rotateOffset, false);
        mSecondGearView.rotateByValue(rotateOffset, true);
        mThirdGearView.rotateByValue(rotateOffset, false);
    }

    public ThreeGearsLayout setDuration(final int duration) {
        mFirstGearView.setDuration(duration);
        mSecondGearView.setDuration(duration);
        mThirdGearView.setDuration(duration);
        return this;
    }

    public ThreeGearsLayout setFirstGearColor(int color) {
        mFirstGearView.setColor(color);
        return this;
    }

    public ThreeGearsLayout setSecondGearColor(int color) {
        mSecondGearView.setColor(color);
        return this;
    }


    public ThreeGearsLayout setThirdGearColor(int color) {
        mThirdGearView.setColor(color);
        return this;
    }

    public ThreeGearsLayout setFirstGearInnerColor(int color) {
        setFirstGearInnerColor(color, false);
        return this;
    }

    public ThreeGearsLayout setSecondGearInnerColor(int color) {
        setSecondGearInnerColor(color, false);
        return this;
    }

    public ThreeGearsLayout setThirdGearInnerColor(int color) {
        setThirdGearInnerColor(color, false);
        return this;
    }

    public ThreeGearsLayout setShadowColor(int color) {
        super.setShadowColor(color);
        return this;
    }

    public ThreeGearsLayout setShadowWidth(int width) {
        super.setShadowWidth(width);
        return this;
    }

    private ThreeGearsLayout setFirstGearInnerColor(int color, boolean enableCuttedCenter) {
        mFirstGearView.setInnerColor(color);
        mFirstGearView.enableCuttedCenter(enableCuttedCenter);
        return this;
    }

    private ThreeGearsLayout setSecondGearInnerColor(int color, boolean enableCuttedCenter) {
        mSecondGearView.setInnerColor(color);
        mSecondGearView.enableCuttedCenter(enableCuttedCenter);
        return this;
    }

    private ThreeGearsLayout setThirdGearInnerColor(int color, boolean enableCuttedCenter) {
        mThirdGearView.setInnerColor(color);
        mThirdGearView.enableCuttedCenter(enableCuttedCenter);
        return this;
    }

    public ThreeGearsLayout setStyle(final Style style) {
        super.setStyle(style);
        mDialogHeight = style == Style.SNACK_BAR ? mResources.getDimensionPixelSize(R.dimen.three_gear_layout_wrapper_height) : DeviceScreenHelper.mDeviceHeight;
        return this;
    }

    public ThreeGearsLayout setDialogBackgroundColor(int color) {
        super.setDialogBackgroundColor(color);
        return this;
    }

    public ThreeGearsLayout setDialogBackgroundAlpha(float alpha) {
        super.setDialogBackgroundAlpha(alpha);
        return this;
    }

    public ThreeGearsLayout setMainBackgroundColor(int color) {
        super.setMainBackgroundColor(color);
        return this;
    }

    public ThreeGearsLayout setMainBackgroundAlpha(float alpha) {
        super.setMainBackgroundAlpha(alpha);
        return this;
    }

    public ThreeGearsLayout enableCutLayout(boolean enable) {
        super.enableCutLayout(enable);
        return this;
    }

    public ThreeGearsLayout setCutRadius(int radius) {
        super.setCutRadius(radius);
        return this;
    }

    public ThreeGearsLayout setCutLayoutColor(int color) {
        super.setCutLayoutColor(color);
        return this;
    }

    public ThreeGearsLayout setCutLayoutAlpha(float alpha) {
        super.setCutLayoutAlpha(alpha);
        return this;
    }

    public ThreeGearsLayout setShowMode(ShowMode showMode) {
        super.setShowMode(showMode);
        return this;
    }

    public ThreeGearsLayout setShowDialogDuration(int showDialogDuration) {
        super.setShowDialogDuration(showDialogDuration);
        return this;
    }

    public ThreeGearsLayout blurBackground(boolean enable) {
        blurBackground(enable, 0, 0);
        return this;
    }

    public ThreeGearsLayout blurBackground(boolean enable, int radius, float scaleFactor) {
        super.blurBackground(enable, radius, scaleFactor);
        return this;
    }

    public ThreeGearsLayout setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    protected void parseAttributes(AttributeSet attrs) {
        super.parseAttributes(attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GearLoadingLayout);
        setFirstGearColor(a.getColor(R.styleable.GearLoadingLayout_firstGearColor, Color.GRAY));
        setSecondGearColor(a.getColor(R.styleable.GearLoadingLayout_secondGearColor, Color.GRAY));
        setThirdGearColor(a.getColor(R.styleable.GearLoadingLayout_thirdGearColor, Color.GRAY));

        setFirstGearInnerColor(a.getColor(R.styleable.GearLoadingLayout_firstInnerGearColor, Color.WHITE), a.getBoolean(R.styleable.GearLoadingLayout_firstGearCuttedCenter, true));
        setSecondGearInnerColor(a.getColor(R.styleable.GearLoadingLayout_secondInnerGearColor, Color.WHITE), a.getBoolean(R.styleable.GearLoadingLayout_secondGearCuttedCenter, true));
        setThirdGearInnerColor(a.getColor(R.styleable.GearLoadingLayout_thirdInnerGearColor, Color.WHITE), a.getBoolean(R.styleable.GearLoadingLayout_thirdGearCuttedCenter, true));

        a.recycle();
        requestLayout();
    }


}
