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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import lj_3d.gearloadinglayout.R;

/**
 * Created by liubomyr on 04.10.16.
 */

public class PullToRefreshLayout extends RelativeLayout {

    private boolean mIsRefreshing;
    private boolean mInnerScrollEnabled;
    private boolean mIsScrollableViewEnabled;

    private int mThreshold;
    private int mTension;
    private int mTotalHeight;
    private int mSecondChildTopPosition;
    private int mFullDragDuration;
    private int mCancelDragDuration;

    private float mStartYValue;
    private float mDeltaYValue;
    private float mMaxYValue;
    private float mLastYValue;
    private float mRestoreYValue;
    private float mOverScrollDelta;

    private View mFirstChild;
    private View mSecondChild;
    private View mViewScrollableViewToFind;

    private MotionEvent mChildMotionEvent;

    private RefreshCallback mRefreshCallback;
    private OnChildTouchListener mOnChildTouchListener;
    private OnListViewScrollListener mOnListViewScrollListener;
    private OnPullToRefreshTouchEvent mOnPullToRefreshTouchEvent;
    private OnNestedScrollViewScrollListener mOnNestedScrollViewScrollListener;

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

                findScrollableView(mSecondChild);
                if (mViewScrollableViewToFind == null)
                    mViewScrollableViewToFind = mSecondChild;
                prepareActionForScrollableView(mViewScrollableViewToFind);

                mFirstChild.setEnabled(false);
                mFirstChild.setFocusable(false);
                mFirstChild.setFocusableInTouchMode(false);

                // set touch listener to child to obtain y coordinates and motion events
                mViewScrollableViewToFind.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        mChildMotionEvent = event;

                        if (mOnChildTouchListener != null)
                            mOnChildTouchListener.onTouch(view, event);

                        final float yAxis = event.getRawY();

