package me.jmhend.CalendarViewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import java.util.List;

/**
 * Created by jmhend on 12/30/14.
 */
public class FullMonthView extends MonthView {

    private static final String TAG = FullMonthView.class.getSimpleName();

////=============================================================================
//// Member variables.
////=============================================================================

    private Paint mGridPaint;
    private Paint mSelectedDayPaint;

////=============================================================================
//// Constructor.
////=============================================================================

    public FullMonthView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FullMonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullMonthView(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        mGridPaint = new Paint();
        mGridPaint.setColor(0x44AAAAAA);
        mGridPaint.setStyle(Paint.Style.FILL);
        mGridPaint.setStrokeWidth(2);

        mSelectedDayPaint = new Paint();
        mSelectedDayPaint.setColor(0x66114488);
        mSelectedDayPaint.setStyle(Paint.Style.FILL);

        super.init();

//        mSelectedDayColor;
//        mTodayNumberColor;
//        mInactiveDayTextColor;
        mActiveDayTextColor = 0xFF444444;

//        setOnClickListener(new OnClickListener() {
//            /*
//             * (non-Javadoc)
//             * @see android.view.View.OnClickListener#onClick(android.view.View)
//             */
//            @Override
//            public void onClick(View v) {
//                CalendarAdapter.CalendarDay day = getDayFromLocation(mLastTouchX, mLastTouchY);
//                if (day != null) {
//                    Toast.makeText(getContext(), day.toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }


    @Override
    public void onDraw(Canvas canvas) {
        calculateDayPoints();

//        canvas.drawColor(0xFF114499);
        canvas.drawColor(0xFFFFFFFF);

        int height = getHeight();
        int width = getWidth();
        int heightSpace = height / 6;
        int widthSpace = width / 7;

        for (int i = 0; i < MAX_DAYS; i++) {
            int x = mDayXs[i];
            int y = mDayYs[i];
//            canvas.drawCircle(x, y, 4, mGridPaint);

            if (isIndexSelectedDay(i)) {
                int rightX = x + widthSpace;
                int bottomY = y + heightSpace;
                canvas.drawRect(x, y, rightX, bottomY, mSelectedDayPaint);
            }

            boolean isSelectedDay = isIndexSelectedDay(i);
            boolean isToday = isIndexCurrentDay(i);
            boolean isThisMonth = isIndexInThisMonth(i);

            int textColor;
            if (isSelectedDay) {
                textColor = mSelectedDayColor;
            } else if (isToday) {
                textColor = mTodayNumberColor;
            } else if (!isThisMonth) {
                textColor = mInactiveDayTextColor;
            } else {
                textColor = mSelectedDayColor;
            }
            Typeface tf = (isToday) ? mTypefaceBold : mTypeface;
            mMonthNumPaint.setTextSize(36);
            mMonthNumPaint.setTypeface(tf);
            mMonthNumPaint.setColor(textColor);
            canvas.drawText(String.valueOf(mDayOfMonths[i]), x + 25, y + 40, mMonthNumPaint);

            int year;
            int month;
            int dayOfMonth;
            CalendarAdapter.CalendarDay day = new CalendarAdapter.CalendarDay();
            if (isIndexInPreviousMonth(i)) {
                year = mPreviousMonthYear;
                month = mPreviousMonth;
                dayOfMonth = mPreviousMonthLastDayOfMonth - (mMonthStartIndex - i) + 1;
            } else if (isIndexInNextMonth(i)) {
                year = mNextMonthYear;
                month = mNextMonth;
                dayOfMonth = i - mMonthEndIndex;
            } else {
                year = mYear;
                month = mMonth;
                dayOfMonth = i - mMonthStartIndex + 1;
            }
            day.year = year;
            day.month = month;
            day.dayOfMonth = dayOfMonth;

            List<? extends Event> events = mModel.getEventsOnDay(day.toCalendar().getTimeInMillis());

            int count = events != null ? events.size() : 0;

            canvas.drawText(String.valueOf(count), x + 125, y + 40, mMonthNumPaint);


        }

        // Horizontal grid.
        for (int i = 1 ; i < 6; i++) {
            canvas.drawLine(0f, i * heightSpace, getWidth(), i * heightSpace, mGridPaint);
        }

        // Vertical grid.
        for (int i = 1 ; i < 7; i++) {
            canvas.drawLine(i * widthSpace, 0f, i * widthSpace, getHeight(), mGridPaint);
        }


//        super.onDraw(canvas);

    }

    /**
     * Calculates the (x,y) coordinates of each day in the month.
     */
    protected void calculateDayPoints() {
        clearDayArrays();

        final int xSpacing = getWidth() / 7;
        final int ySpacing = getHeight() / NUM_WEEKS;

        for (int i = 0; i < MAX_DAYS; i++) {
            // Top-Left corner of the date cell.
            mDayXs[i] = (i % 7) * xSpacing;
            mDayYs[i] = (i / 7) * ySpacing;

            int day;

            // Previous Month
            if (isIndexInPreviousMonth(i)) {
                day = mPreviousMonthLastDayOfMonth - (mMonthStartIndex - i) + 1;

                // Next Month
            } else if (isIndexInNextMonth(i)) {
                day = i - mMonthEndIndex;

                // This Month
            } else {
                day = i - mMonthStartIndex + 1;

                if (mFocusedDayOfMonth == day) {
                    mInvisibleWeekY = mDayYs[i];
                }
            }
            mDayOfMonths[i] = day;
        }
    }

    /**
     * Finds the day at (x, y)
     */
    @Override
    public CalendarAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int width = getWidth();
        int height = getHeight();

        if (x < 0 || x > width || y < 0 || y > height) {
            throw new IllegalStateException("Invalid (x,y) position (" + x + "," + y + ")");
        }

        final int xSpacing = width / 7;
        final int ySpacing = height / NUM_WEEKS;

        int xCell = ((int) x) / xSpacing;
        int yCell = ((int) y) / ySpacing;

        int index = yCell * 7 + xCell;

        CalendarAdapter.CalendarDay day = new CalendarAdapter.CalendarDay();

        if (isIndexInPreviousMonth(index)) {
            day.year = mPreviousMonthYear;
            day.month = mPreviousMonth;
        } else if (isIndexInNextMonth(index)) {
            day.year = mNextMonthYear;
            day.month = mNextMonth;
        } else {
            day.year = mYear;
            day.month = mMonth;
        }
        day.dayOfMonth = mDayOfMonths[index];
        return day;
    }

    /**
     * Uses MATCH_PARENT, instead of MonthView's sizing.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }
}
