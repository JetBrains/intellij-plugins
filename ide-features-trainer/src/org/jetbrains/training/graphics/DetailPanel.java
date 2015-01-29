package org.jetbrains.training.graphics;

import com.intellij.openapi.editor.Editor;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by karashevich on 14/01/15.
 */
public class DetailPanel extends JPanel {
    private final int magicConst = 15;
    private Color backGroundColor = new Color(0, 0 ,0, 190);
    private final Color textColor = new Color(245, 245, 245, 255);
    private final int inset = 4;

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
        Font newFont = new Font(font.getName(), font.getStyle(), 14);
        myLabel.setFont(newFont);
        myLabel.setText("Default text");
        myLabel.setFocusable(false);


        final Color passiveColor = new Color(96, 96, 96, 255);
        final Color activeColor = new Color(128, 128, 128, 255);
        final Color pressedColor = new Color(176,176,176,255);


        final JButton button = new JButton("flat button");
        button.setForeground(Color.WHITE);
        Border line = new LineBorder(new Color(0,0,0,0));
//        Border margin = new EmptyBorder(5, 15, 5, 15);
//        Border compound = new CompoundBorder(line, margin);
//        button.setBorder(compound);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(passiveColor);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                button.setBackground(activeColor);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                button.setBackground(passiveColor);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                button.setBackground(pressedColor);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                button.setBackground(activeColor);
            }
        });

        btn = new RoundedCornerButton("Test button");
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setRolloverEnabled(false);
        btn.setFocusable(false);


//        btn = new JButton("Start");
//        btn.setFocusPainted(false);
//        btn.setContentAreaFilled(false);
//        btn.setFocusable(false);
//        btn.setMargin(new Insets(inset,inset,inset,inset ));

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

    //make backgreound color as green
    public void greenalize(){
        backGroundColor = new Color(0, 58, 0, 190);
        setBackground(backGroundColor);
//        this.paintComponent(this.getGraphics());
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

        g2.setColor(backGroundColor);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setStroke(new BasicStroke(0f));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.dispose();
    }


}
