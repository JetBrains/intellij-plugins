package org.jetbrains.training.graphics;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by karashevich on 14/01/15.
 */
public class DetailPanel extends JPanel {
    private final int magicConst = 15;
    private final Color backGroundColor = new Color(0, 0 ,0, 63);
    private final Color textColor = new Color(245, 245, 245, 255);
    private JLabel myLabel;
    private JButton btn;

    public DetailPanel( Dimension dimension){

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setOpaque(false);
        setPreferredSize(dimension);
        setSize(dimension);
        setBackground(new Color(0, 0 ,0, 63));

        myLabel = new JLabel();
        myLabel.setForeground(textColor);
        Font font = myLabel.getFont();
        Font newFont = new Font(font.getName(), font.getStyle(), 12);
        myLabel.setFont(newFont);
        myLabel.setText("Default text");
        myLabel.setFocusable(false);

        btn = new JButton("Start");
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);

        //btn.setForeground(Color.white);
        //btn.setBackground(new Color(0,0,0,190));

        Box vertBox = Box.createVerticalBox();
        Box lineBox = Box.createHorizontalBox();
//
        lineBox.add(Box.createHorizontalStrut(magicConst));
        lineBox.add(myLabel);
        lineBox.add(Box.createHorizontalGlue());
        lineBox.add(btn);
        lineBox.add(Box.createHorizontalStrut(magicConst));
//
        vertBox.add(Box.createVerticalGlue());
        vertBox.add(lineBox);
        vertBox.add(Box.createVerticalGlue());

        setFocusable(false);

        add(vertBox);
        setVisible(true);

        //this.add(myLabel);
        //this.add(btn);

    }

    public void setText(String s){
        final String newString = s;

        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                myLabel.setText(newString);
            }
        });
    }

    public void showButton(){
        if (btn.isVisible()) {
            //do nothing
        } else {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    btn.setVisible(true);
                }
            });
        }
    }

    public void hideButton(){
        if (btn.isVisible()) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    btn.setVisible(false);
                }
            });
        } else {
            //do nothing
        }
    }

    public void setButtonText(String s){
        final String newString = s;
        final int myPanelWidth = this.getWidth();
        final int myPanelHeight = this.getHeight();

        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                btn.setText(newString);
            }
        });

        showButton();
    }

    public void addWaitToButton(final Editor lockEditor){
        btn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                synchronized (lockEditor){
                    lockEditor.notifyAll();
                }
            }
        });
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

        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setStroke(new BasicStroke(0f));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.dispose();
    }
}
