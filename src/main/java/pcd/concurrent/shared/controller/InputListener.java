package pcd.concurrent.shared.controller;

/**
 * Interface for handling user input events related to the simulation's control flow
 * and parameter adjustments. Implementations of this interface should define the behavior
 * triggered by user interactions such as starting, pausing, or modifying simulation parameters.
 *
 * This listener is typically used by the controller component in an MVC architecture
 * to notify the simulation engine of UI actions.
 *
 * @author Tiziano Vuksan
 * @email tiziano.vuksan@studio.unibo.it
 */
public interface InputListener {

	/**
	 * Called when the simulation is started.
	 */
	void started();

	/**
	 * Called when the simulation is paused.
	 */
	void paused();

	/**
	 * Called when the simulation is resumed after being paused.
	 */
	void resumed();

	/**
	 * Called when the simulation is stopped.
	 */
	void stopped();

	/**
	 * Called when the number of boids is changed.
	 *
	 * @param nBoids the new number of boids
	 */
	void boidsNumberChanged(int nBoids);

	/**
	 * Called when the separation weight parameter is changed.
	 *
	 * @param sWeight the new separation weight value
	 */
	void separationWeightChanged(int sWeight);

	/**
	 * Called when the alignment weight parameter is changed.
	 *
	 * @param hWeight the new alignment weight value
	 */
	void alignmentWeightChanged(int hWeight);

	/**
	 * Called when the cohesion weight parameter is changed.
	 *
	 * @param cWeight the new cohesion weight value
	 */
	void cohesionWeightChanged(int cWeight);
}
