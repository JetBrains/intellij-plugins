package org.jetbrains.training.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by karashevich on 14/01/15.
 */
public class DetailPanel extends JPanel implements Disposable{
    private final int magicConst = 15;
    private Color backGroundColor = new Color(0, 0 ,0, 190);
    private final Color textColor = new Color(245, 245, 245, 255);
    private final String customFontPath = "roboto.ttf";

    private JLabel myLabel;
    private JButton btn;

    public DetailPanel(Dimension dimension) throws IOException, FontFormatException {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setOpaque(false);
        setPreferredSize(dimension);
        setSize(dimension);
        setBackground(new Color(0, 0 ,0, 63));


        myLabel = new JLabel();
        myLabel.setForeground(textColor);
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(customFontPath));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);

        myLabel.setFont(customFont.deriveFont(14.0F));
        myLabel.setText("Default text");
        myLabel.setFocusable(false);

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
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                btn.setText(newString);
            }
        });
        showButton();
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

    public void addButtonAction(final Runnable action) throws InterruptedException {
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                    btn.removeActionListener(this);
                    action.run();
            }
        });
    }

    @Override
    public void dispose() {

    }

//    public void setVisible(boolean visibility){
//        this.set
//    }


}
