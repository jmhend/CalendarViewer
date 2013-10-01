package me.jmhend.ui.calendar_viewer;

/**
 * Utility methods.
 * 
 * @author jmhend
 *
 */
public class Utils {
	
	private static final String TAG = Utils.class.getSimpleName();
	
////===============================================================================
////
////===============================================================================
	
	private Utils() { }
	
	/**
	 * Gets how many days are in the month.
	 * @param month
	 * @param year
	 * @return
	 */
	public static int getDaysInMonth(int month, int year) {
		switch(month) {
			case 0:
			case 2:
			case 4:
			case 6:
			case 7:
			case 9:
			case 11:
				return 31;
			case 3:
			case 5:
			case 8:
			case 10:
				return 30;
			case 1:
				if(year % 4 == 0)
					return 29;
				return 28;
			default:
				throw new IllegalArgumentException("Invalid Month");
		}
	}

}
