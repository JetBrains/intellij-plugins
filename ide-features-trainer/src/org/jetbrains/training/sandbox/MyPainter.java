package org.jetbrains.training.sandbox;

import com.intellij.openapi.ui.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by karashevich on 30/01/15.
 */
public class MyPainter implements Painter {
    private final Font myFont = new JLabel().getFont();
    private String myString;
    private Component myComponent;

    public void setText(String s){
        myString = s;
//            myComponent.repaint();
    }

    public MyPainter(Component component) {
        myString = "Deafault message";
        myComponent = component;
    }

    @Override
    public void addListener(Listener listener) {

    }

    @Override
    public boolean needsRepaint() {
        return true;
    }

    @Override
    public void paint(Component component, Graphics2D graphics2D) {
        int w = 500;
        int h = 60;
        int arc = 15;

        int x = component.getWidth()/2 - w/2;
        int y = component.getHeight() - h - 30;

        graphics2D.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        graphics2D.setColor(new Color(0, 0, 0, 120));
        //graphics2D.drawLine(0, 0, component.getWidth(), component.getHeight());
        graphics2D.fillRoundRect(x, y, w, h, arc, arc);

        graphics2D.setFont(myFont);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        Rectangle2D rs = fontMetrics.getStringBounds(myString, graphics2D);

        graphics2D.setColor(new Color(255, 255, 255, 255));
        graphics2D.drawString(myString, x + (w - (int) rs.getWidth())/2, y + (h - (int) rs.getHeight())/2 + fontMetrics.getAscent());
    }

    @Override
    public void removeListener(Listener listener) {

    }
}