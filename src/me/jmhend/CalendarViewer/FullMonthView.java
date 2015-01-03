package me.jmhend.CalendarViewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

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

    private int mDateLabelSize;
    private int mDateCellPadding;
    private int mEventPadding;
    private int mEventTextSize;
    private int mEventColorWidth;

    private Paint mGridPaint;
    private Paint mDateLabelPaint;
    private Paint mClickedDayPaint;
    private Paint mCurrentMonthCellPaint;
    private Paint mTodayCellPaint;
    private Paint mEventTextPaint;
    private Paint mEventColorPaint;

    private int mClickedDayIndex = -1;

    private Calendar mCalendar = Calendar.getInstance();
    private CalendarAdapter.CalendarDay mCalendarDay = CalendarAdapter.CalendarDay.fromCalendar(mCalendar);

    private long[] mDates = new long[MAX_DAYS];

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

        mDateLabelSize = res.getDimensionPixelSize(R.dimen.full_month_date_label_size);
        mDateCellPadding = res.getDimensionPixelSize(R.dimen.date_cell_padding);
        mEventPadding = res.getDimensionPixelSize(R.dimen.date_cell_event_padding);
        mEventTextSize = res.getDimensionPixelSize(R.dimen.full_month_event_text_size);
        mEventColorWidth = res.getDimensionPixelSize(R.dimen.date_cell_event_color_width);

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

        mTodayCellPaint = new Paint();
        mTodayCellPaint.setStyle(Paint.Style.FILL);
        mTodayCellPaint.setColor(res.getColor(R.color.accent_blue_highlight));

        mDateLabelPaint = new Paint();
        mDateLabelPaint.setTypeface(mTypeface);
        mDateLabelPaint.setColor(mColorCurrentMonthDate);
        mDateLabelPaint.setTextSize(mDateLabelSize);
        mDateLabelPaint.setAntiAlias(true);
        mDateLabelPaint.setStyle(Paint.Style.FILL);

        mEventTextPaint = new Paint();
        mEventTextPaint.setColor(res.getColor(R.color.full_month_event_text_color));
        mEventTextPaint.setTextSize(mEventTextSize);
        mEventTextPaint.setAntiAlias(true);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setTextAlign(Paint.Align.LEFT);
//        mEventTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf"));

        mEventColorPaint = new Paint();
        mEventColorPaint.setStyle(Paint.Style.FILL);
    }

