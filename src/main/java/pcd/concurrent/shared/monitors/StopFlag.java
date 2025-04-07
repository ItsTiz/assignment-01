package pcd.concurrent.shared.monitors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class StopFlag {

	private boolean flag;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition stopped = lock.newCondition();

	public StopFlag() {
		flag = false;
	}

	public void reset() {
		lock.lock();
		try {
			flag = false;
		} finally {
			lock.unlock();
		}
	}

	public void set() {
		lock.lock();
		try {
			flag = true;
			stopped.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public boolean isSet() {
		lock.lock();
		try {
			return flag;
		} finally {
			lock.unlock();
		}
	}
}
