package me.jmhend.ui.calendar_viewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay;
import me.jmhend.ui.calendar_viewer.CalendarView.OnDayClickListener;
import me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks;
import me.jmhend.ui.calendar_viewer.CalendarViewer.Mode;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Activity demonstrating usage of the CalendarViewer
 * @author jmhend
 *
 */
public class MainActivity extends FragmentActivity implements OnDayClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	
////==========================================================================================
//// Member variables.
////==========================================================================================
	
	private CalendarViewer mCalendarViewer;
	private CalendarViewerCallbacks mCallback;
	
////==========================================================================================
//// Activity lifecycle.
////==========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initBehindCalendarList();
		
		addFragment();
		setCallbacks();
		
//		// MonthListAdapter setup.
//		CalendarDay today = CalendarDay.currentDay();
		
//		MonthPagerAdapter pagerAdapter = new MonthPagerAdapter(this, builder.build());
//		mMonthPager = (CalendarViewPager) findViewById(R.id.pager);
//		mMonthPager.setAdapter(pagerAdapter);
//		mMonthPager.setOnDayClickListener(this);
//		mMonthPager.setCurrentItem(pagerAdapter.getPositionForDay(today));
//		
//		WeekPagerAdapter weekAdapter = new WeekPagerAdapter(this, builder.build());
//		mWeekPager = (CalendarViewPager) findViewById(R.id.week_pager);
//		mWeekPager.setAdapter(weekAdapter);
//		mWeekPager.setOnDayClickListener(this);
//		mWeekPager.setCurrentDay(today);
//		
//		
//		// ListView setup.
//		MonthListAdapter monthAdapter = new MonthListAdapter(this, builder.build(), this);
//		mListView = (MonthListView) findViewById(R.id.month_list);
//		mListView.setAdapter(monthAdapter);
//		mListView.postSetSelection(monthAdapter.getPositionForDay(today));

	}
	
	private void addFragment() {
		CalendarDay start = new CalendarDay(2013, Calendar.SEPTEMBER, 10);
		CalendarDay end = new CalendarDay(2014, Calendar.SEPTEMBER, 15);
		CalendarViewerConfig.Builder builder = CalendarViewerConfig.startBuilding()
				.starts(start)
				.ends(end)
				.mode(Mode.MONTH);
		
		mCalendarViewer = CalendarViewer.newInstance(null);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.calendar_viewer_container, mCalendarViewer);
		ft.commit();
	}
	
////==========================================================================================
//// Menu
////==========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.menu_closed:
	    		mCalendarViewer.setMode(Mode.CLOSED);
	    		return true;
	        case R.id.menu_week:
	        	mCalendarViewer.setMode(Mode.WEEK);
	            return true;
	        case R.id.menu_month:
	        	mCalendarViewer.setMode(Mode.MONTH);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
////==========================================================================================
//// OnDayClickListener
////==========================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.OnDayClickListener#onDayClick(android.view.View, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayClick(CalendarView calendarView, CalendarDay day) {
//		Toast.makeText(this, "Click: " + day.toString(), Toast.LENGTH_SHORT).show();
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.OnDayClickListener#onDayLongClick(android.view.View, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(CalendarView calendarView, CalendarDay day) {
//		Toast.makeText(this, "Long-click: " + day.toString(), Toast.LENGTH_SHORT).show();
	}
	
////==========================================================================================
//// BehindCalendarListAdapter
////==========================================================================================

	private void setActionBarTitle(String title) {
		getActionBar().setTitle("Monday, October 13");
	}
	
	private void setActionBarSubtitle(String title) {
		getActionBar().setSubtitle(title);
	}
	
	private void setCallbacks() {
		mCallback = new CalendarViewerCallbacks() {
			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onVisibleDaysChanged(me.jmhend.ui.calendar_viewer.CalendarViewer)
			 */
			@Override
			public void onVisibleDaysChanged(CalendarViewer viewer) {
				setActionBarSubtitle(viewer.getTitle());
			}
			
			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onDaySelected(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
			 */
			@Override
			public void onDaySelected(CalendarView view, CalendarDay day) {
				setActionBarTitle(day.toString());
			}

			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onDayLongPressed(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
			 */
			@Override
			public void onDayLongPressed(CalendarView view, CalendarDay day) {
				// TODO Auto-generated method stub
				
			}

			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onModeChanged(me.jmhend.ui.calendar_viewer.CalendarViewer, me.jmhend.ui.calendar_viewer.CalendarViewer.Mode)
			 */
			@Override
			public void onModeChanged(CalendarViewer viewer, Mode newMode) {
				setActionBarSubtitle(viewer.getTitle());
			}

			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onResized(me.jmhend.ui.calendar_viewer.CalendarViewer, int, int, int)
			 */
			@Override
			public void onResized(CalendarViewer viewer, int top, int width, int height) {
				// TODO Auto-generated method stub
			}

		};
		mCalendarViewer.setCallback(mCallback);
	}
	
	/**
	 * Sets up the ListView that sits behind the CalendarViewer.
	 * Used for testing the alpha, etc. of the CalendarViewer.
	 */
	private void initBehindCalendarList() {
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			strings.add("Marketing Team Meeting");
		}
		
		ListView listView = (ListView) findViewById(R.id.behind_list);
		listView.setClipToPadding(false);
		listView.setPadding(0, 92 * 3, 0, 0);
		listView.setAdapter(new BehindCalendarListAdapter(this, strings));
		listView.setOnItemClickListener(new OnItemClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "Title: " + mCalendarViewer.getTitle());
			}
		});
	}
	/**
	 * Adapter for ListView that sits behind the CalendarViewer.
	 * @author jmhend
	 *
	 */
	private static class BehindCalendarListAdapter extends ArrayAdapter<String> {

		private List<String> mStrings;
		
		/**
		 * @param context
		 * @param objects
		 */
		public BehindCalendarListAdapter(Context context, List<String> objects) {
			super(context, 0, objects);
			mStrings = objects;
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getCount()
		 */
		public int getCount() {
			return mStrings.size();
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem, parent, false);
			}
			
			((TextView) convertView.findViewById(R.id.text)).setText(mStrings.get(position));
			return convertView;
		}
	}
}