////=============================================================================
//// Draw
////=============================================================================

    @Override
    public void onDraw(Canvas canvas) {
        calculateDayPoints();

        final Rect rect = new Rect();

        // Cell by cell.
        for (int i = 0; i < MAX_DAYS; i++) {
            int left = mDayXs[i];
            int top = mDayYs[i];
            int right = left + getWidth() / 7;
            int bottom = top +  getHeight() / NUM_WEEKS;

            boolean isClickedDay = isClickedDay(i);
            boolean isToday = isIndexCurrentDay(i);
            boolean isThisMonth = isIndexInThisMonth(i);

            if (isToday) {
                canvas.drawRect(left, top, right, bottom, mTodayCellPaint);
            } else if (isThisMonth) {
                canvas.drawRect(left, top, right, bottom, mCurrentMonthCellPaint);
            }

            int textBottom = drawDateLabel(canvas, i, left, top, isClickedDay, isToday, isThisMonth, rect);
            rect.left = left + mDateCellPadding;
            rect.top = textBottom + mDateCellPadding;
            rect.right = right;
            rect.bottom = bottom + mDateCellPadding;

            drawEvents(canvas, i, rect);
        }

        drawGrid(canvas);
    }

    /**
     * Draws the grid lines for the calendar, separating each day.
     */
    private void drawGrid(Canvas canvas) {
        int heightSpace = getHeight() / 6;
        int widthSpace = getWidth() / 7;

        // Horizontal lines.
        for (int i = 1 ; i < 6; i++) {
            canvas.drawLine(0f, i * heightSpace, getWidth(), i * heightSpace, mGridPaint);
        }

        // Vertical lines.
        for (int i = 1 ; i < 7; i++) {
            canvas.drawLine(i * widthSpace, 0f, i * widthSpace, getHeight(), mGridPaint);
        }
    }

    /**
     * Draws the date label for the individual date cell.
     *
     * @return The y-coordinate of the lower bound of the text label's bounding Rect.
     */
    private int drawDateLabel(Canvas canvas, int dateIndex, int cellX, int cellY,
                               boolean isClickedDay, boolean isToday, boolean isThisMonth, Rect rect) {
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

        int startX = cellX + mDateCellPadding;
        int startY = cellY + mDateCellPadding / 2 + (mDateLabelSize);

        String dateLabel = String.valueOf(mDayOfMonths[dateIndex]);
        mDateLabelPaint.getTextBounds(dateLabel, 0, dateLabel.length(), rect);

        canvas.drawText(dateLabel, startX, startY, mDateLabelPaint);

        return startY + rect.bottom;
    }

    /**
     * Draws as many events as possible from the date into the bounding area.
     * @param canvas
     * @param dateIndex Index in the dates array.
     * @param bounds Drawing region for events, not the same as the whole date cell's bounds.
     */
    protected void drawEvents(Canvas canvas, int dateIndex, Rect bounds) {
        final long dayStart = mDates[dateIndex];
        final List<? extends Event> events = mModel.getEventsOnDay(dayStart);
        final int count = events != null ? events.size() : 0;
        if (count == 0) {
            return;
        }

        int currYTop = bounds.top;

        for (int i = 0 ; i < count; i++) {
            if (currYTop >= bounds.bottom) {
                break;
            }

            Event event = events.get(i);
            if (!mModel.shouldDrawEvent(event)) {
                break;
            }

            String title = event.getDrawingTitle();
            String actual = clipText(title, mEventTextPaint, bounds.right - bounds.left - 20);

            Rect r = new Rect();
            mEventTextPaint.getTextBounds(actual, 0, actual.length(), r);
            int textHeight = r.bottom - r.top;

            if (currYTop + textHeight + mEventPadding < bounds.bottom) {
                int colorRight = bounds.left + mEventColorWidth;
                mEventColorPaint.setColor(event.getDrawingColor());

                canvas.drawRect(bounds.left, currYTop, colorRight, currYTop + textHeight, mEventColorPaint);

                canvas.drawText(actual, colorRight + mEventColorWidth, currYTop + textHeight, mEventTextPaint);
                currYTop += textHeight + mEventPadding;
            }
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
        long s = System.currentTimeMillis();

        clearDayArrays();

        final int xSpacing = getWidth() / 7;
        final int ySpacing = getHeight() / NUM_WEEKS;

        for (int i = 0; i < MAX_DAYS; i++) {
            // Top-Left corner of the date cell.
            mDayXs[i] = (i % 7) * xSpacing;
            mDayYs[i] = (i / 7) * ySpacing;

            // Previous Month
            if (isIndexInPreviousMonth(i)) {
                mCalendarDay.dayOfMonth = mPreviousMonthLastDayOfMonth - (mMonthStartIndex - i) + 1;
                mCalendarDay.month = mPreviousMonth;
                mCalendarDay.year = mPreviousMonthYear;

                // Next Month
            } else if (isIndexInNextMonth(i)) {
                mCalendarDay.dayOfMonth = i - mMonthEndIndex;
                mCalendarDay.month = mNextMonth;
                mCalendarDay.year = mNextMonthYear;

                // This Month
            } else {
                mCalendarDay.dayOfMonth = i - mMonthStartIndex + 1;
                mCalendarDay.month = mMonth;
                mCalendarDay.year = mYear;
            }

            mCalendarDay.fillCalendar(mCalendar);

            mDayOfMonths[i] = mCalendarDay.dayOfMonth;
            mDates[i] = mCalendar.getTimeInMillis();
        }

        long e = System.currentTimeMillis();

        Log.e(TAG, (e - s) + " millis to calculate day arrays");
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
     * Clips the text and adds an ellipses if it requires more width than maxWidth;
     * @param text
     * @param p
     * @param maxWidth
     * @return
     */
    protected static String clipText(String text, Paint p, int maxWidth) {
        if (text == null) {
            return "";
        }
        int breakpoint = p.breakText(text, true, maxWidth, null);
        if (text.length() < breakpoint) {
            return text;
        }
        String clipped;
        if (breakpoint == 1) {
            clipped = ".";
        } else if (breakpoint == 0) {
            clipped = "";
        } else if (breakpoint < text.length()) {
            clipped = text.substring(0, breakpoint - 1).concat(".");
        } else {
            clipped = text;
        }
        return clipped;
    }

    @Override
    protected void clearDayArrays() {
        super.clearDayArrays();

        for (int i = 0; i < MAX_DAYS; i++) {
            mDates[i] = 0;
        }
    }


}
