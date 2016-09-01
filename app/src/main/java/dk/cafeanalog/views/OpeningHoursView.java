package dk.cafeanalog.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import dk.cafeanalog.R;

/**
 * TODO: document your custom view class.
 */
public class OpeningHoursView extends View {
    private int mShowFromHour, mShowToHour;
    private String[] mNumbers;
    private float[] mNumberStarts;
    private float[] mLineStarts;
    private int[] mOpeningHour, mClosingHour;
    private Paint mBorderPaint, mOpenPaint, mClosedPaint, mTextPaint;
    private int mTextDistance;
    private int mBlockHeight;

    public OpeningHoursView(Context context) {
        super(context);
        init();
    }

    public OpeningHoursView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public OpeningHoursView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        init();
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.OpeningHoursView,
                0, 0);

        try {
            mShowFromHour = a.getInt(R.styleable.OpeningHoursView_show_from_hour, 0);
            mShowToHour = a.getInt(R.styleable.OpeningHoursView_show_to_hour, 24);
            calculateNumbers();

            if (mShowToHour < 0 || mShowToHour > 24) throw new IllegalArgumentException("showToHour cannot be less than 0 or greater than 24!");
            if (mShowFromHour < 0 || mShowFromHour > 24) throw new IllegalArgumentException("showFromHour cannot be less than 0 or greater than 24!");

            if (a.hasValue(R.styleable.OpeningHoursView_opening_hour)) { // If one is set, both should be.
                mOpeningHour = new int[] { a.getInt(R.styleable.OpeningHoursView_opening_hour, 0) };
                mClosingHour = new int[] { a.getInt(R.styleable.OpeningHoursView_closing_hour, 24) };
            } else {
                mOpeningHour = mClosingHour = new int[0];
            }

            mTextPaint.setColor(a.getColor(R.styleable.OpeningHoursView_text_color, Color.BLACK));
            mBorderPaint.setColor(a.getColor(R.styleable.OpeningHoursView_border_color, Color.BLACK));
            mOpenPaint.setColor(a.getColor(R.styleable.OpeningHoursView_open_color, Color.rgb(0, 150, 40)));
            mClosedPaint.setColor(a.getColor(R.styleable.OpeningHoursView_closed_color, Color.argb(0, 0, 0, 0)));

            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, r.getDisplayMetrics());

            mTextDistance = a.getDimensionPixelSize(R.styleable.OpeningHoursView_text_distance, 0);
            mTextPaint.setTextSize(a.getDimensionPixelSize(R.styleable.OpeningHoursView_text_size, (int) px));
            mBlockHeight = a.getDimensionPixelSize(R.styleable.OpeningHoursView_block_height, (int) mBorderPaint.getTextSize() * 2);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mOpenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOpenPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mClosedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClosedPaint.setStyle(Paint.Style.FILL);
    }

    private void calculateNumbers() {
        String[] numbers = new String[mShowToHour - mShowFromHour + 1];

        float[] numberWidths = new float[numbers.length];

        for (int i = mShowFromHour; i <= mShowToHour; i++) {
            String s = Integer.toString(i);
            numbers[i - mShowFromHour] = s;
            numberWidths[i - mShowFromHour] = mTextPaint.measureText(s);
        }

        mNumbers = numbers;

        float width = (getWidth() - getPaddingLeft() - getPaddingRight()) / mNumbers.length;

        mNumberStarts = new float[mNumbers.length];
        mLineStarts = new float[mNumbers.length];

        float letterSpace = width / 2;

        float lineWidth = mTextPaint.measureText("|");

        for (int i = 0; i < mNumberStarts.length; i++) {
            mNumberStarts[i] = getPaddingLeft() + letterSpace;
            letterSpace += width;
            mLineStarts[i] = mNumberStarts[i] + (numberWidths[i] / 2 - lineWidth);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateNumbers();
    }

    public int getShowFromHour() {
        return mShowFromHour;
    }

    public void setShowFromHour(int mShowFromHour) {
        this.mShowFromHour = mShowFromHour;
        calculateNumbers();
        invalidate();
        requestLayout();
    }

    public int getShowToHour() {
        return mShowToHour;
    }

    public void setShowToHour(int mShowToHour) {
        this.mShowToHour = mShowToHour;
        calculateNumbers();
        invalidate();
        requestLayout();
    }

    public int[] getOpeningHour() {
        return mOpeningHour;
    }

    public void setOpeningHour(int[] mOpeningHour) {
        this.mOpeningHour = mOpeningHour;
        invalidate();
        requestLayout();
    }

    public int[] getClosingHour() {
        return mClosingHour;
    }

    public void setClosingHour(int[] mClosingHour) {
        this.mClosingHour = mClosingHour;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int oldToIndex = 0;
        for (int i = 0; i < mOpeningHour.length; i++) {
            int fromIndex = mOpeningHour[i] - mShowFromHour;
            int toIndex = mClosingHour[i] - mShowFromHour;

            canvas.drawRect(mLineStarts[fromIndex], getPaddingTop() + mTextPaint.getTextSize() + mTextDistance, mLineStarts[toIndex], getPaddingTop() + mBlockHeight + mTextPaint.getTextSize() + mTextDistance, mOpenPaint);
            canvas.drawRect(mLineStarts[oldToIndex], getPaddingTop() + mTextPaint.getTextSize() + mTextDistance, mLineStarts[fromIndex], getPaddingTop() + mBlockHeight + mTextPaint.getTextSize() + mTextDistance, mClosedPaint);
            oldToIndex = toIndex;
        }

        canvas.drawRect(mLineStarts[oldToIndex], getPaddingTop() + mTextPaint.getTextSize() + mTextDistance, mLineStarts[mShowToHour - mShowFromHour], getPaddingTop() + mBlockHeight + mTextPaint.getTextSize() + mTextDistance, mClosedPaint);

        for (int i = 0; i < mNumbers.length; i++) {
            canvas.drawText(mNumbers[i], mNumberStarts[i], getPaddingTop() + mTextPaint.getTextSize(), mTextPaint);

            if (i + 1 < mNumbers.length) {
                canvas.drawRect(mLineStarts[i], getPaddingTop() + 1 * mTextPaint.getTextSize() + mTextDistance, mLineStarts[i + 1], getPaddingTop() + mBlockHeight + mTextPaint.getTextSize() + mTextDistance, mBorderPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics());

        int desiredWidth = getPaddingLeft() + getPaddingRight() + mNumbers.length * (int) px;
        int desiredHeight = mTextDistance + getPaddingTop() + getPaddingBottom() + (int) (mTextPaint.getTextSize()) + mBlockHeight;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }
}
