package lj_3d.gearloadinglayout.pullToRefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import lj_3d.gearloadinglayout.R;

/**
 * Created by liubomyr on 04.10.16.
 */

public class PullToRefreshLayout extends RelativeLayout {

    private int mCloseDuration;
    private boolean mIsRefreshing;

    private int mThreshold;
    private float mStartYPosition;
    private float mYDeltaPosition;

    private float mMaxYValue;
    private View mFirstChild;

    private View mSecondChild;
    private final ValueAnimator mBackDragger = new ValueAnimator();

    private RefreshCallback mRefreshCallback;


    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDragger();
        parseAttributes(attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
        if (getChildCount() == 2) {
            mFirstChild = getChildAt(0);
            mSecondChild = getChildAt(1);

            mSecondChild.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return PullToRefreshLayout.this.onTouchEvent(event);
                }
            });
//                mThreshold = mFirstChild.getBottom();
        }
        } catch (NullPointerException exception) {
            Log.d("onFinishInflate ", "Nullable child or child count less 2");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final float xAxis = event.getRawX();
        final float yAxis = event.getRawY();
//        Log.d("onTouchEvent ", xAxis + " " + yAxis);

        if (mFirstChild == null || mSecondChild == null || mYDeltaPosition >= mThreshold || mIsRefreshing)
            return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("onTouchEvent ", "ACTION_DOWN " + yAxis);
                mStartYPosition = yAxis;
                mMaxYValue = mStartYPosition + mThreshold;
                break;
            case MotionEvent.ACTION_UP:
                Log.d("onTouchEvent ", "ACTION_UP");
                if (mStartYPosition > yAxis) {
                    mSecondChild.setTranslationY(0);
                } else if (mStartYPosition < yAxis) {
                    dragUpView(mThreshold - mYDeltaPosition);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float minValue = Math.min(yAxis, Math.min(yAxis, mMaxYValue));
                Log.d("onTouchEvent ", "ACTION_MOVE " + minValue + " " + mMaxYValue + " " + mStartYPosition);
                if (mStartYPosition > yAxis) {
                    mSecondChild.setTranslationY(0);
                } else {
                    if (minValue < mMaxYValue)
                        dragDownView(minValue);
                    else {
                        dragDownView(mMaxYValue);
                        if (mRefreshCallback != null) {
                            mIsRefreshing = true;
                            mRefreshCallback.onRefresh();
                        } else
                            finishRefresh();
                    }
                }
                break;

        }
        return true;
    }

    private void dragDownView(final float shiftOffset) {
        mYDeltaPosition = (mMaxYValue - shiftOffset);
        Log.d("dragDownView ", "mYDeltaPosition " + mYDeltaPosition);
        final float offset = 1 - (mYDeltaPosition / mThreshold);
        mSecondChild.setTranslationY(mThreshold * offset);
        if (mRefreshCallback != null)
            mRefreshCallback.onDrag(offset);
    }

    private void dragUpView(final float from) {
        mBackDragger.setDuration(mCloseDuration);
        mBackDragger.setFloatValues(from, 0f);
        mBackDragger.start();
    }

    private void initDragger() {
        mBackDragger.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (mRefreshCallback != null)
                    mRefreshCallback.onStartClose();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mRefreshCallback != null)
                    mRefreshCallback.onFinishClose();
                reset();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        mBackDragger.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mRefreshCallback != null)
                    mRefreshCallback.onDrag(valueAnimator.getAnimatedFraction());
                setEnabled(false);
                final float delta = (float) valueAnimator.getAnimatedValue();
                mSecondChild.setTranslationY(delta);
            }
        });
    }

    public void finishRefresh() {
        dragUpView(mThreshold);
    }

    private void reset() {
        setEnabled(true);
        mIsRefreshing = false;
        mStartYPosition = 0f;
        mYDeltaPosition = 0f;
        mMaxYValue = 0f;
    }

    public void setRefreshCallback(RefreshCallback refreshCallback) {
        mRefreshCallback = refreshCallback;
    }

    public interface RefreshCallback {

        public void onRefresh();

        public void onDrag(float offset);

        public void onStartClose();

        public void onFinishClose();

    }

    public void setThreshold(int threshold) {
        mThreshold = threshold;
    }

    public void setCloseDuration(int closeDuration) {
        this.mCloseDuration = closeDuration;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        setCloseDuration(a.getInteger(R.styleable.PullToRefreshLayout_closeDuration, 300));
        setThreshold(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_threshold, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_threshold)));
        requestLayout();
        a.recycle();
    }


}
