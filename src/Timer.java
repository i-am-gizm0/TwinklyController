
public class Timer {
	private int now, on, off;

	public Timer(int now, int on, int off) {
		this.now = checkValue(now);
		this.on = checkValue(on);
		this.off = checkValue(off);
	}

	public int getNow() {
		return now;
	}

	public void setNow(int now) {
		this.now = checkValue(now);
	}

	public int getOn() {
		return on;
	}

	public void setOn(int on) {
		this.on = checkValue(on);
	}

	public int getOff() {
		return off;
	}

	public void setOff(int off) {
		this.off = checkValue(off);
	}

	@Override
	public String toString() {
		return now + "now " + on + "on " + off + "off";
	}
	
	private int checkValue(int a) {
		if (a < -2 || a >= 86400) {
			return -1;
		}
		return a;
	}
}
