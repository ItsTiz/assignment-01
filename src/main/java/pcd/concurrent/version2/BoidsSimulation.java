package pcd.concurrent.version2;

import pcd.concurrent.version2.controller.BoidsSimulator;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.view.BoidsView;

/**
 * @author Tiziano Vuksan - tiziano.vuksan@studio.unibo.it
 */

public class BoidsSimulation {
    final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1920;
    final static int ENVIRONMENT_HEIGHT = 1080;
    static final double MAX_SPEED = 4;
    static final double PERCEPTION_RADIUS = 30.0;
    static final double AVOID_RADIUS = 20.0;

    final static int SCREEN_WIDTH = 1920;
    final static int SCREEN_HEIGHT = 1080;

    public static void main(String[] args) {
        var model = new BoidsModel(
                0,
                SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
                ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
                MAX_SPEED,
                PERCEPTION_RADIUS,
                AVOID_RADIUS);
        var sim = new BoidsSimulator(model);
        var view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT);
        view.addListener(sim);
        sim.attachView(view);
    }
}
