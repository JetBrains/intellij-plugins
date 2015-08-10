package org.jetbrains.training.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Created by karashevich on 10/08/15.
 */
public class MiniCloseButton extends JComponent {


    /**
     * @param bounds use 12 pixelse for height and width
     */
    public MiniCloseButton(Rectangle bounds) {
        this.setBounds(bounds);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                setForeground(Color.GRAY);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                setForeground(Color.BLACK);
                repaint();
            }

        });
    }

    public void setTrackingComponent(JComponent component){
        component.getBounds();

        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
            }

            @Override
            public void componentMoved(ComponentEvent componentEvent) {
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        int x0 = 0;
        int y0 = 0;
        int x1 = getBounds().width - 1;
        int y1 = getBounds().height - 1;
        final int shift = 3;

        Graphics2D g2d = (Graphics2D) graphics;

        g2d.setStroke(new BasicStroke(1.0f));

        //cross
        Path2D.Float path = new Path2D.Float();
        g2d.setColor(getForeground());

        // end of arrow is pinched in
        path.moveTo(shift, shift);
        path.lineTo(x1 - shift, y1 - shift);
        path.moveTo(x0 + shift, y1 - shift);
        path.lineTo(x1 - shift, y0 + shift);
        Ellipse2D.Double circle = new Ellipse2D.Double(x0, y0, x1, y1);

        g2d.draw(circle);
        g2d.fill(path);
        g2d.draw(path);
    }

    public void setClickAction(final Runnable runnable) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                runnable.run();
            }
        });
    }
}
