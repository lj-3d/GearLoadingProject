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
import lj_3d.gearloadinglayout.enums.ScrollableViewType;

/**
 * Created by liubomyr on 04.10.16.
 */

public class PullToRefreshLayout extends RelativeLayout {

    private boolean mIsRefreshing;

    private int mCloseDuration;
    private int mThreshold;

    private float mStartYValue;
    private float mDeltaYValue;
    private float mMaxYValue;
    private float mLastYValue;
    private float mInnerScrollValue;
    private float mOverScrollDelta;

    private View mFirstChild;
    private View mSecondChild;
    private View mScrollableView;

    private final ValueAnimator mBackDragger = new ValueAnimator();
    private RefreshCallback mRefreshCallback;
    private ScrollableViewType mScrollableViewType;


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

                checkIfScrollableElementPresentInChild(mSecondChild);
                prepareActionForScrollableView();
                mSecondChild.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        final float yAxis = event.getRawY();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                onActionDown(yAxis);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mStartYValue > yAxis || mInnerScrollValue > 0) {
                                    mSecondChild.setTranslationY(0);
                                    mOverScrollDelta = 0f;
                                    mLastYValue = mSecondChild.getTranslationY();
                                    return false;
                                } else {
                                    return PullToRefreshLayout.this.onTouchEvent(event);
                                }
                            case MotionEvent.ACTION_MOVE:
                                if (mStartYValue > yAxis || mInnerScrollValue > 0) {
                                    mSecondChild.setTranslationY(0);
                                    mOverScrollDelta = 0f;
                                    return false;
                                } else {
                                    if (mOverScrollDelta == 0f) { // need get overscroll offset from scrollable views
                                        mLastYValue = mSecondChild.getTranslationY();
                                        mStartYValue = mOverScrollDelta = yAxis - mLastYValue;
                                        mMaxYValue = mStartYValue + mThreshold;
                                    }
                                    return PullToRefreshLayout.this.onTouchEvent(event);
                                }
                        }
                        return false;
                    }
                });
            }
        } catch (NullPointerException exception) {
            Log.d("onFinishInflate ", "Nullable child or child count less 2");
        }
    }

    private void checkIfScrollableElementPresentInChild(final View child) {
        if (child instanceof ScrollView) {
            mScrollableViewType = ScrollableViewType.SCROLL_VIEW;
            mScrollableView = child;
            return;
        } else if (child instanceof ListView) {
            mScrollableViewType = ScrollableViewType.LIST_VIEW;
            mScrollableView = child;
            return;
        } else if (child instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) child;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                checkIfScrollableElementPresentInChild(viewGroup.getChildAt(i));
        } else mScrollableViewType = ScrollableViewType.NONE;

    }

    private void prepareActionForScrollableView() {
//        switch (mScrollableViewType) {
//            case LIST_VIEW:
//                final ListView listView = (ListView) mScrollableView;
//                listView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//                    @Override
//                    public void onScrollChanged() {
//
//                    }
//                });
//                break;
//            case SCROLL_VIEW:
//                final ScrollView scrollView = (ScrollView) mScrollableView;
//                scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//                    @Override
//                    public void onScrollChanged() {
//                        Log.d("scrollView onScroll", scrollView.getScrollY() + "");
//                    }
//                });
//                break;
//        }
        mScrollableView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mInnerScrollValue = mScrollableView.getScrollY();
                if (mInnerScrollValue < 0) {
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
//                if (mMaxYValue > 0) {
//                    dragView(mMaxYValue);
//                    if (mRefreshCallback != null) {
//                        mIsRefreshing = true;
//                        mRefreshCallback.onRefresh();
//                        Log.d("ACTION refresh", " " + yAxis);
//                    }
//                } else
//                if (mStartYValue < yAxis) {
//                    dragUpView(mThreshold - mDeltaYValue);
//                    Log.d("ACTION drag", " " + yAxis);
//                } else
                if (mStartYValue > yAxis) {
                    mSecondChild.setTranslationY(0);
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
//                        if (mRefreshCallback != null) {
//                            mIsRefreshing = true;
//                            mRefreshCallback.onRefresh();
//                        } else
//                            finishRefresh();
//                    }
                }
                break;

        }
        return true;
    }

    private void dragView(final float shiftOffset) {
        mDeltaYValue = (mMaxYValue - shiftOffset);
        final float offset = 1 - (mDeltaYValue / mThreshold);
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
        mStartYValue = 0f;
        mDeltaYValue = 0f;
        mMaxYValue = 0f;
        mLastYValue = 0f;
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

    private void onActionDown(final float yAxis) {
        mStartYValue = yAxis - mLastYValue;
        mMaxYValue = mStartYValue + mThreshold;
        mOverScrollDelta = 0f;
    }

}
