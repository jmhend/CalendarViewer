package me.jmhend.CalendarViewer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.jmhend.CalendarViewer.AllDayGridView.AllDayAdapter;
import me.jmhend.CalendarViewer.CalendarController.OnCalendarControllerChangeListener;
import me.jmhend.CalendarViewer.DayView.OnEventClickListener;

import org.joda.time.DateTime;
import org.joda.time.Days;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * PagerAdapter for displaying DayViews.
 * @author jmhend
 *
 */
public class DayPagerAdapter extends CalendarAdapter implements OnCalendarControllerChangeListener {

	private static final String TAG = DayPagerAdapter.class.getSimpleName();
	
////==================================================================================
//// DayTitleViewProvider
////==================================================================================
	
	/**
	 * Provides this DayPagerAdapter with the DayView title View.
	 * @author jmhend
	 *
	 */
	public static interface DayTitleViewProvider {
		
		/**
		 * @param dayStart
		 * @return Fills the View for the title of the DayView with 'dayStart'.
		 */
		public void fillDayTitle(long dayStart, TextView dateView, TextView labelView, ImageView iconView);
	}
	
////==================================================================================
//// Static constants
////==================================================================================
	
	protected static final int DATETIME_COLOR = 0xFF666666;
	protected static final int DATETIME_COLOR_FADED = 0xFFAAAAAA;
	
////==================================================================================
//// Member variables.
////==================================================================================
	
	private Context mContext;
	private LayoutInflater mInflater;
	private CalendarController mController;
	private final CalendarModel mModel;
	private DayTitleViewProvider mTitleViewProvider;
	private int mCount;
	
	private final Calendar mCalendar;
	private long mCurrentDayStart;
	
	private OnEventClickListener mEventClickListener;
	
////==================================================================================
//// Constructor
////==================================================================================
	
	/**
	 * @param context
	 * @param controller
	 */
	public DayPagerAdapter(Context context, CalendarModel model, CalendarController controller) {
		mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(mContext);
		mController = controller;
		mCalendar = Calendar.getInstance();
		mCurrentDayStart = mController.getCurrentDay().toCalendar().getTimeInMillis();
		mModel = model;
		resetCalendar();
		calculateCount();
	}
	
////==================================================================================
//// Getters/Setters
////==================================================================================
	
	/**
	 * @param l
	 */
	public void setOnEventClickListener(OnEventClickListener l) {
		mEventClickListener = l;
	}
	
	/**
	 * @param provider
	 */
	public void setDayTitleViewProvider(DayTitleViewProvider provider) {
		mTitleViewProvider = provider;
	}
	
////==================================================================================
//// CalendarAdapter
////==================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#updateView(int, me.jmhend.CalendarViewer.CalendarView)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateView(int position, View view) {
		DayView dayView = (DayView) view;
		dayView.setModel(mModel);
		dayView.setOnEventClickListener(mEventClickListener);
		
		// Necessary to identify Views in CalendarViewPager.
		Map<String, Integer> params = (Map<String, Integer>) dayView.getTag();
		if (params == null) {
			params = new HashMap<String, Integer>();
		}
		params.put(CalendarAdapter.KEY_POSITION, Integer.valueOf(position));
		dayView.setTag(params);
		
		long dayStart = getDayStartForPosition(position);
		long dayEnd = getDayEndForPosition(position);
		dayView.setDayBounds(dayStart, dayEnd);
		
		// All day View.
		updateAllDayView(position, dayView);
		
		// Day title
		if (mTitleViewProvider != null) {
			final ViewGroup titleContainer = (ViewGroup) ((View) dayView.getParent().getParent().getParent()).findViewById(R.id.day_title_container);
			final TextView dateView = (TextView) titleContainer.findViewById(R.id.day_title);
			final TextView labelView = (TextView) titleContainer.findViewById(R.id.day_title_secondary);
			final ImageView iconView = (ImageView) titleContainer.findViewById(R.id.day_title_icon);
			mTitleViewProvider.fillDayTitle(dayStart, dateView, labelView, iconView);
		}
		
