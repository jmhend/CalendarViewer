package me.jmhend.ui.calendar_viewer;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener;
import me.jmhend.ui.calendar_viewer.CalendarViewPager.OnPageSelectedListener;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author jmhend
 */
public class CalendarViewer extends Fragment implements OnPageSelectedListener, OnDayClickListener {
	
	private static final String TAG = CalendarViewer.class.getSimpleName();
	
////====================================================================================
//// CalendarViewerCallbacks
////====================================================================================
	
	/**
	 * Callback methods the CalendarViewer will invoke.
	 * @author jmhend
	 */
	public static interface CalendarViewerCallbacks {
		
		public void onVisibleDaysChanged(CalendarViewer viewer);
		
		public void onDaySelected(CalendarView view, CalendarDay day);
		
		public void onDayLongPressed(CalendarView view, CalendarDay day);
		
		public void onModeChanged(CalendarViewer viewer, Mode newMode);
		
		public void onResized(CalendarViewer viewer, int top, int width, int height);
	}
	
////====================================================================================
//// Mode
////====================================================================================
	
	/**
	 * Describes which mode the CalendarViewer is in.
	 * @author jmhend
	 */
	public static enum Mode {
		CLOSED(0),
		WEEK(1),
		MONTH(2);
		
		private int mNum;
		
		private Mode(int num) {
			mNum = num;
		}
		
		public int intValue() {
			return mNum;
		}
		
		public static Mode ofValue(int num) {
			switch (num) {
			case 0: return CLOSED;
			case 1: return WEEK;
			case 2: return MONTH;
			default: return CLOSED;
			}
		}
	}
	
////====================================================================================
//// Static constants.
////====================================================================================
	
	private static final String EXTRA_CONFIG = "config";
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private CalendarViewerCallbacks mCallback;
	private CalendarViewPager mCurrentPager;
	private CalendarViewPager mWeekPager;
	private CalendarViewPager mMonthPager;
	private CalendarAdapter mMonthAdapter;
	private CalendarAdapter mWeekAdapter;
	private Mode mMode;
	
	private int mFirstDayOfWeek;
	private CalendarDay mStartDay;
	private CalendarDay mEndDay;
	private CalendarDay mSelectedDay;
	
	private CalendarViewerConfig mConfig;
	
////====================================================================================
//// Constructor/Instantiation
////====================================================================================
	
	/**
	 * @return A newly instantiated CalendarViewer.
	 */
	public static CalendarViewer newInstance(CalendarViewerConfig config) {
		Bundle args = new Bundle();
		if (config == null) {
			config = CalendarViewerConfig.getDefault();
		}
		args.putParcelable(EXTRA_CONFIG, config);
		
		CalendarViewer f = new CalendarViewer();
		f.setArguments(args);
		return f;
	}
	
	/**
	 * Empty constructor.
	 */
	public CalendarViewer() { }
	
////====================================================================================
//// Fragment lifecycle.
////====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() == null) {
			throw new IllegalStateException("CalendarViewer must be intialized with a CalendarViewerConfig.");
		}
		
		mConfig = getArguments().getParcelable(EXTRA_CONFIG);
		mFirstDayOfWeek = mConfig.getFirstDayOfWeek();
		mStartDay = mConfig.getStartDay();
		mEndDay = mConfig.getEndDay();
		mSelectedDay = mConfig.getSelectedDay();
		mMode = mConfig.getMode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.calendar_viewer, container, false);
		
		mWeekAdapter = new WeekPagerAdapter(getActivity(), mConfig);
		mMonthAdapter = new MonthPagerAdapter(getActivity(), mConfig);
		mWeekPager = (CalendarViewPager) layout.findViewById(R.id.week_pager);
		mWeekPager.setAdapter(mWeekAdapter);
		mWeekPager.setOnPageSelectedListener(this);
		mWeekPager.setOnDayClickListener(this);
		mMonthPager = (CalendarViewPager) layout.findViewById(R.id.month_pager);
		mMonthPager.setAdapter(mMonthAdapter);
		mMonthPager.setOnPageSelectedListener(this);
		mMonthPager.setOnDayClickListener(this);
		
		transitionMode(null, mMode);
		
		return layout;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
////====================================================================================
//// Getters/Setters
////====================================================================================
	
	/**
	 * @return The title of the currently displayed View.
	 */
	public String getTitle() {
		if (mCurrentPager != null) {
			return mCurrentPager.getCurrentItemTitle();
		}
		return "";
	}
	
	/**
	 * @param callback
	 */
	public void setCallback(CalendarViewerCallbacks callback) {
		mCallback = callback;
	}

	/**
	 * Sets the display Mode of the CalendarViewer.
	 * @param mode
	 */
	public void setMode(Mode mode) {
		transitionMode(mMode, mode);
		mMode = mode;
		
		if (mCallback != null) {
			mCallback.onModeChanged(this, mMode);
		}
	}
	
////====================================================================================
//// State
////====================================================================================
	
	/**
	 * Transitions the CalendarViewer from Mode to Mode.
	 * @param from
	 * @param to
	 */
	private void transitionMode(Mode from, Mode to) {
		if (from == to) {
			return;
		}
		switch (to) {
		case CLOSED:
			mWeekPager.setVisibility(View.GONE);
			mMonthPager.setVisibility(View.GONE);
			mCurrentPager = null;
			break;
		case WEEK:
			mWeekPager.setVisibility(View.VISIBLE);
			mMonthPager.setVisibility(View.GONE);
			mCurrentPager = mWeekPager;
			break;
		case MONTH:
			mWeekPager.setVisibility(View.GONE);
			mMonthPager.setVisibility(View.VISIBLE);
			mCurrentPager = mMonthPager;
			break;
		}
	}
	
////====================================================================================
//// OnPageSelectedListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarViewPager.OnPageSelectedListener#onPageSelected(android.support.v4.view.ViewPager, int)
	 */
	@Override
	public void onPageSelected(ViewPager pager, int position) {
		if (mCallback != null) {
			mCallback.onVisibleDaysChanged(this);
		}
	}
	
////====================================================================================
//// OnDayClickListener
////====================================================================================

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener#
	 * onDayClick(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(CalendarView calendarView, CalendarDay day) {
		if (mCallback != null) {
			mCallback.onDaySelected(calendarView, day);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener#
	 * onDayLongClick(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(CalendarView calendarView, CalendarDay day) {
		if (mCallback != null) {
			mCallback.onDayLongPressed(calendarView, day);
		}	
	}
		
	
	
}
