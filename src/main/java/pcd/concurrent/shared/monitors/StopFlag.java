package pcd.concurrent.shared.monitors;

public class StopFlag {

	private boolean flag;
	
	public StopFlag() {
		flag = false;
	}
	
	public synchronized void reset() {
		flag = false;
	}
	
	public synchronized void set() {
		flag = true;
	}
	
	public synchronized boolean isSet() {
		return flag;
	}
}
