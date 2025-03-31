package pcd.version1.monitors;

public class PauseFlag {

	private boolean flag;
	
	public PauseFlag() {
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
