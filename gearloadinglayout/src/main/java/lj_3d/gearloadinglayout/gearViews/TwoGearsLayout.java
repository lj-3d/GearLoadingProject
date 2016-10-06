package lj_3d.gearloadinglayout.gearViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import lj_3d.gearloadinglayout.R;
import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Style;
import lj_3d.gearloadinglayout.utils.DeviceScreenHelper;

/**
 * Created by LJ on 23.03.2016.
 */
public class TwoGearsLayout extends GearLoadingLayout {

    public static final String IDENTIFIER = "TwoGearsLayout";

    private GearView mFirstGearView;
    private GearView mSecondGearView;

    public TwoGearsLayout(Context context) {
        this(context, null);
    }

    public TwoGearsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoGearsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addChildView();
        parseAttributes(attrs);
    }

    private void addChildView() {
        final View childView = inflate(getContext(), R.layout.layout_two_gears, null);
        initUI(childView);
        addView(childView);
    }

    protected void initUI(View rootView) {
        super.initUI(rootView);
        mFirstGearView = (GearView) rootView.findViewById(R.id.gear_view);
        mSecondGearView = (GearView) rootView.findViewById(R.id.gear_view_second);
    }


    public void start() {
        mFirstGearView.startSpinning(false);
        mSecondGearView.startSpinning(true);
    }

    public void stop() {
        mFirstGearView.stopSpinning();
        mSecondGearView.stopSpinning();
    }

    public void rotateByValue(float rotateOffset) {
        mFirstGearView.rotateByValue(rotateOffset, false);
        mSecondGearView.rotateByValue(rotateOffset, true);
    }

    public TwoGearsLayout setDuration(final int duration) {
        mFirstGearView.setDuration(duration);
        mSecondGearView.setDuration(duration);
        return this;
    }

    public TwoGearsLayout setFirstGearColor(int color) {
        mFirstGearView.setColor(color);
        return this;
    }

    public TwoGearsLayout setSecondGearColor(int color) {
        mSecondGearView.setColor(color);
        return this;
    }


    public TwoGearsLayout setFirstGearInnerColor(int color) {
        setFirstGearInnerColor(color, false);
        return this;
    }

    public TwoGearsLayout setSecondGearInnerColor(int color) {
        setSecondGearInnerColor(color, false);
        return this;
    }

    public TwoGearsLayout setShadowColor(int color) {
        super.setShadowColor(color);
        return this;
    }

    public TwoGearsLayout setShadowWidth(int width) {
        super.setShadowWidth(width);
        return this;
    }


    private TwoGearsLayout setFirstGearInnerColor(int color, boolean enableCuttedCenter) {
        mFirstGearView.setInnerColor(color);
        mFirstGearView.enableCuttedCenter(enableCuttedCenter);
        return this;
    }

    private TwoGearsLayout setSecondGearInnerColor(int color, boolean enableCuttedCenter) {
        mSecondGearView.setInnerColor(color);
        mSecondGearView.enableCuttedCenter(enableCuttedCenter);
        return this;
    }

    public TwoGearsLayout setStyle(final Style style) {
        super.setStyle(style);
        mDialogHeight = style == Style.SNACK_BAR ? mResources.getDimensionPixelSize(R.dimen.three_gear_layout_wrapper_height) : DeviceScreenHelper.mDeviceHeight;
        return this;
    }

    public TwoGearsLayout setDialogBackgroundColor(int color) {
        super.setDialogBackgroundColor(color);
        return this;
    }

    public TwoGearsLayout setDialogBackgroundAlpha(float alpha) {
        super.setDialogBackgroundAlpha(alpha);
        return this;
    }

    public TwoGearsLayout setMainBackgroundColor(int color) {
        super.setMainBackgroundColor(color);
        return this;
    }

    public TwoGearsLayout setMainBackgroundAlpha(float alpha) {
        super.setMainBackgroundAlpha(alpha);
        return this;
    }

    public TwoGearsLayout enableCutLayout(boolean enable) {
        super.enableCutLayout(enable);
        return this;
    }

    public TwoGearsLayout setCutRadius(int radius) {
        super.setCutRadius(radius);
        return this;
    }

    public TwoGearsLayout setCutLayoutColor(int color) {
        super.setCutLayoutColor(color);
        return this;
    }

    public TwoGearsLayout setCutLayoutAlpha(float alpha) {
        super.setCutLayoutAlpha(alpha);
        return this;
    }

    public TwoGearsLayout setShowMode(ShowMode showMode) {
        super.setShowMode(showMode);
        return this;
    }

    public TwoGearsLayout setShowDialogDuration(int showDialogDuration) {
        super.setShowDialogDuration(showDialogDuration);
        return this;
    }

    public TwoGearsLayout blurBackground(boolean enable) {
        blurBackground(enable, 0, 0);
        return this;
    }

    public TwoGearsLayout blurBackground(boolean enable, int radius, float scaleFactor) {
        super.blurBackground(enable, radius, scaleFactor);
        return this;
    }

    public TwoGearsLayout setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }


    protected void parseAttributes(AttributeSet attrs) {
        super.parseAttributes(attrs);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GearLoadingLayout);

        setFirstGearColor(a.getColor(R.styleable.GearLoadingLayout_firstGearColor, Color.GRAY));
        setSecondGearColor(a.getColor(R.styleable.GearLoadingLayout_secondGearColor, Color.GRAY));

        setFirstGearInnerColor(a.getColor(R.styleable.GearLoadingLayout_firstInnerGearColor, Color.WHITE), a.getBoolean(R.styleable.GearLoadingLayout_firstGearCuttedCenter, true));
        setSecondGearInnerColor(a.getColor(R.styleable.GearLoadingLayout_secondInnerGearColor, Color.WHITE), a.getBoolean(R.styleable.GearLoadingLayout_secondGearCuttedCenter, true));

        a.recycle();
        requestLayout();
    }
}
