package lj_3d.gearloadinglayout.pullToRefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import lj_3d.gearloadinglayout.R;

/**
 * Created by liubomyr on 04.10.16.
 */

public class PullToRefreshLayout extends RelativeLayout {

    private boolean mIsRefreshing;

    private int mFullExpandedCloseDuration;
    private int mNonFullExpandedCloseDuration;
    private int mThreshold;

    private float mStartYValue;
    private float mDeltaYValue;
    private float mMaxYValue;
    private float mLastYValue;
    private float mInnerScrollValue;
    private float mOverScrollDelta;

    private float mRestoreValue;

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

                prepareActionForScrollableView();

                mSecondChild.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final float yAxis = event.getRawY();

                        if (mIsRefreshing) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN)
                                mRestoreValue = yAxis;
                            return false; // control for blocking content
                        }

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                onActionDown(yAxis);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mStartYValue > yAxis || mInnerScrollValue > 0f) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    mLastYValue = mSecondChild.getTranslationY();
                                    return false;
                                } else {
                                    return onTouchEvent(event);
                                }
                            case MotionEvent.ACTION_MOVE:
                                if (mStartYValue > yAxis || mInnerScrollValue > 0f) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    return false;
                                } else {
                                    if (mOverScrollDelta == 0f) { // need get overscroll offset from scrollable views
                                        mLastYValue = mSecondChild.getTranslationY();
                                        mStartYValue = mOverScrollDelta = yAxis - mLastYValue;
                                        mMaxYValue = mStartYValue + mThreshold;
                                    }
                                    Log.d("mSecondChild", "ACTION_MOVE super");
                                    return onTouchEvent(event);
                                }
                        }
                        return onTouchEvent(event);
                    }
                });
            }
        } catch (NullPointerException exception) {
            Log.d("onFinishInflate ", "Nullable child or child count less 2");
        }
    }

    private void prepareActionForScrollableView() {
        mSecondChild.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mInnerScrollValue = mSecondChild.getScrollY();
                if (mInnerScrollValue < 0f) {
                    mInnerScrollValue = 0f;
                }
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float yAxis = event.getRawY() < mOverScrollDelta ? event.getRawY() + mOverScrollDelta : event.getRawY();


        if (mFirstChild == null || mSecondChild == null || mIsRefreshing) { //|| mDeltaYValue >= mThreshold
            return super.onTouchEvent(event);
        }

        final float minValue = Math.min(yAxis, Math.min(yAxis, mMaxYValue));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(yAxis);
                break;
            case MotionEvent.ACTION_UP:
                if (mStartYValue < yAxis) {
                    dragUpView(mThreshold - mDeltaYValue);
                } else if (mStartYValue > yAxis) {
                    mSecondChild.setTranslationY(0f);
                    mOverScrollDelta = 0f;
                }
                mLastYValue = mSecondChild.getTranslationY();
                mOverScrollDelta = 0f;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mStartYValue > yAxis) {
                    mSecondChild.setTranslationY(0);
                } else {
                    dragView(minValue);
                    if (minValue >= mMaxYValue) {
                        if (mRefreshCallback != null) {
                            mRefreshCallback.onRefresh();
                        } else
                            finishRefresh();
                    }
                }
                break;
        }
        return true;
    }

    private void dragView(final float shiftOffset) {
        mDeltaYValue = (mMaxYValue - shiftOffset);
        final float offset = 1 - (mDeltaYValue / mThreshold);
        mSecondChild.setTranslationY(mThreshold * offset);
        mIsRefreshing = shiftOffset >= mMaxYValue;
        if (mRefreshCallback != null)
            mRefreshCallback.onDrag(offset);
    }

    private void dragUpView(final float from) {
        mBackDragger.setDuration(isRefreshing() ? mFullExpandedCloseDuration : mNonFullExpandedCloseDuration);
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
                final float delta = (float) valueAnimator.getAnimatedValue();
                mSecondChild.setTranslationY(delta);
            }
        });
    }

    public void finishRefresh() {
        dragUpView(mThreshold);
    }

    private void reset() {
        mIsRefreshing = false;
        mStartYValue = 0f;
        mDeltaYValue = 0f;
        mMaxYValue = 0f;
        mLastYValue = 0f;
        mOverScrollDelta = 0f;
        mSecondChild.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, mRestoreValue, 0, 0, 0, 0, 0, 0, 0));
        mRestoreValue = 0f;
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

    public void setFullExpandedCloseDuration(int fullExpandedCloseDuration) {
        this.mFullExpandedCloseDuration = fullExpandedCloseDuration;
    }

    public void setNonFullExpandedCloseDuration(int nonFullExpandedCloseDuration) {
        this.mNonFullExpandedCloseDuration = nonFullExpandedCloseDuration;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        setFullExpandedCloseDuration(a.getInteger(R.styleable.PullToRefreshLayout_fullExpandedCloseDuration, 300));
        setNonFullExpandedCloseDuration(a.getInteger(R.styleable.PullToRefreshLayout_nonFullExpandedCloseDuration, 200));
        setThreshold(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_threshold, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_threshold)));
        requestLayout();
        a.recycle();
    }

    private void onActionDown(final float yAxis) {
        mStartYValue = yAxis - mLastYValue;
        mMaxYValue = mStartYValue + mThreshold;
        mOverScrollDelta = 0f;
    }

}
