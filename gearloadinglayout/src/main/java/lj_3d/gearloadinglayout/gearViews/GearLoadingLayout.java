package lj_3d.gearloadinglayout.gearViews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import lj_3d.gearloadinglayout.R;
import lj_3d.gearloadinglayout.enums.ShowMode;
import lj_3d.gearloadinglayout.enums.Style;
import lj_3d.gearloadinglayout.interfaces.OnBlurCompleteInterface;
import lj_3d.gearloadinglayout.utils.DeviceScreenHelper;
import lj_3d.gearloadinglayout.utils.FastBlur;

/**
 * Created by LJ on 23.03.2016.
 */
public class GearLoadingLayout extends FrameLayout implements OnBlurCompleteInterface, View.OnClickListener {

    protected Resources mResources;
    protected Style mCurrentStyle = Style.SNACK_BAR;

    protected ViewGroup mActivityContentView;
    protected View mGearLayoutInnerWrapper;
    protected View mGearWrapper;
    protected RelativeLayout mMainWrapper;
    protected FrameLayout mGearLayoutWrapper;
    protected CutOutLayout mCutOutLayout;
    protected FrameLayout mMainBackground;
    protected ShowMode mShowMode = ShowMode.CENTER;
    protected FastBlur mFastBlur = new FastBlur();

    protected int showDialogDuration = 333;
    protected int mDialogWidth;
    protected int mDialogHeight;


    protected int mMainBackgroundColor = Color.TRANSPARENT;
    protected int mInnerWrapperColor = Color.TRANSPARENT;
    protected float mMainBackgroundAlpha;

    protected boolean isDialogShown;
    protected boolean cancelable = true;
    protected boolean showDialog;
    protected boolean isAnimating;
    protected boolean isEnableBlur = true;

    public GearLoadingLayout(Context context) {
        this(context, null);
    }

