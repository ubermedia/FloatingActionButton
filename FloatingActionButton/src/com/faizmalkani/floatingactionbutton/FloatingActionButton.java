package com.faizmalkani.floatingactionbutton;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class FloatingActionButton extends View {

    private static final int Z_TRANSLATION_DURATION = 100;
    private static final float SHADOW_COEF_NORMAL = 0.9f;
    private static final float SHADOW_COEF_PRESSED = 0.7f;

    private final Interpolator hideInterpolator;
	private final Interpolator showInterpolator;
    private final Paint mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float shadowRadius, dx, dy;
    private final int shadowColor;
    private final float elevation;
    private final float pressedElevation;
    private Bitmap mBitmap;
    private int mColor;
    private boolean mHidden = false;
    /**
     * The FAB button's Y position when it is displayed.
     */
    private float mYDisplayed = -1;
    /**
     * The FAB button's Y position when it is hidden.
     */
    private float mYHidden = -1;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }


    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources res = getResources();
        final float defaultElevation = res.getDimension(R.dimen.fab_default_elevation);
        final float defaultElevationPressed = res.getDimension(R.dimen.fab_default_elevationPressed);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr ,0);
        mColor = a.getColor(R.styleable.FloatingActionButton_android_color, Color.WHITE);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mColor);
        elevation = a.getDimension(R.styleable.FloatingActionButton_fab_elevation, defaultElevation);
        pressedElevation = a.getDimension(R.styleable.FloatingActionButton_fab_elevationPressed, defaultElevationPressed);

        shadowColor = a.getInteger(R.styleable.FloatingActionButton_android_shadowColor, Color.argb(110, 0, 0, 0));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            hideInterpolator = showInterpolator = new AccelerateDecelerateInterpolator();
            shadowRadius = elevation;
            dx = elevation * 0.15f;
            dy = elevation * 0.3f;
            mButtonPaint.setShadowLayer(SHADOW_COEF_NORMAL * elevation, dx, dy, shadowColor);
        } else {
            hideInterpolator = AnimationUtils.loadInterpolator(context, android.R.interpolator.fast_out_linear_in);
            showInterpolator = AnimationUtils.loadInterpolator(context, android.R.interpolator.linear_out_slow_in);
            shadowRadius = 0;
            dx = dy = 0.0f;
            setElevation(elevation);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
        }

        Drawable drawable = a.getDrawable(R.styleable.FloatingActionButton_android_drawable);
        if (null != drawable) {
            mBitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            mYHidden = size.y;
        } else mYHidden = display.getHeight();
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public void setColor(int color) {
        mColor = color;
        mButtonPaint.setColor(mColor);
        invalidate();
    }

    public void setDrawable(Drawable drawable) {
        mBitmap = ((BitmapDrawable) drawable).getBitmap();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = (float) (getWidth() / 2) - shadowRadius /*- Math.max(dx, dy)*/;
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, mButtonPaint);
        if (null != mBitmap) {
            canvas.drawBitmap(mBitmap, (getWidth() - mBitmap.getWidth()) / 2,
                    (getHeight() - mBitmap.getHeight()) / 2, mDrawablePaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Perform the default behavior
        super.onLayout(changed, left, top, right, bottom);

        // Store the FAB button's displayed Y position if we are not already aware of it
        if (mYDisplayed == -1) {

            mYDisplayed = ViewHelper.getY(this);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int color;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            color = mColor;
        } else {
            color = darkenColor(mColor);
        }
        mButtonPaint.setColor(color);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.L) {
                mButtonPaint.setShadowLayer(SHADOW_COEF_PRESSED * elevation, dx, SHADOW_COEF_PRESSED * dy, shadowColor);
            } else {
                animate().translationZ(pressedElevation - elevation).setDuration(Z_TRANSLATION_DURATION);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.L) {
                mButtonPaint.setShadowLayer(SHADOW_COEF_NORMAL * elevation, dx, SHADOW_COEF_NORMAL * dy, shadowColor);
            } else {
                animate().translationZ(0).setDuration(Z_TRANSLATION_DURATION);
            }
        }

        invalidate();
        return super.onTouchEvent(event);
    }

    public boolean hide(boolean hide) {
        // If the hidden state is being updated
        if (mHidden != hide) {

            // Store the new hidden state
            mHidden = hide;

            // Animate the FAB to it's new Y position
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", mHidden ? mYHidden : mYDisplayed).setDuration(500);
            animator.setInterpolator(hide ? hideInterpolator : showInterpolator);
            animator.start();

            return true;
        }
        return false;
    }

    public void listenTo(AbsListView listView) {
        if (null != listView) {
            listView.setOnScrollListener(new DirectionScrollListener(this, !listView.isStackFromBottom()));
        }
    }

    public boolean isHidden() {
        return mHidden;
    }

    public TimeInterpolator getShowInterpolator() {
        return showInterpolator;
    }

    public TimeInterpolator getHideInterpolator() {
        return hideInterpolator;
    }
}