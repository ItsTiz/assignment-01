package pcd.concurrent.shared.view;

import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BoidsPanel extends JPanel {

    private BoidsView view;
    private BoidsModel model;
    private int framerate;
    private final Map<String, String> workerIdentifiers;

    public BoidsPanel(BoidsView view, BoidsModel model) {
        this.model = model;
        this.view = view;
        this.workerIdentifiers = new HashMap<>();
    }

    public void setFrameRate(int framerate) {
        this.framerate = framerate;
    }

    public void fillUniqueHandlers(List<String> handlers){
        for(String handler: handlers) {
            int r = new Random().nextInt(0xFFFFFF + 1);
            workerIdentifiers.put(handler, String.format("#%06X", r));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);

        var w = view.getWidth();
        var h = view.getHeight();
        var envWidth = model.getWidth();
        var envHeight = model.getHeight();
        var xScale = 1;
        var yScale = 1;

        var boids = model.getBoids();

        var cellSize = model.getPerceptionRadius();

        g.setColor(Color.LIGHT_GRAY);

        for (int x = (int) (-envWidth / 2); x <= envWidth / 2; x += cellSize) {
            int screenX = (int) (w / 2 + x * xScale);
            g.drawLine(screenX, 0, screenX, h);
        }

        for (int y = (int) (-envHeight / 2); y <= envHeight / 2; y += cellSize) {
            int screenY = (int) (h / 2 - y * yScale);
            g.drawLine(0, screenY, w, screenY);
        }

        for (Boid boid : boids) {
            String hexColor = boid.getCurrentHandler() != null ? workerIdentifiers.get(boid.getCurrentHandler())  : "#269744";
            g.setColor(Color.decode(hexColor));
            var x = boid.getPos().x();
            var y = boid.getPos().y();
            int px = (int)(w/2 + x*xScale);
            int py = (int)(h/2 - y*xScale);
            g.fillOval(px,py, 8, 8);
        }

        g.setColor(Color.BLACK);
        g.drawString("Num. Boids: " + boids.size(), 10, 25);
        g.drawString("Framerate: " + framerate, 10, 40);
    }
}