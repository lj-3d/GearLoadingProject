package lj_3d.gearloadinglayout.gearViews;

import android.content.Context;
import android.content.res.TypedArray;
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
    private int cutRadius;
    private int height = 218;
    private int width = 620;
    private Paint paint;
    private Paint shadow;
    private final PorterDuffXfermode mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    public void setColor(int backgroundColor) {
        this.color = backgroundColor;
    }

    public void setWidth(int width) {
        this.width = width;
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
        requestLayout();
    }


    public CutOutLayout(Context context) {
        super(context);
    }

    public CutOutLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(attrs);
        initTools();
    }

    public CutOutLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttributes(attrs);
        initTools();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void initTools() {
        paint = new Paint();
        shadow = new Paint();
        shadow.setShadowLayer(15, 0.0f, 0.0f, getResources().getColor(R.color.shadow_grey));
        shadow.setAntiAlias(true);
        shadow.setDither(true);
        shadow.setColor(getResources().getColor(R.color.shadow_grey));
        shadow.setStyle(Paint.Style.STROKE);
        shadow.setAlpha(70);
        shadow.setStrokeWidth(7);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setXfermode(mPorterDuffXfermode);
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CutOutLayout);

        color = a.getColor(R.styleable.CutOutLayout_cutLayoutColor, Color.WHITE);
        setColor(color);

        cutRadius = (int) a.getDimension(R.styleable.CutOutLayout_cutRadius, 50);
        setCutRadius(cutRadius);

        a.recycle();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(color);
        canvas.drawCircle(width / 2, height / 2, cutRadius, paint);
        canvas.drawCircle(width / 2, height / 2, cutRadius, shadow);
    }

}
