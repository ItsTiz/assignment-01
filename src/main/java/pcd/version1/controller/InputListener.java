package pcd.version1.controller;

public interface InputListener {

	void started();

	void paused();

	void resumed();
	
	void stopped();

	void boidsNumberChanged(int nBoids);

	void separationWeightChanged(int sWeight);

	void alignmentWeightChanged(int hWeight);

	void cohesionWeightChanged(int cWeight);
}
