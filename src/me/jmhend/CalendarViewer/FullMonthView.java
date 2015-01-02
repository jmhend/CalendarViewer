package me.jmhend.CalendarViewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by jmhend on 12/30/14.
 */
public class FullMonthView extends MonthView {

    private static final String TAG = FullMonthView.class.getSimpleName();

////=============================================================================
//// Member variables.
////=============================================================================

    private int mColorCurrentMonthDate;
    private int mColorOtherMonthDate;
    private int mColorClickedDayDate;
    private int mColorTodayDate;

    private Paint mGridPaint;
    private Paint mDateLabelPaint;
    private Paint mClickedDayPaint;
    private Paint mCurrentMonthCellPaint;

    private int mClickedDayIndex = -1;

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

////=============================================================================
//// View.
////=============================================================================

    @Override
    protected void init() {
        super.init();
        Resources res = getContext().getResources();

        mColorCurrentMonthDate = res.getColor(R.color.accent_blue);
        mColorOtherMonthDate = res.getColor(R.color.other_month_date_label);
        mColorClickedDayDate = res.getColor(android.R.color.white);
        mColorTodayDate = res.getColor(android.R.color.white);

        mGridPaint = new Paint();
        mGridPaint.setColor(res.getColor(R.color.full_month_grid_color));
        mGridPaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.full_month_grid_thickness));
        mGridPaint.setStyle(Paint.Style.FILL);

        mClickedDayPaint = new Paint();
        mClickedDayPaint.setColor(res.getColor(R.color.accent_blue_highlight));
        mClickedDayPaint.setStyle(Paint.Style.FILL);

        mCurrentMonthCellPaint = new Paint();
        mCurrentMonthCellPaint.setStyle(Paint.Style.FILL);
        mCurrentMonthCellPaint.setColor(res.getColor(R.color.full_month_current_month_cell_color));

        mDateLabelPaint = new Paint();
        mDateLabelPaint.setTypeface(mTypeface);
        mDateLabelPaint.setColor(mColorCurrentMonthDate);
        mDateLabelPaint.setTextSize(res.getDimensionPixelSize(R.dimen.full_month_date_label_size));
        mDateLabelPaint.setAntiAlias(true);
        mDateLabelPaint.setStyle(Paint.Style.FILL);
    }

////=============================================================================
//// Draw
////=============================================================================

    @Override
    public void onDraw(Canvas canvas) {
        calculateDayPoints();

        for (int i = 0; i < MAX_DAYS; i++) {
            int x = mDayXs[i];
            int y = mDayYs[i];

            boolean isClickedDay = isClickedDay(i);
            boolean isToday = isIndexCurrentDay(i);
            boolean isThisMonth = isIndexInThisMonth(i);

            if (isThisMonth) {
                int right = x + getWidth() / 7;
                int bottom = y +  getHeight() / NUM_WEEKS;
                canvas.drawRect(x, y, right, bottom, mCurrentMonthCellPaint);
            }

            int dateTextColor;
            if (isClickedDay) {
                dateTextColor = mColorClickedDayDate;
            } else if (isToday) {
                dateTextColor = mColorTodayDate;
            } else if (!isThisMonth) {
                dateTextColor = mColorOtherMonthDate;
            } else {
                dateTextColor = mColorCurrentMonthDate;
            }

            Typeface tf = (isToday) ? mTypefaceBold : mTypeface;
            mDateLabelPaint.setTypeface(tf);
            mDateLabelPaint.setColor(dateTextColor);
            canvas.drawText(String.valueOf(mDayOfMonths[i]), x + 25, y + 40, mDateLabelPaint);

//            int year;
//            int month;
//            int dayOfMonth;
//            CalendarAdapter.CalendarDay day = new CalendarAdapter.CalendarDay();
//            if (isIndexInPreviousMonth(i)) {
//                year = mPreviousMonthYear;
//                month = mPreviousMonth;
//                dayOfMonth = mPreviousMonthLastDayOfMonth - (mMonthStartIndex - i) + 1;
//            } else if (isIndexInNextMonth(i)) {
//                year = mNextMonthYear;
//                month = mNextMonth;
//                dayOfMonth = i - mMonthEndIndex;
//            } else {
//                year = mYear;
//                month = mMonth;
//                dayOfMonth = i - mMonthStartIndex + 1;
//            }
//            day.year = year;
//            day.month = month;
//            day.dayOfMonth = dayOfMonth;
//
//            List<? extends Event> events = mModel.getEventsOnDay(day.toCalendar().getTimeInMillis());
//
//            int count = events != null ? events.size() : 0;
//
//            canvas.drawText(String.valueOf(count), x + 125, y + 40, mMonthNumPaint);
        }

        drawGrid(canvas);
    }

    /**
     * Draws the grid lines for the calendar, separating each day.
     */
    private void drawGrid(Canvas canvas) {
        int heightSpace = getHeight() / 6;
        int widthSpace = getWidth() / 7;

        // Horizontal grid.
        for (int i = 1 ; i < 6; i++) {
            canvas.drawLine(0f, i * heightSpace, getWidth(), i * heightSpace, mGridPaint);
        }

        // Vertical grid.
        for (int i = 1 ; i < 7; i++) {
            canvas.drawLine(i * widthSpace, 0f, i * widthSpace, getHeight(), mGridPaint);
        }
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

////=============================================================================
//// Utils.
////=============================================================================

    /**
     * @return True if the day at 'dayIndex' is the clicked day.
     */
    protected boolean isClickedDay(int dayIndex) {
        return dayIndex != -1 && mClickedDayIndex == dayIndex;
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


}
