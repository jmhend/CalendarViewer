package me.jmhend.CalendarViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;

public class ActivityDay extends Activity {
	
	private static final String TAG = ActivityDay.class.getSimpleName();
	
////==============================================================================
//// Activity lifecycle.
////==============================================================================
	
	private static List<Event> mEvents;
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day);
		
		CalendarViewPager pager = (CalendarViewPager) findViewById(R.id.day_pager);
		pager.setFadeViews(false);
		
		CalendarControllerConfig config = CalendarControllerConfig.getDefault();
		CalendarController controller = new CalendarController(config);
		
		CalModel model = new CalModel();
		DayPagerAdapter a = new DayPagerAdapter(this, model, controller);
		pager.setAdapter(a);
	}
	
	private static class CalModel implements CalendarModel {

		@Override
		public List<? extends Event> getEvents() {
			return new ArrayList<Event>();
		}

		@Override
		public List<? extends Event> getEventsOnDay(long dayStart) {
			Calendar recycle = Calendar.getInstance();
			recycle.set(Calendar.MILLISECOND, 0);
			recycle.set(Calendar.SECOND, 0);
			recycle.set(Calendar.MINUTE, 0);
			recycle.set(Calendar.HOUR_OF_DAY, 0);
			
			if (recycle.getTimeInMillis() == dayStart) {
				return ActivityDay.getEvents();
			}
			return new ArrayList<Event>();
		}

		@Override
		public int getHeat(long dayStart) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void registerObserver(CalendarModelObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unregisterObserver(CalendarModelObserver observer) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
////============================================================================
////Test
////============================================================================

	private static List<Event> getEvents() {
		if (mEvents == null) {
			mEvents = getTestEvents();
		}
		return mEvents;
	}
	private static List<Event> getTestEvents() {
		List<Event> events = new ArrayList<Event>();
		events.add(new TestEvent.Builder()
			.title("Check to make sure I'm asleep.")
			.start(1383888600000L)
			.end(1383894000000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Terrible seminar on synergy or something.")
			.location("Conference Room Soulkiller 2B")
			.lines(2)
			.start(1383832800000L)
			.end(1383865200000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Grandpa's Ballet Recital")
			.location("Florida")
			.lines(2)
			.start(1383840000000L)
			.end(1383865200000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Bored Meeting")
			.start(1383847200000L)
			.end(1383850800000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("DVR Spongebob")
			.lines(2)
			.location("Bikini Bottom")
			.start(1383847200000L)
			.end(1383854300000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Rap Battle")
			.location("Lincoln Memorial")
			.lines(2)
			.start(1383848100000L)
			.end(1383854300000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("M@dison Building Teardown")
			.location("1555 Broadway, Detroit, MI 48226")
			.lines(2)
			.start(1383937200000L)
			.end(1383938100000L)
			.build());
		events.add(new TestEvent.Builder()
			.title("Unscheduled Time")
			.start(1383854400000L)
			.end(1383863400000L)
			.build());
		return events;
	}
	
	private static class TestEvent implements Event {
		
		public long start;
		public long end;
		public String title;
		public String location;
		public boolean allDay;
		public int color;
		public int lines;
		
		public TestEvent(long start, long end) {
			this.start = start;
			this.end = end;
		}
		
		public static class Builder {
			public long start;
			public long end;
			public String title;
			public String location;
			public boolean allDay;
			public int color;
			public int lines;
			
			public Builder() {
				title = "";
				location = "";
				lines = 1;
				
				color = 0x660088CC;
				Random r = new Random();
				int x = r.nextInt();
				if (x % 2 == 0) {
					color = 0x6644AA00;
				} else if (x % 3 == 0) {
					color = 0x66FFEE66;
				}
			}
			
			public Builder start(long start) {
				this.start = start;
				return this;
			}
			
			public Builder end(long end) {
				this.end = end;
				return this;
			}
			
			public Builder title(String title) {
				this.title = title;
				return this;
			}
			
			public Builder location(String location) {
				this.location = location;
				return this;
			}
			
			public Builder allDay(boolean allDay) {
				this.allDay = allDay;
				return this;
			}
			
			public Builder color(int color) {
				this.color = color;
				return this;
			}
			
			public Builder lines(int lines) {
				this.lines = lines;
				return this;
			}
			
			public TestEvent build() {
				TestEvent e = new TestEvent(start, end);
				e.title = title;
				e.location = location;
				e.allDay = allDay;
				e.color = color;
				e.lines = lines;
				return e;
			}
			
			
		}

		@Override
		public long getDrawingStartTime() {
			return start;
		}
		@Override
		public long getDrawingEndTime() {
			return end;
		}
		@Override
		public boolean isDrawingAllDay() {
			return allDay;
		}
		@Override
		public int getDrawingColor() {
			return color;
		}
		@Override
		public int getTextLinesCount() {
			return lines;
		}

		@Override
		public String getDrawablingTitle() {
			return title;
		}

		@Override
		public String getDrawingLocation() {
			return location;
		}
	}
}
