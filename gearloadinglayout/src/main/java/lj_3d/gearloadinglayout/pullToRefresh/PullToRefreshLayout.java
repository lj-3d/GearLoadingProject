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
import android.widget.FrameLayout;
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

public class PullToRefreshLayout extends FrameLayout {

    private DragMode mDragMode;
    private TensionMode mTensionMode;

    private boolean mIsRefreshing;
    private boolean mInnerScrollEnabled;
    private boolean mIsChildTouched;

    private int mThreshold;
    private int mTension;
    private int mTotalHeight;
    private int mFirstChildHeight;
    private int mSecondChildTopPosition;
    private int mFullBackDuration;
    private int mCancelBackDuration;
    private int mTensionBackDuration;
    private int mAutoRefreshDuration;

    private float mMaxYValue;
    private float mLastYValue;
    private float mStartYValue;
    private float mRestoreYValue;
    private float mDragCoefficient;
    private float mOverScrollDelta;
    private float mTensionCoefficient;
    private float mParallaxCoefficient;
    private float mIncreasedTensionYValue;
    private float mIncreasedThresholdYValue;

    private View mFirstChild;
    private View mSecondChild;
    private View mViewScrollableViewToFind;

    private ValueAnimator mDragUpAnimator;
    private ValueAnimator mAutoRefreshAnimator;
    private ValueAnimator mDragTensionUpAnimator;

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
                if (mViewScrollableViewToFind == null) {
                    mViewScrollableViewToFind = mSecondChild;
                }
                prepareCallbacksForScrollableView(mViewScrollableViewToFind);

                mFirstChild.setEnabled(false);
                mFirstChild.setFocusable(false);
                mFirstChild.setFocusableInTouchMode(false);
                setupLayoutByMode(mDragMode);

                // set touch listener to child to obtain y coordinates and motion events
                mViewScrollableViewToFind.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (mOnChildTouchListener != null) {
                            mOnChildTouchListener.onTouch(view, event);
                        }

                        final float yAxis = event.getRawY();
                        final int eventAction = event.getAction();

