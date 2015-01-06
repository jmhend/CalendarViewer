package me.jmhend.CalendarViewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.Calendar;
import java.util.List;

/**
 * Created by jmhend on 12/30/14.
 */
public class FullMonthView extends MonthView {

    private static final String TAG = FullMonthView.class.getSimpleName();

////=============================================================================
//// Static constants.
////=============================================================================

    public static final int CLICK_DISPLAY_DURATION = 50;

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
    private Paint mTodayCirclePaint;
    private Paint mExtraEventsPaint;

    private int mTouchSlop;
    private int mTapDelay;
    private long mTouchDown;
    private boolean mCanClickDate = false;
    private int mClickedDayIndex = -1;

    private Calendar mCalendar = Calendar.getInstance();
    private CalendarAdapter.CalendarDay mCalendarDay = CalendarAdapter.CalendarDay.fromCalendar(mCalendar);

    private long[] mDates = new long[MAX_DAYS];

    private Rect mTextMeasureRect = new Rect();
    private Point mDateMeasurePoint = new Point();

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

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mTapDelay = ViewConfiguration.getTapTimeout();

        mColorCurrentMonthDate = res.getColor(R.color.accent_blue);
        mColorOtherMonthDate = res.getColor(R.color.other_month_date_label);
        mColorClickedDayDate = res.getColor(android.R.color.white);
        mColorTodayDate = /*res.getColor(android.R.color.white);*/ mColorCurrentMonthDate;

        mDateLabelSize = res.getDimensionPixelSize(R.dimen.full_month_date_label_size);
        mDateCellPadding = res.getDimensionPixelSize(R.dimen.date_cell_padding);
        mEventPadding = res.getDimensionPixelSize(R.dimen.date_cell_event_padding);
        mEventTextSize = res.getDimensionPixelSize(R.dimen.full_month_event_text_size);
        mEventColorWidth = res.getDimensionPixelSize(R.dimen.date_cell_event_color_width);

        mGridPaint = new Paint();
        mGridPaint.setColor(res.getColor(R.color.full_month_grid_color));
        mGridPaint.setStrokeWidth(res.getDimensionPixelSize(R.dimen.full_month_grid_thickness));
        mGridPaint.setStyle(Paint.Style.FILL);

        mTodayCirclePaint = new Paint();
        mTodayCirclePaint.setAntiAlias(true);
        mTodayCirclePaint.setColor(mColorCurrentMonthDate);
        mTodayCirclePaint.setStyle(Paint.Style.FILL);

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

        mExtraEventsPaint = new Paint();
        mExtraEventsPaint.setColor(res.getColor(R.color.full_month_event_text_color));
        mExtraEventsPaint.setTextSize(mEventTextSize);
        mExtraEventsPaint.setAntiAlias(true);
        mExtraEventsPaint.setStyle(Paint.Style.FILL);
        mExtraEventsPaint.setTextAlign(Paint.Align.RIGHT);

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

            if (isClickedDay) {
                canvas.drawRect(left, top, right, bottom, mTodayCellPaint);
            } else if (isThisMonth) {
                canvas.drawRect(left, top, right, bottom, mCurrentMonthCellPaint);
            }

            Point dateBottomRight = drawDateLabel(canvas, i, left, top, isClickedDay, isToday, isThisMonth, rect);
            rect.left = left + mDateCellPadding;
            rect.top = dateBottomRight.y + mEventPadding;
            rect.right = right;
            rect.bottom = bottom + mDateCellPadding;

