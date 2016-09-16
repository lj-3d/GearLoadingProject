package lj_3d.gearloadinglayout.gearViews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import lj_3d.gearloadinglayout.R;


/**
 * Created by LJ on 28.09.2015.
 */
public class CutOutLayout extends View {

    private int color;
    private int shadowColor;
    private int shadowWidth;
    private int cutRadius;
    private int height = 218;
    private int width = 620;
    private Paint mainPaint = new Paint();
    private Paint shadowPaint = new Paint();
    private Paint shadowClearPaint = new Paint();
    private final PorterDuffXfermode mPorterDuffXfermodeClear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private final PorterDuffXfermode mPorterDuffXfermodeShadowClear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private Resources mResources;
    private Bitmap mShadowBitmap;

    public void setColor(int backgroundColor) {
        this.color = backgroundColor;
        invalidate();
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
        updateTools();
        invalidate();
    }

    public void setShadowWidth(int shadowWidth) {
        this.shadowWidth = shadowWidth;
        updateTools();
        invalidate();
    }

    public void setWidth(int width) {
        this.width = width;
        updateTools();
        requestLayout();
    }

    public void setHeight(int height) {
        this.height = height;
        requestLayout();
    }

    public void setCutRadius(int cutRadius) {
        if (cutRadius == 0)
            return;
        this.cutRadius = cutRadius;
        updateTools();
        invalidate();
    }


    public CutOutLayout(Context context) {
        this(context, null);
    }

    public CutOutLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CutOutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mResources = getResources();
        initTools();
        parseAttributes(attrs);
        shadowPaint.setMaskFilter(new BlurMaskFilter(shadowWidth, BlurMaskFilter.Blur.NORMAL));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void initTools() {
        shadowPaint.setAntiAlias(true);
        shadowPaint.setDither(true);
        shadowClearPaint.setAntiAlias(true);
        shadowClearPaint.setDither(true);
        mainPaint.setAntiAlias(true);
        mainPaint.setDither(true);
        shadowPaint.setStyle(Paint.Style.STROKE);
        mainPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mainPaint.setXfermode(mPorterDuffXfermodeClear);
        shadowClearPaint.setXfermode(mPorterDuffXfermodeShadowClear);
        shadowClearPaint.setStyle(Paint.Style.STROKE);
        shadowClearPaint.setColor(Color.TRANSPARENT);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CutOutLayout);

        setColor(a.getColor(R.styleable.CutOutLayout_cutLayoutColor, Color.WHITE));
        setCutRadius((int) a.getDimension(R.styleable.CutOutLayout_cutRadius, mResources.getDimensionPixelSize(R.dimen.cut_layout_diameter)));
        setShadowWidth((int) a.getDimension(R.styleable.CutOutLayout_shadowWidth, mResources.getDimensionPixelSize(R.dimen.shadow_width)));
        setShadowColor(a.getColor(R.styleable.CutOutLayout_shadowColor, mResources.getColor(R.color.shadow_grey)));

        a.recycle();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(color);
        canvas.drawCircle(width / 2, height / 2, cutRadius, mainPaint);
        if (mShadowBitmap == null)
            mShadowBitmap = prepareShadow();
        canvas.drawBitmap(mShadowBitmap, (width / 2) - cutRadius, height / 2 - cutRadius, null);
    }

    private void updateTools() {
        shadowPaint.setColor(shadowColor);
        shadowPaint.setStrokeWidth(shadowWidth);
        shadowClearPaint.setStrokeWidth(cutRadius);
    }

    private Bitmap prepareShadow() {
        final int diameter = cutRadius * 2;
        final Bitmap bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        final Canvas shadowCanvas = new Canvas(bitmap);
        shadowCanvas.drawCircle(cutRadius, cutRadius, cutRadius + (shadowWidth / 2), shadowPaint);
        shadowCanvas.drawCircle(cutRadius, cutRadius, cutRadius * 1.5f, shadowClearPaint);
        return bitmap;
    }


}