                        if (mIsRefreshing || getTag() != null) {
                            if (eventAction == MotionEvent.ACTION_DOWN) {
                                mRestoreYValue = yAxis;
                            }
                            return true; // control for blocking content
                        }
                        switch (eventAction) {
                            case MotionEvent.ACTION_DOWN:
                                tryToFinishBackAnimators();
                                onActionDown(yAxis);
                                return false;
                            case MotionEvent.ACTION_UP:
                                if (mStartYValue > yAxis || mInnerScrollEnabled) {
                                    mSecondChild.setTranslationY(0f);
                                    mOverScrollDelta = 0f;
                                    mLastYValue = 0f;
                                    return false;
                                } else {
                                    return onTouchEvent(event);
                                }
                            case MotionEvent.ACTION_MOVE:
                                tryToFinishBackAnimators();
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
                if (mViewScrollableViewToFind == null) {
                    findScrollableView(((ViewGroup) secondChild).getChildAt(i));
                }
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
                public void onScrollStateChanged(final AbsListView listView, final int scrollState) {
                    if (mOnListViewScrollListener != null) {
                        mOnListViewScrollListener.onScrollStateChanged(listView, scrollState);
                    }
                }

                @Override
                public void onScroll(final AbsListView listView, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                    if (mOnListViewScrollListener != null) {
                        mOnListViewScrollListener.onScroll(listView, firstVisibleItem, visibleItemCount, totalItemCount);
                    }
                    if (firstVisibleItem == 0 && listView != null && listView.getChildAt(0) != null) {
                        final int topPosition = listView.getChildAt(0).getTop();
                        mInnerScrollEnabled = Math.abs(topPosition) != 0;
                    }
                }
            });
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
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
                public void onScrollChange(final NestedScrollView nestedScrollView, final int scrollX, final int scrollY, final int oldScrollX, final int oldScrollY) {
                    if (mOnNestedScrollViewScrollListener != null) {
                        mOnNestedScrollViewScrollListener.onScrollChange(nestedScrollView, scrollX, scrollY, oldScrollX, oldScrollY);
                    }
                    mInnerScrollEnabled = scrollY != 0f;
                    Log.d("onScrollChange", scrollY + " " + oldScrollY);
                }
            });
        } else {
            scrollableView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    Log.d("onScrollChange", mInnerScrollEnabled + "");
                    mInnerScrollEnabled = scrollableView.getScrollY() != 0f;
                }
            });
        }
    }


    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (mOnPullToRefreshTouchEvent != null) {
            mOnPullToRefreshTouchEvent.onTouchEvent(event);
        }

        final int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_POINTER_2_DOWN || eventAction == MotionEvent.ACTION_POINTER_INDEX_MASK) {
            mSecondChild.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, event.getRawY(), 0, 0, 0, 0, 0, 0, 0));
        }
        if (mFirstChild == null || mSecondChild == null || mIsRefreshing || getTag() != null) {
            return super.onTouchEvent(event);
        }

        final float yAxis = event.getRawY() < mOverScrollDelta ? event.getRawY() + mOverScrollDelta : event.getRawY();
        final float minValue = Math.min(yAxis, Math.min(yAxis, mMaxYValue));

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                tryToFinishBackAnimators();
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
                tryToFinishBackAnimators();
                if (mStartYValue > yAxis) {
                    mSecondChild.setTranslationY(0);
                } else {
                    calculateOffsetAndDragView(minValue);
                }
                break;
        }
        return true;
    }

    private void calculateOffsetAndDragView(final float shiftOffset) {
        final float delta = mMaxYValue - mIncreasedTensionYValue;
        final float offset = 1f - ((delta - shiftOffset) / (mThreshold * mDragCoefficient));
        dragView(offset, shiftOffset);
    }

    private void dragView(final float offset, final float shiftOffset) {
        final float dragOffset = mThreshold * offset;
        final float dragValue = mThreshold - dragOffset;

        if (dragValue > 0f) {
            mSecondChild.setTranslationY(dragOffset);
            if (mDragMode == DragMode.DRAG) {
                mFirstChild.setTranslationY(-dragValue);
            } else if (mDragMode == DragMode.PARALLAX) {
                final float parallaxOffset = (mThreshold * mParallaxCoefficient);
                final float originParallaxShift = mThreshold - parallaxOffset;
                mFirstChild.setTranslationY(-originParallaxShift * (1 - offset));
            }
        } else if (shiftOffset >= 0f) {
            final float tensionDelta = 1 - ((mMaxYValue - shiftOffset) / mIncreasedTensionYValue);
            final float tensionOffset = tensionDelta * mTension;
            mSecondChild.setTranslationY(mThreshold + tensionOffset);

            if (mDragMode == DragMode.DRAG) {
                if (mTensionMode == TensionMode.BOTTOM) {
                    setFirstChildDefaultState();
                } else if (mTensionMode == TensionMode.TOP) {
                    mFirstChild.setTranslationY(tensionOffset);
                }
            } else if (mDragMode == DragMode.PARALLAX) {
                setFirstChildDefaultState();
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

    private void dragUpView(final float from) {
        mDragUpAnimator = new ValueAnimator();
        mDragUpAnimator.setFloatValues(from, 0f);
        mDragUpAnimator.setDuration(getTag() != null ? mFullBackDuration : mCancelBackDuration);
        mDragUpAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (mRefreshCallback != null) {
                    mRefreshCallback.onStartClose();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mRefreshCallback != null) {
                    mRefreshCallback.onFinishClose();
                }
                reset();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        mDragUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                onBackDrag(valueAnimator);
            }
        });
        mDragUpAnimator.start();
    }

    private void dragUpWithTension(final float from) {
        mDragTensionUpAnimator = new ValueAnimator();
        mDragTensionUpAnimator.setDuration(mTensionBackDuration);
        mDragTensionUpAnimator.setFloatValues(from, mThreshold);

        setTag(mDragTensionUpAnimator);
        mDragTensionUpAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRefreshCallback != null) {
                    mRefreshCallback.onTensionComplete();
                }
                onRefresh();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mDragTensionUpAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                onBackTension(valueAnimator);
            }
        });
        mDragTensionUpAnimator.start();
    }

    public void callAutoRefresh() {
        if (getTag() != null || mIsChildTouched) return;
        mAutoRefreshAnimator = new ValueAnimator();
        setTag(mAutoRefreshAnimator);
        mAutoRefreshAnimator.setFloatValues(0f, mThreshold);
        mAutoRefreshAnimator.setDuration(mAutoRefreshDuration);
        mAutoRefreshAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                onRefresh();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        mAutoRefreshAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                dragView(valueAnimator.getAnimatedFraction(), -1f);
            }
        });
        mAutoRefreshAnimator.start();

    }

    private void onBackDrag(final ValueAnimator valueAnimator) {
        final float fraction = valueAnimator.getAnimatedFraction();
        dragView(1 - fraction, -1f);
    }

    private void onBackTension(final ValueAnimator valueAnimator) {
        final float delta = (float) valueAnimator.getAnimatedValue();
        mSecondChild.setTranslationY(delta);
        if (mDragMode == DragMode.DRAG && mTensionMode == TensionMode.TOP && mTension > 0) {
            mFirstChild.setTranslationY(delta - mThreshold);
        }
        if (mRefreshCallback != null) {
            mRefreshCallback.onTensionUp(1 - ((delta - mTension) / mTension));
        }
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

    private void setFirstChildDefaultState() {
        if (mFirstChild.getTranslationY() != 0f) {
            mFirstChild.setTranslationY(0f);
        }
    }

    private void reset() {
        mIsRefreshing = false;
        mIsChildTouched = false;
        mOverScrollDelta = 0f;
        mStartYValue = 0f;
        mMaxYValue = 0f;
        mLastYValue = 0f;
        mSecondChild.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0, mRestoreYValue - mSecondChildTopPosition, 0, 0, 0, 0, 0, 0, 0));
        mRestoreYValue = 0f;
        setTag(null);
    }

    public void setThreshold(final int threshold) {
        mThreshold = threshold;
        calculateTotalHeight();
    }

    public void setTension(final int tension) {
        mTension = tension < 0 ? 0 : tension;
        calculateTotalHeight();
    }

    public void setDragMode(final DragMode dragMode) {
        mDragMode = dragMode;
        setupLayoutByMode(dragMode);
    }

    public void setTensionMode(final TensionMode tensionMode) {
        mTensionMode = tensionMode;
    }

    public void setDragCoefficient(final float dragCoefficient) {
        mDragCoefficient = dragCoefficient < 1 ? 1 : dragCoefficient;
    }

    public void setTensionCoefficient(final float tensionCoefficient) {
        mTensionCoefficient = tensionCoefficient < 1 ? 1 : tensionCoefficient;
    }

    public void setParallaxCoefficient(final float parallaxCoefficient) {
        if (parallaxCoefficient < 0f || parallaxCoefficient > 1f) {
            mParallaxCoefficient = 0.5f;
        } else {
            mParallaxCoefficient = parallaxCoefficient;
        }
        setupLayoutByMode(mDragMode);
    }

    public void setupLayoutByMode(final DragMode dragMode) {
        if (mFirstChild == null) return;
        final LayoutParams firstChildLayoutParams = (LayoutParams) mFirstChild.getLayoutParams();
        mFirstChildHeight = firstChildLayoutParams.height;
        if (firstChildLayoutParams == null) return;
        switch (dragMode) {
            case OVERLAY:
                mFirstChild.setTranslationY(0);
                break;
            case DRAG:
                mFirstChild.setTranslationY(-mFirstChildHeight);
                break;
            case PARALLAX:
                if (mParallaxCoefficient == 0f) {
                    mParallaxCoefficient = 0.5f;
                }
                final float parallaxShift = -mFirstChildHeight * mParallaxCoefficient;
                mFirstChild.setTranslationY(parallaxShift);
                break;
        }
        mFirstChild.requestLayout();
    }

    private void calculateTotalHeight() {
        mTotalHeight = mThreshold + mTension;
    }

    public void setFullBackDuration(final int fullBackDuration) {
        this.mFullBackDuration = fullBackDuration;
    }

    public void setCancelBackDuration(final int cancelBackDuration) {
        this.mCancelBackDuration = cancelBackDuration;
    }

    public void setTensionBackDuration(final int tensionBackDuration) {
        this.mTensionBackDuration = tensionBackDuration;
    }

    public void setAutoRefreshDuration(final int autoRefreshDuration) {
        this.mAutoRefreshDuration = autoRefreshDuration;
    }

    public boolean isRefreshing() {
        return mIsRefreshing;
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        setFullBackDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_fullBackDuration, 300));
        setCancelBackDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_cancelBackDuration, 200));
        setTensionBackDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_cancelBackDuration, 200));
        setAutoRefreshDuration(a.getInteger(R.styleable.PullToRefreshLayout_ptr_autoRefreshDuration, 400));
        setThreshold(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_ptr_threshold, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_threshold)));
        setTension(a.getDimensionPixelSize(R.styleable.PullToRefreshLayout_ptr_tension, getResources().getDimensionPixelOffset(R.dimen.pull_to_refresh_tension)));
        setDragCoefficient(a.getFloat(R.styleable.PullToRefreshLayout_ptr_drag_coefficient, 1.0f));
        setTensionCoefficient(a.getFloat(R.styleable.PullToRefreshLayout_ptr_tension_coefficient, 3.0f));
        setParallaxCoefficient(a.getFloat(R.styleable.PullToRefreshLayout_ptr_parallax_coefficient, 0.5f));
        setDragMode(DragMode.values()[a.getInt(R.styleable.PullToRefreshLayout_ptr_drag_mode, 0)]);
        setTensionMode(TensionMode.values()[a.getInt(R.styleable.PullToRefreshLayout_ptr_tension_mode, 0)]);
        requestLayout();
        a.recycle();
    }

    private void onActionDown(final float yAxis) {
        mStartYValue = yAxis - mLastYValue;
        mIncreasedThresholdYValue = (mThreshold * mDragCoefficient);
        mIncreasedTensionYValue = (mTension * mTensionCoefficient);
        mMaxYValue = mStartYValue + mIncreasedThresholdYValue + mIncreasedTensionYValue;
        mOverScrollDelta = 0f;
        mIsChildTouched = true;
    }

    private int getTopPosition(final View view) {
        final Rect viewRect = new Rect();
        view.getGlobalVisibleRect(viewRect);
        return viewRect.top;
    }

    private void tryToFinishBackAnimators() {
        if (mSecondChildTopPosition > 0) {
            if (mDragUpAnimator != null && mDragUpAnimator.isRunning()) {
                mDragUpAnimator.cancel();
            }
            if (mDragTensionUpAnimator != null && mDragTensionUpAnimator.isRunning()) {
                mDragTensionUpAnimator.cancel();
            }
        }
    }


    public void setRefreshCallback(final RefreshCallback refreshCallback) {
        mRefreshCallback = refreshCallback;
    }

    public void setOnChildTouchListener(final OnChildTouchListener onChildTouchListener) {
        mOnChildTouchListener = onChildTouchListener;
    }

    public void setOnListViewScrollListener(final OnListViewScrollListener onListViewScrollListener) {
        mOnListViewScrollListener = onListViewScrollListener;
    }

    public void setOnPullToRefreshTouchEvent(final OnPullToRefreshTouchEvent onPullToRefreshTouchEvent) {
        mOnPullToRefreshTouchEvent = onPullToRefreshTouchEvent;
    }

    public void setOnNestedScrollViewScrollListener(final OnNestedScrollViewScrollListener onNestedScrollViewScrollListener) {
        mOnNestedScrollViewScrollListener = onNestedScrollViewScrollListener;
    }

}