                        if (mIsRefreshing) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN)
                                mRestoreYValue = yAxis;
                            return true; // control for blocking content
                        }
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                onActionDown(yAxis);
                                return false;
                            case MotionEvent.ACTION_UP:
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
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
                                        mMaxYValue = mStartYValue + mTotalHeight;
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

    private void findScrollableView(final View secondChild) {
        if (secondChild instanceof AbsListView ||
                secondChild instanceof RecyclerView ||
                secondChild instanceof ScrollView ||
                secondChild instanceof WebView ||
                secondChild instanceof NestedScrollView) {
            mViewScrollableViewToFind = secondChild;
        } else if (mViewScrollableViewToFind == null && secondChild instanceof ViewGroup && ((ViewGroup) secondChild).getChildCount() > 0) {
            for (int i = 0; i < ((ViewGroup) secondChild).getChildCount(); i++) {
                if (mViewScrollableViewToFind == null)
                    findScrollableView(((ViewGroup) secondChild).getChildAt(i));
            }
        }
    }

    private void prepareActionForScrollableView(final View scrollableView) {
        // need to get top scrollable child top absolute position
        // for restore scrollable state after finish refresh
        scrollableView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSecondChildTopPosition = getTopPosition(mSecondChild);
            }
        });
        if (scrollableView instanceof AbsListView) {
            ((AbsListView) scrollableView).setOnScrollListener(new AbsListView.OnScrollListener() {
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
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        } else if (scrollableView instanceof NestedScrollView) {
            ((NestedScrollView) scrollableView).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView nestedScrollView, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (mOnNestedScrollViewScrollListener != null)
                        mOnNestedScrollViewScrollListener.onScrollChange(nestedScrollView, scrollX, scrollY, oldScrollX, oldScrollY);
                    mInnerScrollEnabled = scrollY != 0f;
                }
            });
        } else {
            scrollableView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    mInnerScrollEnabled = scrollableView.getScrollY() != 0f;
                    Log.d("onScrollChanged ", " " + scrollableView.getScrollY());
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
                if (minValue >= mMaxYValue) {
                    mIsRefreshing = true;
                    if (mTension == 0)
                        onRefresh();
                    else {
                        dragWithTension();
                    }
                } else if (mStartYValue < yAxis) {
                    dragUpView(mTotalHeight - mDeltaYValue);
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
                }
                break;
        }
        return true;
    }

    private void dragView(final float shiftOffset) {
        mDeltaYValue = (mMaxYValue - shiftOffset);
        final float offset = 1 - (mDeltaYValue / mTotalHeight);
        mSecondChild.setTranslationY(mTotalHeight * offset);
        if (mRefreshCallback != null) {
            mRefreshCallback.onDrag(offset);
            if (mTension != 0) {
                if (mDeltaYValue <= mTension) {
                    final float tensionFraction = 1 - (mDeltaYValue / mTension);
                    mRefreshCallback.onTension(tensionFraction);
                } else mRefreshCallback.onTension(0f);
            }
        }
    }

    private void dragUpView(final float from) {
        final ValueAnimator backDragAnimator = new ValueAnimator();
        backDragAnimator.setFloatValues(from, 0f);
        backDragAnimator.setDuration(mIsRefreshing ? mFullDragDuration : mCancelDragDuration);
        backDragAnimator.addListener(new Animator.AnimatorListener() {
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

        backDragAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                onBackDrag(valueAnimator);
                if (mRefreshCallback != null)
                    mRefreshCallback.onBackDrag(valueAnimator.getAnimatedFraction());
            }
        });
        backDragAnimator.start();
    }

    private void dragWithTension() {
        final ValueAnimator tensionAnimator = new ValueAnimator();
        tensionAnimator.setDuration(200);
        tensionAnimator.setFloatValues(mTotalHeight, mThreshold);
        tensionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRefreshCallback != null)
                    mRefreshCallback.onTensionComplete();
                onRefresh();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        tensionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                onBackDrag(valueAnimator);
                if (mRefreshCallback != null)
                    mRefreshCallback.onTensionUp(valueAnimator.getAnimatedFraction());
            }
        });
        tensionAnimator.start();
    }

    private void onBackDrag(final ValueAnimator valueAnimator) {
        final float delta = (float) valueAnimator.getAnimatedValue();
        mSecondChild.setTranslationY(delta);
    }

    private void onRefresh() {
        if (mRefreshCallback != null) {
            mRefreshCallback.onRefresh();
        } else
            finishRefresh();
    }

    public void finishRefresh() {
        dragUpView(mThreshold);
    }

    private void reset() {
        mIsRefreshing = false;
        mOverScrollDelta = 0f;
        if (mChildMotionEvent.getAction() != MotionEvent.ACTION_MOVE) {
            mStartYValue = 0f;
            mDeltaYValue = 0f;
            mMaxYValue = 0f;
            mLastYValue = 0f;
            mSecondChild.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, mRestoreYValue - mSecondChildTopPosition, 0, 0, 0, 0, 0, 0, 0));
            mRestoreYValue = 0f;
        }
    }

    public void setThreshold(int threshold) {
        mThreshold = threshold;
        calculateTotalHeight();
    }

    public void setTension(int tension) {
        mTension = tension < 0 ? 0 : tension;
        calculateTotalHeight();
    }

    private void calculateTotalHeight() {
        mTotalHeight = mThreshold + mTension;
    }

    public void setFullDragDuration(int fullDragDuration) {
        this.mFullDragDuration = fullDragDuration;
    }

    public void setCancelDragDuration(int cancelDragDuration) {
        this.mCancelDragDuration = cancelDragDuration;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        setFullDragDuration(a.getInteger(R.styleable.PullToRefreshLayout_fullDragDuration, 300));
        setCancelDragDuration(a.getInteger(R.styleable.PullToRefreshLayout_cancelDragDuration, 200));
        setThreshold(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_threshold, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_threshold)));
        setTension(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_tension, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_tension)));
        requestLayout();
        a.recycle();
    }

    private void onActionDown(final float yAxis) {
        mStartYValue = yAxis - mLastYValue;
        mMaxYValue = mStartYValue + mTotalHeight;
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