		dayView.invalidate();
	}
	
	/**
	 * Updates the all day Events View part of the DayView.
	 * @param position
	 * @param dayView
	 */
	@SuppressWarnings("unchecked")
	private void updateAllDayView(int position, final DayView dayView) {
		final List<Event> allDayEvents = dayView.getAllDayEvents();
		allDayEvents.clear();
		
		// dayView.mDayStart must be set prior to this.
		long dayStart = dayView.getDayStart();
		long dayEnd = dayView.getDayEnd();
		
		// Filter out all day Events.
		List<Event> events = (List<Event>) mModel.getEventsOnDay(dayStart);
		for (Event event : events) {
			if (event.isDrawingAllDay(dayStart, dayEnd) && mModel.shouldDrawEvent(event)) {
				allDayEvents.add(event);
			}
		}
		
		dayView.setAllDayEvents(allDayEvents);
		dayView.updateEventCountView();
		
		final LinearLayout allDayView = getAllDayViewForDayView(dayView);
		AllDayGridView listView = (AllDayGridView) allDayView.findViewById(R.id.all_day_list);
		AllDayAdapter adapter = (AllDayAdapter) listView.getAdapter();
		if (adapter == null) {
			adapter = new AllDayAdapter(mContext, allDayEvents);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				/*
				 * (non-Javadoc)
				 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
				 */
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Event event = (Event) parent.getAdapter().getItem(position);
					dayView.onEventClick(event);
				}
				
			});
		} else {
			adapter.replaceContent(allDayEvents);
		}
	}
	
	/**
	 * @param dayView
	 * @return The AllDayListView of the DayView.
	 */
	private LinearLayout getAllDayViewForDayView(DayView dayView) {
		return (LinearLayout) ((View) dayView.getParent().getParent().getParent()).findViewById(R.id.all_day_list_container);
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#getPositionForDay(me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public int getPositionForDay(CalendarDay day) {
		if (day.isBeforeDay(mController.getStartDay()) || day.isAfterDay(mController.getEndDay())) {
			return -1;
		}
		DateTime dtStart = mController.getStartDay().toDateTime();
		DateTime dtDay = day.toDateTime();
		int position = Days.daysBetween(dtStart, dtDay).getDays();
		return position;
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#setSelectedDay(me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void setSelectedDay(CalendarDay day) {
		mController.setSelectedDay(day);
		updateViewPager();
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.RecyclingPagerAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		DayView dayView;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.day_view, container, false);
		} 
		dayView = ((DayView) convertView.findViewById(R.id.day));
		updateView(position, dayView);
		convertView.findViewById(R.id.all_day_list).requestLayout();
		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.CalendarViewer.CalendarAdapter#getFocusedDay(int)
	 */
	@Override
	public CalendarDay getFocusedDay(int position) {
		resetCalendar();
		mCalendar.add(Calendar.DAY_OF_YEAR, position);
		return CalendarDay.fromCalendar(mCalendar);
	}
	
////==================================================================================
//// Position
////==================================================================================
	
	/**
	 * Calculates how many days are supplied by this DayPagerAdapter.
	 */
	private void calculateCount() {
		DateTime dtStart = mController.getStartDay().toDateTime();
		DateTime dtEnd = mController.getEndDay().toDateTime();
		int numDays = Days.daysBetween(dtStart, dtEnd).getDays();
		mCount = numDays + 1;
	}
	
	/**
	 * @param position
	 * @return The day start time at position.
	 */
	public long getDayStartForPosition(int position) {
		resetCalendar();
		mCalendar.add(Calendar.DAY_OF_YEAR, position);
		return mCalendar.getTimeInMillis();
	}
	
	/**
	 * @param position
	 * @return The day end time at position;
	 */
	public long getDayEndForPosition(int position) {
		resetCalendar();
		mCalendar.add(Calendar.DAY_OF_YEAR, position);
		mCalendar.set(Calendar.SECOND, 59);
		mCalendar.set(Calendar.MINUTE, 59);
		mCalendar.set(Calendar.HOUR_OF_DAY, 23);
		return mCalendar.getTimeInMillis();
	}
	
	/**
	 * Resets mCalendar to be the starting day.
	 */
	private void resetCalendar() {
		mController.getStartDay().fillCalendar(mCalendar);
	}
	
	
////====================================================================================
//// OnCalendarControllerChangeListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarController.OnCalendarControllerChangeListener#onChange(me.jmhend.ui.calendar_viewer.CalendarController, java.lang.Object, java.lang.String)
	 */
	@Override
	public void onChange(CalendarController controller, Object obj, String tag) {
		if (CalendarController.FIRST_DAY_OF_WEEK.equals(tag)
				|| CalendarController.START_DAY.equals(tag)
				|| CalendarController.END_DAY.equals(tag)) {
			calculateCount();
		}
		if (CalendarController.SELECTED_DAY.equals(tag)) {
			CalendarDay selectedDay = (CalendarDay) obj;
			if (selectedDay != null) {
				getViewPager().setCurrentDay(selectedDay);
			}
		}
		if (CalendarController.CURRENT_DAY.equals(tag)) {
			mCurrentDayStart = mController.getCurrentDay().toCalendar().getTimeInMillis();
		}
		updateViewPager();
	}
}





