package com.john.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Function: 依据需要裁剪圆形波浪效果. <br/>
 * 核心公式 y=Asin(ωx+φ)+k
 *
 * @author qinchong
 * @see <a href="https://github.com/john990/WaveView">参考自WaveView</a>
 * @since JDK 1.6
 */
public class CircleWaveView extends View {

    private static final String TAG = CircleWaveView.class.getSimpleName();
    private final boolean DEBUG = false;

    private int mAboveWaveColor;
    private int mBlowWaveColor;
    private int mProgress;
    private int mMaxProgress;
    private int mWaveToTop;

    private final int DEFAULT_ABOVE_WAVE_COLOR = Color.WHITE;
    private final int DEFAULT_BLOW_WAVE_COLOR = Color.WHITE;

    public final int DEFAULT_ABOVE_WAVE_ALPHA = 255;
    public final int DEFAULT_BLOW_WAVE_ALPHA = 255;
    public final int MAX_PROGRESS = 1000;
    private final int DEFAULT_PROGRESS = MAX_PROGRESS / 2;

    private final float X_SPACE = 20;
    private final double PI2 = 2 * Math.PI;

    private Path mAboveWavePath = new Path();
    private Path mBlowWavePath = new Path();
    private Path mClipPath;

    private Paint mAboveWavePaint = new Paint();
    private Paint mBlowWavePaint = new Paint();

    private float mWaveMultiple;
    private float mWaveLength;
    private int mWaveHeight;
    private float mMaxRight;
    private float mWaveHz;

    // wave animation
    private float mAboveOffset = 0.0f;
    private float mBlowOffset;

    private RefreshProgressRunnable mRefreshProgressRunnable;

    private int left, right, bottom;
    // ω
    private double omega;
    private float mCirclePadding;
    private int mInterval = 25;
    private boolean mRefresh = true;

