package task04;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DrawPad - Completed and Repaired.
 * Fixes:
 * 1. Correct method signature mouseClicked (with @Override) instead of typo mouseclicked.
 * 2. Persistent coordinate collection (List<Point>) rendered inside paint(Graphics g)
 *    so drawings survive minimize, restore, and OS repaint cycles.
 */
public class DrawPad extends Frame {
    private List<Point> points = new ArrayList<>();
    private Label status;

    public DrawPad() {
        setTitle("Draw Pad");
        setSize(400, 300);
        status = new Label("Clicks: 0");
        add(status, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            // TODO 1: Fix method name and store click coordinates
            @Override
            public void mouseClicked(MouseEvent e) {
                points.add(new Point(e.getX(), e.getY()));
                status.setText("Clicks: " + points.size());
                repaint();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        // TODO 2: Render all stored points
        g.setColor(Color.RED);
        for (Point p : points) {
            g.fillOval(p.x - 3, p.y - 3, 6, 6);
        }
    }

    // Accessors for automated testing
    public List<Point> getPoints() {
        return points;
    }

    public Label getStatusLabel() {
        return status;
    }

    public static void main(String[] args) {
        new DrawPad().setVisible(true);
    }
}