            // Today indicator.
            if (isToday) {
                int circleX = dateBottomRight.x + mDateCellPadding;
                int circleY = top + mDateCellPadding / 2 + (mDateLabelSize) - mDateLabelSize / 3;
                canvas.drawCircle(circleX, circleY, 8, mTodayCirclePaint);
            }

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
     * @return The (x,y)-coordinate of the lower-right bound of the text label's bounding Rect.
     */
    private Point drawDateLabel(Canvas canvas, int dateIndex, int cellX, int cellY,
                               boolean isClickedDay, boolean isToday, boolean isThisMonth, Rect rect) {
        int dateTextColor;
        if (isClickedDay) {
            dateTextColor = mColorCurrentMonthDate;
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

        mDateMeasurePoint.set(startX + (rect.right - rect.left) , startY + rect.bottom);
        return mDateMeasurePoint;
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

        int eventsDrawn = 0;
        int currYTop = bounds.top;

        for (int i = 0; i < count; i++) {
            if (currYTop >= bounds.bottom) {
                break;
            }

            Event event = events.get(i);
            if (!mModel.shouldDrawEvent(event)) {
                break;
            }

            int eventColorLeft = bounds.left;
            int eventColorRight = eventColorLeft + mEventColorWidth;
            int eventLabelLeft = eventColorRight + mEventPadding;

            String title = event.getDrawingTitle();
            String actual = clipText(title, mEventTextPaint, bounds.right - eventLabelLeft);
            mEventTextPaint.getTextBounds(actual, 0, actual.length(), mTextMeasureRect);

            int measuredHeight = mTextMeasureRect.bottom - mTextMeasureRect.top;
            int textHeight = mEventTextSize;
            int currYBottom = currYTop + textHeight;
            int nextYTop = currYBottom + mEventPadding;
            int drawingY = currYTop + measuredHeight + ((textHeight - measuredHeight) / 2);
            int colorDrawingTop = currYTop + (mEventPadding / 2);
            int colorDrawingBottom = currYTop + textHeight - (mEventPadding / 2);

            // Draw the remaining count of events for this date.
            if (count - eventsDrawn > 2 && (nextYTop >= bounds.bottom - mEventPadding - textHeight)) {
                String extraEvents = "+".concat(String.valueOf(count - eventsDrawn));
                int extrasX = bounds.right - mEventPadding * 2;
                int extrasY = bounds.bottom - mEventPadding - textHeight / 2;
                canvas.drawText(extraEvents, extrasX, extrasY, mExtraEventsPaint);
                break;
            }

            // If room, draw the next event.
            if (nextYTop < bounds.bottom) {
                mEventColorPaint.setColor(event.getDrawingColor());
                canvas.drawRect(eventColorLeft, colorDrawingTop, eventColorRight, colorDrawingBottom, mEventColorPaint);

                canvas.drawText(actual, eventLabelLeft, drawingY, mEventTextPaint);
                currYTop = nextYTop;
                eventsDrawn++;
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
    }


    /**
     * Finds the day at (x, y)
     */
    @Override
    public CalendarAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int index = getDateIndexFromLocation(x, y);
        if (index == -1) {
            return null;
        }

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
     * @return The index in the mDayOfMonths array at the (x,y) coordinate.
     */
    protected int getDateIndexFromLocation(float x, float y) {
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
        return index;
    }


    /**
     * Clips the text and adds an period if it requires more width than maxWidth;
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
            clipped = text.substring(0, breakpoint).concat(".");
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

////=============================================================================
//// Touch.
////=============================================================================

    /*
 * (non-Javadoc)
 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
 */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                if (distance(mFirstTouchX, mLastTouchX, mFirstTouchY, mLastTouchY) > mTouchSlop) {
                    MotionEvent cancel = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    handleTouchInternal(cancel);
                    return super.onTouchEvent(cancel);
                }
                mLastTouchX = e.getX();
                mLastTouchY = e.getY();
                break;
            case MotionEvent.ACTION_DOWN:
                mFirstTouchX = e.getX();
                mFirstTouchY = e.getY();
                mLastTouchX = e.getX();
                mLastTouchY = e.getY();
                break;
            default:
        }
        handleTouchInternal(e);
        return super.onTouchEvent(e);
    }

    /**
     * @return Cartesian distance of the points.
     */
    private int distance(float x1, float x2, float y1, float y2) {
        return (int) Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1),2) );
    }

    /**
     * Responds to touches without intercepting any MotionEvents.
     */
    private boolean handleTouchInternal(MotionEvent e) {
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            int index = getDateIndexFromLocation(e.getX(), e.getY());
            if (index >= 0) {
                mCanClickDate = true;
                mTouchDown = System.currentTimeMillis();
                postDelayed(new ClickRunnable(index), mTapDelay);
                return true;
            }
            return false;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            mClickedDayIndex = -1;
            mCanClickDate = false;
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_UP) {
            int index = getDateIndexFromLocation(e.getX(), e.getY());
            if (index >= 0) {
                long clearDelay = (CLICK_DISPLAY_DURATION + mTapDelay) - (System.currentTimeMillis() - mTouchDown);
                if (clearDelay > 0) {
                    postDelayed(new ClearRunnable(index), clearDelay);
                } else {
                    post(new ClearRunnable(index));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a delayed click event.
     * @author jmhend
     */
    private class ClickRunnable implements Runnable {

        public ClickRunnable(int index) {
            mIndex = index;
        }

        private int mIndex;
        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            if (mCanClickDate) {
                mClickedDayIndex = mIndex;
                invalidate();
            }
        }
    }

    /**
     * Called when an item has clicked and needs to be cleared.
     * @author jmhend
     *
     */
    private class ClearRunnable implements Runnable {

        public ClearRunnable(int index) { }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            mClickedDayIndex = -1;
            invalidate();
        }
    }
}
