package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.jmhend.CalendarViewer.ActivityDay.CalModel;
import me.jmhend.CalendarViewer.CalendarAdapter.CalendarDay;
import me.jmhend.CalendarViewer.CalendarView.OnDayClickListener;
import me.jmhend.CalendarViewer.CalendarViewer.CalendarViewerCallbacks;
import me.jmhend.CalendarViewer.CalendarViewer.Mode;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
	private ListView mBehindList;
	
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
		
		addCalendarViewer();
		setCallbacks();
	}
	
	private void addCalendarViewer() {
		CalendarDay start = new CalendarDay(2011, Calendar.SEPTEMBER, 10);
		CalendarDay end = new CalendarDay(2026, Calendar.OCTOBER, 31);
		CalendarControllerConfig.Builder builder = CalendarControllerConfig.startBuilding()
				.starts(start)
				.ends(end)
				.mode(Mode.MONTH);
		
		ViewGroup container = (ViewGroup) findViewById(R.id.calendar_viewer_container);
		
		mCalendarViewer = new CalendarViewer(this, container, new CalModel(), builder.build());
		mCalendarViewer.getView().findViewById(R.id.week_month_container).setBackgroundColor(0xEECC8800);
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
	    int itemId = item.getItemId();
		if (itemId == R.id.menu_closed) {
			mCalendarViewer.transitionMode(Mode.CLOSED);
			return true;
		} else if (itemId == R.id.menu_week) {
			mCalendarViewer.transitionMode(Mode.WEEK);
			return true;
		} else if (itemId == R.id.menu_month) {
			mCalendarViewer.transitionMode(Mode.MONTH);
			return true;
		} else {
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
	public void onDayClick(View calendarView, CalendarDay day) {
//		Toast.makeText(this, "Click: " + day.toString(), Toast.LENGTH_SHORT).show();
	}

	/*
	 * (non-Javadoc)
	 * @see me.jmhend.ui.calendar_viewer.OnDayClickListener#onDayLongClick(android.view.View, me.jmhend.ui.calendar_viewer.MonthListAdapter.CalendarDay)
	 */
	@Override
	public void onDayLongClick(View calendarView, CalendarDay day) {
//		Toast.makeText(this, "Long-click: " + day.toString(), Toast.LENGTH_SHORT).show();
	}
	
////==========================================================================================
//// BehindCalendarListAdapter
////==========================================================================================

	private void setActionBarTitle(String title) {
		getActionBar().setTitle(title);
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
			public void onDaySelected(View view, CalendarDay day) {
				String title = "Title";
				setActionBarTitle(title);
			}

			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onDayLongPressed(me.jmhend.ui.calendar_viewer.CalendarView, me.jmhend.ui.calendar_viewer.CalendarAdapter.CalendarDay)
			 */
			@Override
			public void onDayLongPressed(View view, CalendarDay day) {
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
			 * @see me.jmhend.CalendarViewer.CalendarViewer.CalendarViewerCallbacks#onEventClick(android.view.View, me.jmhend.CalendarViewer.Event)
			 */
			@Override
			public void onEventClick(View view, Event event) {
			}

			/*
			 * (non-Javadoc)
			 * @see me.jmhend.ui.calendar_viewer.CalendarViewer.CalendarViewerCallbacks#
			 * onResized(me.jmhend.ui.calendar_viewer.CalendarViewer, int, int, int)
			 */
			@Override
			public void onResized(CalendarViewer viewer, int top, int width, int height) {
				mBehindList.setPadding(0, top + height, 0, 0);
			}

			

		};
		mCalendarViewer.setCallback(mCallback);
	}
	
	int lol = 1000;
	
	/**
	 * Sets up the ListView that sits behind the CalendarViewer.
	 * Used for testing the alpha, etc. of the CalendarViewer.
	 */
	private void initBehindCalendarList() {
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			strings.add("Marketing Team Meeting");
		}
		
		mBehindList = (ListView) findViewById(R.id.behind_list);
		mBehindList.setClipToPadding(false);
		mBehindList.setPadding(0, 92 * 3, 0, 0);
		mBehindList.setAdapter(new BehindCalendarListAdapter(this, strings));
		mBehindList.setOnItemClickListener(new OnItemClickListener() {
			/*
			 * (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});
	}
	
	private float p = 1f;
	
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
