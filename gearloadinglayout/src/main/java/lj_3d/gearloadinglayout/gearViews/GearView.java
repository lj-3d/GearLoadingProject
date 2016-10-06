package lj_3d.gearloadinglayout.gearViews;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import lj_3d.gearloadinglayout.R;

/**
 * Created by LJ on 28.03.2016.
 */
public class GearView extends View {

    private Paint mainTeethPaint;
    private Paint mainCirclePaint;
    private Paint innerCirclePaint;
    private Paint cutCenterPaint;

    private Path path;

    private ObjectAnimator rotateAnimation = new ObjectAnimator();

    private final PorterDuffXfermode mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private int cutOffset;
    private float rotateOffset;
    private int teethWidth = 40;
    private int mainDiameter = 400;
    private int secondDiameter = 150;
    private int duration = 3000;
    private int innerDiameter = mainDiameter / 6;

    private int mainColor = Color.RED;
    private int innerColor = Color.WHITE;

    private boolean enableCutCenter = true;

    private Bitmap mainBitmap;
    private Bitmap teeth;
    private Matrix matrix;

    private boolean reverse;

    public GearView(Context context) {
        this(context, null);
    }

    public GearView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GearView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBaseTools();
        parseAttributes(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int desiredWSpec = MeasureSpec.makeMeasureSpec(mainDiameter, MeasureSpec.EXACTLY);
        final int desiredHSpec = MeasureSpec.makeMeasureSpec(mainDiameter, MeasureSpec.EXACTLY);
        super.onMeasure(desiredWSpec, desiredHSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(mainDiameter, mainDiameter, w, h);
        mainBitmap = Bitmap.createBitmap(mainDiameter, mainDiameter, Bitmap.Config.ARGB_8888);
        drawGear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mainBitmap, 0, 0, null);
    }

    public void setTeethWidth(int width) {
        this.teethWidth = width;
    }

    public void setColor(int color) {
        this.mainColor = color;
    }

    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
    }

    public void enableCuttedCenter(boolean enableCutCenter) {
        this.enableCutCenter = enableCutCenter;
    }

    public void setMainDiameter(int mainDiameter) {
        this.mainDiameter = mainDiameter;
    }

    public void setSecondDiameter(int secondDiameter) {
        this.secondDiameter = secondDiameter;
    }

    public void setInnerDiameter(int innerDiameter) {
        this.innerDiameter = innerDiameter;
    }


    public void startSpinning(boolean reverse) {
        this.reverse = reverse;

        rotateAnimation.start();
    }

    public void stopSpinning() {
        rotateAnimation.cancel();
    }

    public void setDuration(int duration) {
        this.duration = duration;
        rotateAnimation.setDuration(duration);
    }


    private void initBaseTools() {
        mainTeethPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cutCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mainTeethPaint.setDither(true);
        mainCirclePaint.setDither(true);
        innerCirclePaint.setDither(true);
        cutCenterPaint.setDither(true);

        mainTeethPaint.setStrokeJoin(Paint.Join.ROUND);
        mainTeethPaint.setStrokeCap(Paint.Cap.ROUND);
        mainTeethPaint.setPathEffect(new CornerPathEffect(2));

        mainCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        path = new Path();

        cutCenterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        cutCenterPaint.setXfermode(mPorterDuffXfermode);
        matrix = new Matrix();

        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        rotateAnimation.setFloatValues(0f, 360f);
        rotateAnimation.setDuration(duration);
        rotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float rotateOffset = (float) animation.getAnimatedValue();
                rotateByValue(rotateOffset, reverse);
            }
        });
    }

    private void updateColors() {
        mainTeethPaint.setColor(mainColor);
        mainCirclePaint.setColor(mainColor);
        innerCirclePaint.setColor(innerColor);
    }

    public void setRotateOffset(float rotateOffset) {
        this.rotateOffset = rotateOffset;
    }

    public void rotateByValue(float rotateOffset, boolean reverse) {
        if (reverse)
            rotateOffset = 360f - rotateOffset;
        ViewCompat.setRotation(this, rotateOffset);
    }


    private void drawTeeth() {
        updateColors();
        teeth = Bitmap.createBitmap(mainDiameter, mainDiameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(teeth);

        int xZero = (mainDiameter / 2) - (teethWidth / 2);
        int xMax = (mainDiameter / 2) + (teethWidth / 2);
        int yZero = 0;
        cutOffset = teethWidth / 6;
        path.moveTo(xZero + cutOffset, yZero);
        path.lineTo(xMax - cutOffset, yZero);
        path.lineTo(xMax, teethWidth);
        path.lineTo(xMax, mainDiameter - teethWidth);
        path.lineTo(xMax - cutOffset, mainDiameter);
        path.lineTo(xZero + cutOffset, mainDiameter);
        path.lineTo(xZero, mainDiameter - teethWidth);
        path.lineTo(xZero, teethWidth);
        path.close();
        canvas.drawPath(path, mainTeethPaint);
    }

    private void drawGear() {
        final Canvas mainCanvas = new Canvas(mainBitmap);
        drawTeeth();
        for (float angle = (0 + rotateOffset); angle <= (150 + rotateOffset); angle = (angle + 30)) {
            matrix.setRotate(angle, mainDiameter / 2, mainDiameter / 2);
            mainCanvas.drawBitmap(teeth, matrix, null);
        }
        mainCanvas.drawCircle(mainDiameter / 2, mainDiameter / 2, secondDiameter / 2, mainCirclePaint);
        mainCanvas.drawCircle(mainDiameter / 2, mainDiameter / 2, innerDiameter, enableCutCenter ? cutCenterPaint : innerCirclePaint);
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GearView);
        setColor(a.getColor(R.styleable.GearView_mainColor, Color.RED));
        setInnerColor(a.getColor(R.styleable.GearView_innerColor, Color.WHITE));
        setMainDiameter((int) a.getDimension(R.styleable.GearView_mainDiameter, 400));
        setSecondDiameter((int) a.getDimension(R.styleable.GearView_secondDiameter, 150));
        setInnerDiameter((int) a.getDimension(R.styleable.GearView_innerDiameter, 50));
        setTeethWidth((int) a.getDimension(R.styleable.GearView_teethWidth, 40));
        setRotateOffset(a.getFloat(R.styleable.GearView_rotateAngle, 0));
        enableCuttedCenter(a.getBoolean(R.styleable.GearView_enableCutCenter, false));
        requestLayout();
        a.recycle();
    }
}
