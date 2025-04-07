package pcd.concurrent.shared.monitors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PauseFlag {
	private boolean flag;
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition unPaused = lock.newCondition();

	public PauseFlag() {
		flag = false;
	}

	public void reset() {
		lock.lock();
		try {
			flag = false;
			unPaused.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public void set() {
		lock.lock();
		try {
			flag = true;
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

	public void awaitUnpause() throws InterruptedException {
		lock.lock();
		try {
			while (flag) {
				unPaused.await();
			}
		} finally {
			lock.unlock();
		}
	}
}
