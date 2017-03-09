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
import lj_3d.gearloadinglayout.pullToRefresh.callbacks.OnChildTouchListener;
import lj_3d.gearloadinglayout.pullToRefresh.callbacks.OnListViewScrollListener;
import lj_3d.gearloadinglayout.pullToRefresh.callbacks.OnNestedScrollViewScrollListener;
import lj_3d.gearloadinglayout.pullToRefresh.callbacks.OnPullToRefreshTouchEvent;
import lj_3d.gearloadinglayout.pullToRefresh.callbacks.RefreshCallback;

/**
 * Created by liubomyr on 04.10.16.
 */

public class PullToRefreshLayout extends RelativeLayout {

    private Mode mMode;

    private boolean mIsRefreshing;
    private boolean mInnerScrollEnabled;

    private int mThreshold;
    private int mTension;
    private int mTotalHeight;
    private int mFirstChildHeight;
    private int mSecondChildTopPosition;
    private int mFullBackDuration;
    private int mCancelBackDuration;
    private int mTensionBackDuration;
    private float mStartYValue;

    private float mDeltaYValue;
    private float mMaxYValue;
    private float mLastYValue;
    private float mRestoreYValue;
    private float mOverScrollDelta;
    private float mDragCoefficient;
    private float mTensionCoefficient;
    private float mIncreasedTensionYValue;
    private float mIncreasedThresholdYValue;

    private View mFirstChild;
    private View mSecondChild;

    private View mViewScrollableViewToFind;
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
                prepareCallbacksForScrollableView(mViewScrollableViewToFind);

                mFirstChild.setEnabled(false);
                mFirstChild.setFocusable(false);
                mFirstChild.setFocusableInTouchMode(false);
                setupLayoutByMode(mMode);

