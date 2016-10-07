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

    private int mCloseDuration;
    private boolean mIsRefreshing;

    private int mThreshold;
    private float mStartYPosition;
    private float mYDeltaPosition;

    private float mMaxYValue;
    private View mFirstChild;

    private View mSecondChild;
    private View mScrollableView;
    private final ValueAnimator mBackDragger = new ValueAnimator();

    private RefreshCallback mRefreshCallback;
    private ScrollableViewType mScrollableViewType;

    private float mIsInnerScroll;
    private float mInnerScrollPosition;


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
                                mStartYPosition = yAxis;
                                mMaxYValue = mStartYPosition + mThreshold;
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mStartYPosition > yAxis || mIsInnerScroll > 0) {
                                    mSecondChild.setTranslationY(0);
                                    return false;
                                } else
                                    return PullToRefreshLayout.this.onTouchEvent(event);
                            case MotionEvent.ACTION_MOVE:
                                Log.d("onTouchEvent ", "ACTION_MOVE child " + yAxis + " " + mInnerScrollPosition);
                                if (mStartYPosition > yAxis || mIsInnerScroll > 0) {
                                    return false;
                                } else
                                    return PullToRefreshLayout.this.onTouchEvent(event);
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
                Log.d("scrollView onScroll", mScrollableView.getScrollY() + "");
                mIsInnerScroll = mScrollableView.getScrollY();
                if (mIsInnerScroll < 0) {
                    mIsInnerScroll = 0;
                }
            }
        });
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
                Log.d("onTouchEvent ", "ACTION_MOVE parent " + minValue + " " + mMaxYValue + " " + mStartYPosition);
                if (mStartYPosition > yAxis) {
                    mSecondChild.setTranslationY(0);
                } else {
                    if (minValue < mMaxYValue)
                        dragDownView(minValue);
                    else if (mMaxYValue > 0) {
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