    public GearLoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GearLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDialogWidth = DeviceScreenHelper.mDeviceWidth;
        mResources = getResources();
    }

    protected void initUI(View rootView) {
        mMainWrapper = (RelativeLayout) rootView.findViewById(R.id.main_wrapper);
        mGearLayoutWrapper = (FrameLayout) rootView.findViewById(R.id.gear_layout_wrapper);
        mGearLayoutInnerWrapper = rootView.findViewById(R.id.gear_layout_inner_wrapper);
        mGearWrapper = rootView.findViewById(R.id.gears_wrapper);
        mMainBackground = (FrameLayout) rootView.findViewById(R.id.main_background);
        mCutOutLayout = (CutOutLayout) rootView.findViewById(R.id.cut_out_layout);
        mMainWrapper.setOnClickListener(this);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    public void onBlurComplete() {
        selectShowAnimation(true);
    }


    public void show() {
        if (mActivityContentView == null) return;
        mGearLayoutWrapper.getLayoutParams().height = mDialogHeight;
        mGearLayoutWrapper.requestLayout();
        if (isEnableBlur) {
            mFastBlur.callBlur(mActivityContentView, mMainBackground, this);
        } else {
            mMainBackground.setBackgroundColor(mMainBackgroundColor);
            mMainBackground.setAlpha(mMainBackgroundAlpha);
            selectShowAnimation(true);
        }

    }

    public void hide() {
        selectShowAnimation(false);
    }


    public void setActivityContentView(ViewGroup activityContentView) {
        mActivityContentView = activityContentView;
    }

    public GearLoadingLayout setStyle(Style style) {
        this.mCurrentStyle = style;
        return this;
    }

    public GearLoadingLayout setDialogBackgroundColor(int color) {
        this.mInnerWrapperColor = color;
        mGearLayoutInnerWrapper.setBackgroundColor(color);
        return this;
    }

    public GearLoadingLayout setDialogBackgroundAlpha(float alpha) {
        mGearLayoutInnerWrapper.setAlpha(alpha);
        return this;
    }

    public GearLoadingLayout setMainBackgroundColor(int color) {
        this.mMainBackgroundColor = color;
        return this;
    }

    public GearLoadingLayout setMainBackgroundAlpha(float alpha) {
        this.mMainBackgroundAlpha = alpha;
        return this;
    }

    public GearLoadingLayout enableCutLayout(boolean enable) {
        mCutOutLayout.setVisibility(enable ? VISIBLE : GONE);
        return this;
    }

    public GearLoadingLayout setCutRadius(int radius) {
        mCutOutLayout.setCutRadius(radius);
        return this;
    }

    public GearLoadingLayout setShadowWidth(int width) {
        mCutOutLayout.setShadowWidth(width);
        return this;
    }

    public GearLoadingLayout setShadowColor(int color) {
        mCutOutLayout.setShadowColor(color);
        return this;
    }

    public GearLoadingLayout setCutLayoutColor(int color) {
        mCutOutLayout.setColor(color);
        return this;
    }

    public GearLoadingLayout setCutLayoutAlpha(float alpha) {
        mCutOutLayout.setAlpha(alpha);
        return this;
    }

    public GearLoadingLayout setShowMode(ShowMode showMode) {
        mShowMode = showMode;
        return this;
    }

    public GearLoadingLayout setDuration(final int duration) {
        return this;
    }

    public GearLoadingLayout setShowDialogDuration(int showDialogDuration) {
        this.showDialogDuration = showDialogDuration;
        return this;
    }

    public GearLoadingLayout blurBackground(boolean enable, int radius, float scaleFactor) {
        isEnableBlur = enable;
        if (isEnableBlur) {
            if (radius > 0 && scaleFactor > 0)
                mFastBlur = new FastBlur(radius, scaleFactor);
            else mFastBlur = new FastBlur();
        }
        return this;
    }

    public GearLoadingLayout setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }


    public void start() {
    }

    public void start(boolean reverse) {
    }

    public void stop() {
    }

    public void rotateByValue(float rotateOffset) {
    }

    public boolean isDialogShown() {
        return isDialogShown;
    }

    private final Runnable startAction = new Runnable() {
        @Override
        public void run() {
            isAnimating = true;
            if (showDialog)
                isDialogShown = true;
        }
    };

    private final Runnable endAction = new Runnable() {
        @Override
        public void run() {
            isAnimating = false;
            if (!showDialog) {
                mActivityContentView.removeView(GearLoadingLayout.this);
                isDialogShown = false;
            }
        }
    };

    private void setGearLayoutWrapperGravity() {
        final RelativeLayout.LayoutParams mLayoutParams = (RelativeLayout.LayoutParams) mGearLayoutWrapper.getLayoutParams();
        switch (mShowMode) {
            case CENTER:
            case LEFT:
            case RIGHT:
                mLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                break;
            case TOP:
                mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                break;
            case BOTTOM:
                mLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                break;
        }
        mGearLayoutWrapper.setLayoutParams(mLayoutParams);
    }

    private void applyStyle() {
        if (mCurrentStyle == Style.DIALOG) {
//            mGearLayoutWrapper.setBackgroundColor(Color.TRANSPARENT);
            mGearLayoutInnerWrapper.setBackgroundColor(Color.TRANSPARENT);
//            mCutOutLayout.setVisibility(GONE);
        }
    }

    private void selectShowAnimation(boolean showDialog) {
        if (mActivityContentView == null || isAnimating)
            return;

        if (showDialog) {
            mActivityContentView.addView(GearLoadingLayout.this);
        }

        this.showDialog = showDialog;
        float from = 0f;
        float to = 0f;
        boolean xAxis = false;
        boolean scaleDialog = false;


        switch (mShowMode) {
            case TOP:
                from = -mDialogHeight;
                to = 0;
                break;
            case BOTTOM:
                from = mDialogHeight;
                to = 0;
                break;
            case LEFT:
                from = -mDialogWidth;
                to = 0;
                xAxis = true;
                break;
            case RIGHT:
                from = mDialogWidth;
                to = 0;
                xAxis = true;
                break;
            case CENTER:
                scaleDialog = true;
                break;
            default:
                break;
        }

        setGearLayoutWrapperGravity();
        applyStyle();
        start();
        if (scaleDialog) {
            ViewCompat.setScaleX(mGearLayoutWrapper, showDialog ? 0 : 1);
            ViewCompat.setScaleY(mGearLayoutWrapper, showDialog ? 0 : 1);
            ViewCompat.animate(mGearLayoutWrapper).scaleX(showDialog ? 1 : 0).scaleY(showDialog ? 1 : 0).setDuration(showDialogDuration).withStartAction(startAction).withEndAction(endAction).start();
        } else {
            if (!xAxis) {
                ViewCompat.setTranslationY(mGearLayoutWrapper, showDialog ? from : to);
                ViewCompat.animate(mGearLayoutWrapper).translationY(showDialog ? to : from).setDuration(showDialogDuration).withStartAction(startAction).withEndAction(endAction).start();
            } else {
                ViewCompat.setTranslationX(mGearLayoutWrapper, showDialog ? from : to);
                ViewCompat.animate(mGearLayoutWrapper).translationX(showDialog ? to : from).setDuration(showDialogDuration).withStartAction(startAction).withEndAction(endAction).start();
            }
        }

        ViewCompat.setAlpha(mMainBackground, showDialog ? 0 : isEnableBlur ? 1 : mMainBackgroundAlpha);
        ViewCompat.animate(mMainBackground).alpha(showDialog ? isEnableBlur ? 1 : mMainBackgroundAlpha : 0).setDuration(showDialogDuration).start();
    }


    @Override
    public void onClick(View v) {
        if (cancelable)
            hide();
    }

    protected void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GearLoadingLayout);
        setCutRadius((int) a.getDimension(R.styleable.GearLoadingLayout_gearLayoutCutRadius, mResources.getDimensionPixelSize(R.dimen.cut_layout_diameter)));
        setCutLayoutColor(a.getColor(R.styleable.GearLoadingLayout_gearLayoutCutColor, Color.WHITE));
        enableCutLayout(a.getBoolean(R.styleable.GearLoadingLayout_cutLayoutVisibility, true));
        setCutLayoutAlpha(a.getFloat(R.styleable.GearLoadingLayout_gearLayoutCutAlpha, 0.5f));
        setDialogBackgroundColor(a.getColor(R.styleable.GearLoadingLayout_layoutBackground, Color.WHITE));
        setDialogBackgroundAlpha(a.getFloat(R.styleable.GearLoadingLayout_layoutAlpha, 1f));
        setShadowWidth((int) a.getDimension(R.styleable.GearLoadingLayout_gearLayoutShadowWidth, mResources.getDimensionPixelSize(R.dimen.shadow_width)));
        setShadowColor(a.getColor(R.styleable.GearLoadingLayout_gearLayoutShadowColor, mResources.getColor(R.color.shadow_grey)));

        a.recycle();
        requestLayout();
    }

}