                // set touch listener to child to obtain y coordinates and motion events
                mViewScrollableViewToFind.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mOnChildTouchListener != null)
                            mOnChildTouchListener.onTouch(view, event);

                        final float yAxis = event.getRawY();

                        int eventAction = event.getAction();

                        if (eventAction == MotionEvent.ACTION_POINTER_DOWN ||
                                eventAction == MotionEvent.ACTION_POINTER_2_DOWN ||
                                eventAction == MotionEvent.ACTION_POINTER_UP ||
                                eventAction == MotionEvent.ACTION_POINTER_2_UP) {
                            Log.d("motion_event_", " " + eventAction + " yAxis " + yAxis);
                        }

                        if (mIsRefreshing || getTag() != null) {
                            if (eventAction == MotionEvent.ACTION_DOWN)
                                mRestoreYValue = yAxis;
                            return true; // control for blocking content
                        }
                        switch (eventAction) {
                            case MotionEvent.ACTION_DOWN:
                                if (mSecondChildTopPosition > 0) {
                                    if (dragUpAnimator != null && dragUpAnimator.isRunning())
                                        dragUpAnimator.cancel();
                                    if (dragTensionUpAnimator != null && dragTensionUpAnimator.isRunning())
                                        dragTensionUpAnimator.cancel();
                                }
                                onActionDown(yAxis);
                                Log.d("onActionDown", "triggered");
                                return false;
                            case MotionEvent.ACTION_POINTER_2_DOWN:
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    mLastYValue = mSecondChild.getTranslationY();
                                    return false;
                                } else {
                                    return onTouchEvent(event);
                                }
                            case MotionEvent.ACTION_POINTER_UP:
                                onActionDown(mStartYValue + mLastYValue);
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    mLastYValue = mSecondChild.getTranslationY();
                                    return false;
                                } else {
                                    return onTouchEvent(event);
                                }
                            case MotionEvent.ACTION_POINTER_2_UP:
                                return false;
                            case MotionEvent.ACTION_CANCEL:
                                Log.d("onActionDown_cancel", "triggered");
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (mSecondChildTopPosition > 0) {
                                    if (dragUpAnimator != null && dragUpAnimator.isRunning())
                                        dragUpAnimator.cancel();
                                    if (dragTensionUpAnimator != null && dragTensionUpAnimator.isRunning())
                                        dragTensionUpAnimator.cancel();
                                }
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    return false;
                                } else {
                                    if (mOverScrollDelta == 0f) { // need get overscroll offset from scrollable views
                                        mLastYValue = mSecondChild.getTranslationY();
                                        mStartYValue = mOverScrollDelta = yAxis - mLastYValue;
                                        mIncreasedThresholdYValue = (mThreshold * mDragCoefficient);
                                        mIncreasedTensionYValue = (mTension * mTensionCoefficient);
                                        mMaxYValue = mStartYValue + mIncreasedThresholdYValue + mIncreasedTensionYValue;
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

    private void prepareCallbacksForScrollableView(final View scrollableView) {
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

        final int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_POINTER_DOWN ||
                eventAction == MotionEvent.ACTION_POINTER_2_DOWN ||
                eventAction == MotionEvent.ACTION_POINTER_UP ||
                eventAction == MotionEvent.ACTION_POINTER_2_UP) {
            Log.d("motion_event_reset", " " + event.getAction());
            return false;
        }

        if (mFirstChild == null || mSecondChild == null || mIsRefreshing) {
            return super.onTouchEvent(event);
        }

        final float yAxis = event.getRawY() < mOverScrollDelta ? event.getRawY() + mOverScrollDelta : event.getRawY();
        final float minValue = Math.min(yAxis, Math.min(yAxis, mMaxYValue));

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                if (mSecondChildTopPosition > 0) {
                    if (dragUpAnimator != null && dragUpAnimator.isRunning())
                        dragUpAnimator.cancel();
                    if (dragTensionUpAnimator != null && dragTensionUpAnimator.isRunning())
                        dragTensionUpAnimator.cancel();
                } else
                    onActionDown(yAxis);
                break;
            case MotionEvent.ACTION_UP:
                if (minValue >= mMaxYValue) {
                    mIsRefreshing = true;
                    if (mTension == 0)
                        onRefresh();
                    else {
                        dragUpWithTension(mSecondChild.getTranslationY());
                    }
                } else if (mStartYValue < yAxis) {
                    if (mSecondChild.getTranslationY() <= mThreshold) {
                        dragUpView(mSecondChild.getTranslationY());
                    } else
                        dragUpWithTension(mSecondChild.getTranslationY());
                } else if (mStartYValue > yAxis) {
                    mSecondChild.setTranslationY(0f);
                    mOverScrollDelta = 0f;
                }
                mLastYValue = mSecondChild.getTranslationY();
                mOverScrollDelta = 0f;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mSecondChildTopPosition > 0) {
                    if (dragUpAnimator != null && dragUpAnimator.isRunning())
                        dragUpAnimator.cancel();
                    if (dragTensionUpAnimator != null && dragTensionUpAnimator.isRunning())
                        dragTensionUpAnimator.cancel();
                }
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
        mDeltaYValue = mMaxYValue - shiftOffset;
        final float delta = mMaxYValue - mIncreasedTensionYValue;
        final float offset = 1f - ((delta - shiftOffset) / (mThreshold * mDragCoefficient));
        final float dragOffset = mThreshold * offset;
        final float dragValue = mThreshold - dragOffset;

        if (dragValue > 0f) {
            mSecondChild.setTranslationY(dragOffset);

            if (mMode == Mode.DRAG)
                mFirstChild.setTranslationY(-dragValue);
        } else {
            final float tensionDelta = 1 - ((mMaxYValue - shiftOffset) / mIncreasedTensionYValue);
            mSecondChild.setTranslationY(mThreshold + (tensionDelta * mTension));

            if (mMode == Mode.DRAG) {
                if (mFirstChild.getTranslationY() != 0f)
                    mFirstChild.setTranslationY(0f);
            }
        }

        if (mRefreshCallback != null) {
            final float fullDragFraction = 1 - (mSecondChild.getTranslationY() / mTotalHeight);
            mRefreshCallback.onDrag(fullDragFraction);
            if (mTension != 0) {
                if (dragValue < 0f) {
                    final float tensionFraction = Math.abs(dragValue) / mIncreasedTensionYValue;
                    mRefreshCallback.onTension(tensionFraction);
                } else mRefreshCallback.onTension(0f);
            }
        }
    }

    ValueAnimator dragUpAnimator;

    private void dragUpView(final float from) {
        dragUpAnimator = new ValueAnimator();
        dragUpAnimator.setFloatValues(from, 0f);
        dragUpAnimator.setDuration(getTag() != null ? mFullBackDuration : mCancelBackDuration);
        dragUpAnimator.addListener(new Animator.AnimatorListener() {
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

        dragUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                onBackDrag(valueAnimator);
            }
        });
        dragUpAnimator.start();
    }

    ValueAnimator dragTensionUpAnimator;

    private void dragUpWithTension(final float from) {
        dragTensionUpAnimator = new ValueAnimator();
        dragTensionUpAnimator.setDuration(mTensionBackDuration);
        dragTensionUpAnimator.setFloatValues(from, mThreshold);

        setTag(dragTensionUpAnimator);
        dragTensionUpAnimator.addListener(new Animator.AnimatorListener() {
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

        dragTensionUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                onBackTension(valueAnimator);
            }
        });
        dragTensionUpAnimator.start();
    }

    private void onBackDrag(final ValueAnimator valueAnimator) {
        final float delta = (float) valueAnimator.getAnimatedValue();
        final float fraction = valueAnimator.getAnimatedFraction();
        mSecondChild.setTranslationY(delta);
        if (mMode == Mode.DRAG) {
            mFirstChild.setTranslationY(delta - mFirstChildHeight);
        }
        if (mRefreshCallback != null)
            mRefreshCallback.onBackDrag(fraction);
    }

    private void onBackTension(final ValueAnimator valueAnimator) {
        final float delta = (float) valueAnimator.getAnimatedValue();
        mSecondChild.setTranslationY(delta);
        if (mRefreshCallback != null)
            mRefreshCallback.onTensionUp(1 - ((delta - mTension) / mTension));
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
        mStartYValue = 0f;
        mDeltaYValue = 0f;
        mMaxYValue = 0f;
        mLastYValue = 0f;
        mSecondChild.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, mRestoreYValue - mSecondChildTopPosition, 0, 0, 0, 0, 0, 0, 0));
        mRestoreYValue = 0f;
        setTag(null);
        Log.d("onActionDown_reset", "triggered");
    }

    public void setThreshold(int threshold) {
        mThreshold = threshold;
        calculateTotalHeight();
    }

    public void setTension(int tension) {
        mTension = tension < 0 ? 0 : tension;
        calculateTotalHeight();
    }


    public void setMode(Mode mode) {
        mMode = mode;
        setupLayoutByMode(mode);
    }

    public void setDragCoefficient(float dragCoefficient) {
        mDragCoefficient = dragCoefficient < 1 ? 1 : dragCoefficient;
    }

    public void setTensionCoefficient(float tensionCoefficient) {
        mTensionCoefficient = tensionCoefficient < 1 ? 1 : tensionCoefficient;
    }

    public void setupLayoutByMode(Mode mode) {
        if (mFirstChild == null) return;
        final LayoutParams firstChildLayoutParams = (LayoutParams) mFirstChild.getLayoutParams();
        mFirstChildHeight = firstChildLayoutParams.height;
        if (firstChildLayoutParams == null) return;
        switch (mode) {
            case OVERLAY:
                mFirstChild.setTranslationY(0);
                break;
            case DRAG:
                mFirstChild.setTranslationY(-mFirstChildHeight);
                break;
            case PARALLAX:
                mFirstChild.setTranslationY(-mFirstChildHeight);
                break;
        }
        mFirstChild.requestLayout();
    }

    private void calculateTotalHeight() {
        mTotalHeight = mThreshold + mTension;
    }

    public void setFullBackDuration(int fullBackDuration) {
        this.mFullBackDuration = fullBackDuration;
    }

    public void setCancelBackDuration(int cancelBackDuration) {
        this.mCancelBackDuration = cancelBackDuration;
    }

    public void setTensionBackDuration(int tensionBackDuration) {
        this.mTensionBackDuration = tensionBackDuration;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        setFullBackDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_fullBackDuration, 300));
        setCancelBackDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_cancelBackDuration, 200));
        setTensionBackDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_cancelBackDuration, 200));
        setThreshold(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_ptr_threshold, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_threshold)));
        setTension(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_ptr_tension, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_tension)));
        setDragCoefficient(a.getFloat(R.styleable.PullToRefreshLayout_ptr_drag_coefficient, 1.0f));
        setTensionCoefficient(a.getFloat(R.styleable.PullToRefreshLayout_ptr_tension_coefficient, 3.0f));
        setMode(Mode.values()[a.getInt(R.styleable.PullToRefreshLayout_ptr_mode, 0)]);
        requestLayout();
        a.recycle();
    }

    private void onActionDown(final float yAxis) {
        mStartYValue = yAxis - mLastYValue;
        mIncreasedThresholdYValue = (mThreshold * mDragCoefficient);
        mIncreasedTensionYValue = (mTension * mTensionCoefficient);
        mMaxYValue = mStartYValue + mIncreasedThresholdYValue + mIncreasedTensionYValue;
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
