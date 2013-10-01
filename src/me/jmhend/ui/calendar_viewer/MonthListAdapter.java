package me.jmhend.ui.calendar_viewer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Toast;

/**
 * Adapter for presenting a List of months.
 * 
 * @author jmhend
 */
public class MonthListAdapter extends BaseAdapter implements OnDayClickListener {
	
	private static final String TAG = MonthListAdapter.class.getSimpleName();
	
////=====================================================================================
//// Static constants.
////=====================================================================================
	
	protected static final int WEEK_7_OVERHANG_HEIGHT = 7;
	
////=====================================================================================
//// Member variables.
////=====================================================================================
	
	private final Context mContext;
	private CalendarController mController;
	private CalendarDay mSelectedDay;
	
	private int mCount;
	
////=====================================================================================
//// Constructor.
////=====================================================================================
	
	/**
	 * Constructor.
	 * @param context
	 * @param controller
	 */
	public MonthListAdapter(Context context, CalendarController controller) {
		mContext = context;
		mController = controller;
		init();
		calculateCount();
	}
	
////=====================================================================================
//// Init
////=====================================================================================
	
	/**
	 * Initialize.
	 */
	private void init() {
		Calendar now = Calendar.getInstance();
		mSelectedDay = new CalendarDay(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
	}
	
////=====================================================================================
//// BaseAdapter
////=====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// One per month in the year range.
		return mCount;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MonthView monthView;
		if (convertView == null) {
			monthView = new MonthView(mContext);
			monthView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			monthView.setClickable(true);
			monthView.setOnDayClickListener(this);
		} else {
			monthView = (MonthView) convertView;
		}
		
		Map<String, Integer> params = (Map<String, Integer>) monthView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		
		// Generate MonthView data.
		final int month = getMonthForPosition(position);
		final int year = getYearForPosition(position);
//		final int month = getMonthForPosition
//		final int year = position / 12 + mController.getMinYear();
		final int selectedDay = isSelectedDayInMonth(year, month) ? mSelectedDay.dayOfMonth : -1;
		params.put(MonthView.KEY_MONTH, Integer.valueOf(month));
		params.put(MonthView.KEY_YEAR, Integer.valueOf(year));
		params.put(MonthView.KEY_SELECTED_DAY, Integer.valueOf(selectedDay));
		params.put(MonthView.KEY_WEEK_START, Integer.valueOf(mController.getFirstDayOfWeek()));

		monthView.reset();
		monthView.setMonthParams(params);
		monthView.invalidate();
		return monthView;
	}
	
////=====================================================================================
//// 
////=====================================================================================
	
	/**
	 * Calculates how many months the list will contain.
	 */
	private void calculateCount() {
		final CalendarDay start = mController.getStartDay();
		final CalendarDay end = mController.getEndDay();
		int startMonths = start.year * 12 + start.month;
		int endMonths = end.year * 12 + end.month;
		int months = endMonths - startMonths + 1;
		mCount = months;
	}
	
	/**
	 * Gets which month to display for 'position'
	 * @param position
	 * @return
	 */
	private int getMonthForPosition(int position) {
		int month = (position + mController.getStartDay().month) % 12;
		return month;
	}
	
	/**
	 * Gets which year to display for 'position'
	 * @param position
	 * @return
	 */
	private int getYearForPosition(int position) {
		int year = (position + mController.getStartDay().month) / 12 + mController.getStartDay().year;
		return year;
	}
	
	/**
	 * Sets which day to select.
	 * @param day
	 */
	public void setSelectedDay(CalendarDay day) {
		mSelectedDay = day;
		notifyDataSetChanged();
	}
	
	/**
	 * @param year
	 * @param month
	 * @return True if the currently selected day is the month.
	 */
	private boolean isSelectedDayInMonth(int year, int month) {
		return mSelectedDay.year == year && mSelectedDay.month == month;
	}
	
////=====================================================================================
//// OnDayClickListener
////=====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener#onDayClick(me.jmhend.ui.calendar_viewer.MonthView, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(MonthView monthView, CalendarDay day) {
		if (day != null) {
			mController.onDaySelected(day.year, day.month, day.dayOfMonth);
			setSelectedDay(day);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.MonthView.OnDayClickListener#onDayLongClick(me.jmhend.ui.calendar_viewer.MonthView, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(MonthView monthView, CalendarDay day) {
		if (day != null) {
			Toast.makeText(mContext, "Long-click: " + day.toString(), Toast.LENGTH_SHORT).show();
		}
	}

////=====================================================================================
//// CalendarDay
////=====================================================================================
	
	/**
	 * Represents a day on the Calendar.
	 * 
	 * @author jmhend
	 */
	public static class CalendarDay {
		int year;
		int month;
		int dayOfMonth;
		
		/**
		 * Constructor with all args.
		 * @param year
		 * @param month
		 * @param dayOfMonth
		 */
		public CalendarDay(int year, int month, int dayOfMonth) { 
			this.year = year;
			this.month = month;
			this.dayOfMonth = dayOfMonth;
		}
		
		/**
		 * Fill from another CalendarDay.
		 * @param day
		 */
		public void set(CalendarDay day) {
			this.year = day.year;
			this.month = day.month;
			this.dayOfMonth = day.dayOfMonth;
		}
		
		/**
		 * Set date fields.
		 * @param year
		 * @param month
		 * @param dayOfMonth
		 */
		public void set(int year, int month, int dayOfMonth) {
			this.year = year;
			this.month = month;
			this.dayOfMonth = dayOfMonth;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return (month + 1) + "/" + dayOfMonth + "/" + year;
		}
	}
}