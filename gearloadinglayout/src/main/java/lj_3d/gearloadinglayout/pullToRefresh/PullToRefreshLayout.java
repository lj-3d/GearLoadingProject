package lj_3d.gearloadinglayout.pullToRefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import lj_3d.gearloadinglayout.R;

/**
 * Created by liubomyr on 04.10.16.
 */

public class PullToRefreshLayout extends RelativeLayout {

    private boolean mIsRefreshing;
    private boolean mInnerScrollEnabled;

    private int mThreshold;
    private int mSecondChildTopPosition;
    private int mFullExpandedCloseDuration;
    private int mNonFullExpandedCloseDuration;

    private float mStartYValue;
    private float mDeltaYValue;
    private float mMaxYValue;
    private float mLastYValue;
    private float mRestoreYValue;
    private float mOverScrollDelta;

    private View mFirstChild;
    private View mSecondChild;

    private RefreshCallback mRefreshCallback;
    private OnChildTouchListener mOnChildTouchListener;
    private OnListViewScrollListener mOnListViewScrollListener;
    private OnPullToRefreshTouchEvent mOnPullToRefreshTouchEvent;
    private OnNestedScrollViewScrollListener mOnNestedScrollViewScrollListener;

    private final ValueAnimator mBackDragAnimator = new ValueAnimator();

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDrag();
        parseAttributes(attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        prepareChildren();
    }

    private void prepareChildren() {
        try {
            if (getChildCount() == 2) {
                mFirstChild = getChildAt(0);
                mSecondChild = getChildAt(1);

                prepareActionForScrollableView();

                mFirstChild.setEnabled(false);
                mFirstChild.setFocusable(false);
                mFirstChild.setFocusableInTouchMode(false);

                // need to get top scrollable child top absolute position
                // for restore scrollable state after finish refresh
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mSecondChildTopPosition = getTopPosition(mSecondChild);
                    }
                });

                // set touch listener to child to obtain y coordinates and motion events
                mSecondChild.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mOnChildTouchListener != null)
                            mOnChildTouchListener.onTouch(view, event);

                        final float yAxis = event.getRawY();

                        if (mIsRefreshing) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN)
                                mRestoreYValue = yAxis;
                            return false; // control for blocking content
                        }
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                onActionDown(yAxis);
                                return false;
                            case MotionEvent.ACTION_UP:
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    mLastYValue = mSecondChild.getTranslationY();
                                    return false;
                                } else {
                                    return onTouchEvent(event);
                                }
                            case MotionEvent.ACTION_MOVE:
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    return false;
                                } else {
                                    if (mOverScrollDelta == 0f) { // need get overscroll offset from scrollable views
                                        mLastYValue = mSecondChild.getTranslationY();
                                        mStartYValue = mOverScrollDelta = yAxis - mLastYValue;
                                        mMaxYValue = mStartYValue + mThreshold;
                                    }
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
        if (mSecondChild instanceof ListView) {
            ((ListView) mSecondChild).setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView listView, int scrollState) {
                    if (mOnListViewScrollListener != null)
                        mOnListViewScrollListener.onScrollStateChanged(listView, scrollState);
                }

                @Override
                public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (mOnListViewScrollListener != null)
                        mOnListViewScrollListener.onScroll(listView, firstVisibleItem, visibleItemCount, totalItemCount);

                    if (firstVisibleItem == 0 && listView != null && listView.getChildAt(0) != null) {
                        final int topPosition = listView.getChildAt(0).getTop();
                        mInnerScrollEnabled = Math.abs(topPosition) != 0;
                    }
                }
            });
        } else if (mSecondChild instanceof RecyclerView) {
            ((RecyclerView) mSecondChild).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (recyclerView != null && recyclerView.getChildAt(0) != null) {
                        final int topPosition = recyclerView.getChildAt(0).getTop();
                        mInnerScrollEnabled = Math.abs(topPosition) != 0;
                    }
                }
            });
        } else if (mSecondChild instanceof NestedScrollView) {
            ((NestedScrollView) mSecondChild).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (mOnNestedScrollViewScrollListener != null)
                        mOnNestedScrollViewScrollListener.onScrollChange(nestedScrollView, scrollX, scrollY, oldScrollX, oldScrollY);
                    mInnerScrollEnabled = scrollY != 0f;
                }
            });
        } else {
            mSecondChild.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    mInnerScrollEnabled = mSecondChild.getScrollY() != 0f;
                }
            });
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnPullToRefreshTouchEvent != null)
            mOnPullToRefreshTouchEvent.onTouchEvent(event);

        if (mFirstChild == null || mSecondChild == null || mIsRefreshing) {
            return super.onTouchEvent(event);
        }

        final float yAxis = event.getRawY() < mOverScrollDelta ? event.getRawY() + mOverScrollDelta : event.getRawY();
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
        mBackDragAnimator.setDuration(isRefreshing() ? mFullExpandedCloseDuration : mNonFullExpandedCloseDuration);
        mBackDragAnimator.setFloatValues(from, 0f);
        mBackDragAnimator.start();
    }

    private void initDrag() {
        mBackDragAnimator.addListener(new Animator.AnimatorListener() {
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

        mBackDragAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
        mSecondChild.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, mRestoreYValue - mSecondChildTopPosition, 0, 0, 0, 0, 0, 0, 0));
        mRestoreYValue = 0f;
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

    private int getTopPosition(final View view) {
        final Rect viewRect = new Rect();
        view.getGlobalVisibleRect(viewRect);
        return viewRect.top;
    }


    public void setRefreshCallback(RefreshCallback refreshCallback) {
        mRefreshCallback = refreshCallback;
    }

    public void setOnChildTouchListener(OnChildTouchListener onChildTouchListener) {
        mOnChildTouchListener = onChildTouchListener;
    }

    public void setOnListViewScrollListener(OnListViewScrollListener onListViewScrollListener) {
        mOnListViewScrollListener = onListViewScrollListener;
    }

    public void setOnPullToRefreshTouchEvent(OnPullToRefreshTouchEvent onPullToRefreshTouchEvent) {
        mOnPullToRefreshTouchEvent = onPullToRefreshTouchEvent;
    }

    public void setOnNestedScrollViewScrollListener(OnNestedScrollViewScrollListener onNestedScrollViewScrollListener) {
        mOnNestedScrollViewScrollListener = onNestedScrollViewScrollListener;
    }


}
