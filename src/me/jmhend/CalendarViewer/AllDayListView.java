package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 *  
 * ListView for displaying all day Events in the DayView.
 * 
 * @author jmhend
 *
 */
public class AllDayListView extends ListView {
	
	private static final String TAG = AllDayListView.class.getSimpleName();
	
////====================================================================================
//// Static constants.
////====================================================================================
	
	private static final int DEFAULT_VISIBLE_EVENTS = 2;
	private static final int MIN_VISIBLE_EVENTS = 1;
	private static final int MAX_VISIBLE_EVENTS = 10;
	
////====================================================================================
//// Member variables.
////====================================================================================
	
	private int mVisibleEvents = DEFAULT_VISIBLE_EVENTS;
	private int mRowHeight;
	
////====================================================================================
//// Constructor
////====================================================================================
	
	public AllDayListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public AllDayListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public AllDayListView(Context context) {
		super(context);
		init();
	}
	
////====================================================================================
//// Initialization
////====================================================================================
	
	/**
	 * Initalize all the stuff.
	 */
	private void init() {
		Resources r = getResources();
		mRowHeight = r.getDimensionPixelSize(R.dimen.all_day_row_height);
	}
	
////====================================================================================
//// Getters/Setters
////====================================================================================
	
	/**
	 * Sets how many Events should be on screen at once in this ListView.
	 * @param visibleEvents
	 */
	public void setVisibleEvents(int visibleEvents) {
		if (visibleEvents < MIN_VISIBLE_EVENTS) {
			visibleEvents = MIN_VISIBLE_EVENTS;
		} else if (visibleEvents > MAX_VISIBLE_EVENTS) {
			visibleEvents = MAX_VISIBLE_EVENTS;
		}
		mVisibleEvents = visibleEvents;
	}
	
////====================================================================================
//// View.
////====================================================================================
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ListView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		
		// Height = (rows_to_show_count * row_height) (dividers_count * divider_height) + vertical padding.
		// But if there are no rows, set height = 0;
		int visibleItems = getCount();
		boolean overflow = false;
		if (visibleItems > mVisibleEvents) {
			visibleItems = mVisibleEvents;
			overflow = true;
		}
		int height;
		if (visibleItems == 0) {
			height = 0;
		} else {
			float multiplier = (float) visibleItems + (overflow? 0.35f : 0f);
			height = (int) ((multiplier * mRowHeight) + (multiplier * getDividerHeight()) + getPaddingTop() + getPaddingBottom());
		}
		
		setMeasuredDimension(width, height);
	}
	
////====================================================================================
//// AllDayAdapter
////====================================================================================
	
	/**
	 * Adapter for supplying Views to AllDayListView.
	 * 
	 * @author jmhend
	 *
	 */
	public static class AllDayAdapter extends ArrayAdapter<Event> {
		
		private List<Event> mEvents;
		private LayoutInflater mInflater;
		
		
		public AllDayAdapter(Context context, List<Event> events) {
			super(context, 0, new ArrayList<Event>());
			mInflater = LayoutInflater.from(context);
			replaceContent(events);
		}
		
		/**
		 * Updates the dataset of the Adapter.
		 * @param events
		 */
		public void replaceContent(List<Event> events) {
			if (events == null) {
				events = new ArrayList<Event>();
			}
			synchronized(this) {
				clear();
				mEvents = events;
				addAll(mEvents);
			}
		}
		
		/*
		 * (non-Javadoc)
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.all_day_row, parent, false);
				holder.titleView = (TextView) convertView.findViewById(R.id.title);
				holder.locationView = (TextView) convertView.findViewById(R.id.location);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			final Event event = mEvents.get(position);
			holder.titleView.setText(event.getDrawingTitle());
			holder.locationView.setText(event.getDrawingLocation());
			
			int color = event.getDrawingColor();
			if (color == 0) {
				color = DayView.DEFAULT_COLOR;
			}
			color = DayView.setAlpha(.9f, color);
			
			convertView.setBackgroundColor(color);
			
			return convertView;
		}
		
		/**
		 * ViewHolder.
		 * @author jmhend
		 */
		private static class ViewHolder {
			private TextView titleView;
			private TextView locationView;
		}
	}

}
