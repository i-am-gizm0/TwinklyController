package net.gizm0.twinkly;

/**
 * A class to store timer data for Twinkly
 * All times are in seconds after midnight
 */
public class Timer {
	private int now, on, off;

	/**
	 * Create a Timer with all possible values
	 * @param now The current time
	 * @param on The time to turn on
	 * @param off The time to turn off
	 */
	public Timer(int now, int on, int off) {
		this.now = checkValue(now);
		this.on = checkValue(on);
		this.off = checkValue(off);
	}

	public int getNow() {
		return now;
	}

	/**
	 * Set the current time
	 * @param now the current time
	 */
	public void setNow(int now) {
		this.now = checkValue(now);
	}

	public int getOn() {
		return on;
	}

	/**
	 * Set the time to turn on
	 * @param on the time to turn on
	 */
	public void setOn(int on) {
		this.on = checkValue(on);
	}

	public int getOff() {
		return off;
	}

	/**
	 * The time to turn off
	 * @param off the time to turn off
	 */
	public void setOff(int off) {
		this.off = checkValue(off);
	}

	@Override
	public String toString() {
		return now + "now " + on + "on " + off + "off";
	}
	
	/**
	 * Ensure the value is reasonable (between -1 and 86400, inclusive)
	 * @param a
	 * @return
	 */
	private int checkValue(int a) {
		if (a < -1 || a >= 86400) { // -1 is treated as "disable" and 86400 is the number of seconds in a day
			return -1;
		}
		return a;
	}
}