    public CircleWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // load styled attributes.
        final TypedArray attributes =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveView, R.attr.waveViewStyle, 0);
        mAboveWaveColor = attributes.getColor(R.styleable.WaveView_above_wave_color, DEFAULT_ABOVE_WAVE_COLOR);
        mBlowWaveColor = attributes.getColor(R.styleable.WaveView_blow_wave_color, DEFAULT_BLOW_WAVE_COLOR);
        mProgress = attributes.getInt(R.styleable.WaveView_progress, DEFAULT_PROGRESS);
        mCirclePadding = attributes.getDimension(R.styleable.WaveView_circle_padding, 0);
        mMaxProgress = attributes.getInt(R.styleable.WaveView_maxprogress, MAX_PROGRESS);
        attributes.recycle();

        mWaveMultiple = 1.5f;
        mWaveHeight = 20;
        mWaveHz = 0.05f;
        mBlowOffset = getBlowOffset();
        setAboveWaveColor(mAboveWaveColor);
        setBlowWaveColor(mBlowWaveColor);
        initializePainters();
        mClipPath = new Path();
        setProgress(mProgress);
        // http://developer.android.com/intl/zh-cn/guide/topics/graphics/hardware-accel.html#unsupported
        // close hardwareAccelerated in case of
        // java.lang.UnsupportedOperationException at android.view.GLES20Canvas.clipPath(GLES20Canvas.java:446)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (DEBUG) {
            Log.d(TAG, "init CircleWaveView");
            dump();
        }
    }

    public void dump() {
        StringBuilder sb = new StringBuilder();
        Log.d(TAG, "---------------------CircleWaveView dump---------------------");
        sb.append("mWaveMultiple=" + mWaveMultiple + "\n");
        sb.append("mWaveHeight=" + mWaveHeight + "\n");
        sb.append("mWaveHz=" + mWaveHz + "\n");
        sb.append("mBlowOffset=" + mBlowOffset + "\n");
        sb.append("omega=" + omega + "\n");
        sb.append("mWaveLength=" + mWaveLength + "\n");
        sb.append("left=" + left + "\n");
        sb.append("right=" + right + "\n");
        sb.append("bottom=" + bottom + "\n");
        sb.append("mCirclePadding=" + mCirclePadding + "\n");
        sb.append("getWidth=" + getWidth() + "\n");
        sb.append("getHeight=" + getHeight() + "\n");
        Log.d(TAG, sb.toString());
    }

    public void setWaveHeight(int waveHeight) {
        mWaveHeight = waveHeight;
    }

    public void setRefreshInterval(int interval) {
        mInterval = interval;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        // Clip circle to show
        mClipPath.reset();
        canvas.clipPath(mClipPath); // makes the clip empty
        int width = getWidth();
        mClipPath.addCircle(width / 2, width / 2, width / 2 - mCirclePadding, Path.Direction.CCW);
        canvas.clipPath(mClipPath, Region.Op.REPLACE);

        // draw wave
        canvas.drawPath(mAboveWavePath, mAboveWavePaint);
        canvas.drawPath(mBlowWavePath, mBlowWavePaint);

        canvas.restore();
    }

    public void setProgress(int progress) {
        this.mProgress = progress * 10 > MAX_PROGRESS ? MAX_PROGRESS : progress * 10;
        computeWaveToTop();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public int getMaxProgress() {
        return this.mMaxProgress;
    }

    private void computeWaveToTop() {
        mWaveToTop = (int) (getHeight() * (1f - mProgress * 1.0f / mMaxProgress));
    }

    public void setAboveWaveColor(int aboveWaveColor) {
        this.mAboveWaveColor = aboveWaveColor;
    }

    public void setBlowWaveColor(int blowWaveColor) {
        this.mBlowWaveColor = blowWaveColor;
    }

    private float getBlowOffset() {
        return (float) (PI2 / 6);
    }

    public void initializePainters() {
        mAboveWavePaint.setColor(mAboveWaveColor);
        // mAboveWavePaint.setAlpha(DEFAULT_ABOVE_WAVE_ALPHA);
        mAboveWavePaint.setStyle(Paint.Style.FILL);
        mAboveWavePaint.setAntiAlias(true);

        mBlowWavePaint.setColor(mBlowWaveColor);
        // mBlowWavePaint.setAlpha(DEFAULT_BLOW_WAVE_ALPHA);
        mBlowWavePaint.setStyle(Paint.Style.FILL);
        mBlowWavePaint.setAntiAlias(true);
    }

    /**
     * calculate wave track
     */
    private void calculatePath() {
        mAboveWavePath.reset();
        mBlowWavePath.reset();

        getWaveOffset();

        float y;
        mAboveWavePath.moveTo(left, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x - mAboveOffset));
            y += mWaveToTop;
            mAboveWavePath.lineTo(x, y);
            if (DEBUG) {
                Log.d(TAG, "Above@ x =" + x + " ,y=" + y + " ,=mAboveOffset=" + mAboveOffset);
            }
        }
        mAboveWavePath.lineTo(right, bottom);

        mBlowWavePath.moveTo(left, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x - mBlowOffset));
            y += mWaveToTop;
            mBlowWavePath.lineTo(x, y);
            if (DEBUG) {
                Log.d(TAG, "Blow@ x=" + x + " ,y=" + y + " ,mBlowOffset=" + mBlowOffset);
            }
        }
        mBlowWavePath.lineTo(right, bottom);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        Log.d(TAG, "onWindowVisibilityChanged---" + visibility);
        if (View.GONE == visibility) {
            removeCallbacks(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        }
    }

    /**
     * 由于绘制该图形很耗电，请在界面对用户不可见时关掉动画。
     */
    public void refreshProgress(boolean refresh) {
        mRefresh = refresh;
        if (refresh) {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void initWaveParameter() {
        Log.v(TAG, "startWave getWidth()=" + getWidth());
        if (getWidth() != 0) {
            int width = getWidth();
            mWaveLength = width * mWaveMultiple;
            left = getLeft();
            right = getRight();
            bottom = getBottom();
            mMaxRight = right + X_SPACE;
            omega = PI2 / mWaveLength;
            dump();
        }
    }

    private void getWaveOffset() {
        if (mBlowOffset > Float.MAX_VALUE - 100) {
            mBlowOffset = getBlowOffset();
        } else {
            mBlowOffset += mWaveHz;
        }

        if (mAboveOffset > Float.MAX_VALUE - 100) {
            mAboveOffset = 0;
        } else {
            mAboveOffset += mWaveHz;
        }
    }

    private class RefreshProgressRunnable implements Runnable {

        public void run() {
            synchronized (CircleWaveView.this) {

                if (!mRefresh) {
                    return;
                }

                if (mWaveLength == 0) {
                    computeWaveToTop();
                    initWaveParameter();
                }

                long start = System.currentTimeMillis();
                calculatePath();
                invalidate();
                long gap = mInterval - (System.currentTimeMillis() - start);
                if (DEBUG) {
                    postDelayed(this, 1000);
                } else {
                    postDelayed(this, gap < 0 ? 0 : gap);
                }
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.progress = mProgress;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
    }

    private static class SavedState extends BaseSavedState {
        int progress;

        /**
         * Constructor called from {@link android.widget.ProgressBar#onSaveInstanceState()}
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}