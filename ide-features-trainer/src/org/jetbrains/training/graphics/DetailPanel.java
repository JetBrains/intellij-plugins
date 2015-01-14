package org.jetbrains.training.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 14/01/15.
 */
public class DetailPanel extends JPanel {


    private final int magicConst = 10;
    private final Color backGroundColor = new Color(0, 0 ,0, 180);
    private final Color textColor = new Color(245, 245, 245, 255);
    private JLabel myLabel;

    public DetailPanel() {
        setOpaque(false);
    }

    public DetailPanel(Dimension dimension){

        setOpaque(false);
        setPreferredSize(dimension);
        setSize(dimension);
        setBackground(backGroundColor);

        myLabel = new JLabel();
        myLabel.setForeground(textColor);
        Font font = myLabel.getFont();
        Font newFont = new Font(font.getName(), font.getStyle(), 18);
        myLabel.setFont(newFont);

        this.add(myLabel);
    }

    public void setText(String s){
        myLabel.setText(s);
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        int arc = magicConst;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(backGroundColor);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setStroke(new BasicStroke(0f));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.dispose();
    }
}
