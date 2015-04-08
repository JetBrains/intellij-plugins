package org.jetbrains.training.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by karashevich on 07/04/15.
 */
public class HintPanel extends JPanel implements Disposable {

    private Color backGroundColor = new Color(0, 0 ,0, 190);
    private Color textColor = new Color(150, 150, 150, 190);
    private int fontSize = 14;
    private JLabel myLabel;
    private ArrayList<JLabel> labels;

    @Nullable
    private Balloon balloon;
    private boolean balloonShown;

    public HintPanel(Dimension dimension) throws IOException, FontFormatException {

        balloonShown = false;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setOpaque(false);
        setPreferredSize(dimension);
        setSize(dimension);
        myLabel = new JLabel("");
        myLabel.setHorizontalTextPosition(JLabel.LEFT);
        myLabel.setVerticalAlignment(JLabel.NORTH);
        myLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));


        JLabel checkLabel1 = new JLabel("✓");
        checkLabel1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        checkLabel1.setVerticalAlignment(JLabel.NORTH);

        JLabel checkLabel2 = new JLabel("✓");
        checkLabel2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        checkLabel2.setVerticalAlignment(JLabel.NORTH);

        JLabel myLabel2;
        myLabel2 = new JLabel("Custom text here");
        myLabel2.setHorizontalTextPosition(JLabel.LEFT);
        myLabel2.setVerticalAlignment(JLabel.NORTH);
        myLabel2.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel myLabel3;
        myLabel3 = new JLabel("Custom text here again");
        myLabel3.setHorizontalTextPosition(JLabel.LEFT);
        myLabel3.setVerticalAlignment(JLabel.NORTH);
        myLabel3.setBorder(BorderFactory.createLineBorder(Color.BLACK));

//        custom settings
//        myLabel.setForeground(textColor);

        int heightGap = 14;
        final BorderLayout borderLayout1 = new BorderLayout();
        final BorderLayout borderLayout2 = new BorderLayout();
        borderLayout1.setHgap(10);
        borderLayout2.setHgap(10);

        JPanel line1 = new JPanel();
        line1.setLayout(borderLayout1);
        line1.add(checkLabel1, BorderLayout.WEST);
        line1.add(myLabel, BorderLayout.CENTER);

        JPanel line2 = new JPanel();
        line2.setLayout(borderLayout2);
        line2.add(checkLabel2, BorderLayout.WEST);
        line2.add(myLabel2, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        this.add(line1);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(line2);

        setFocusable(false);
        setVisible(true);
    }

    public HintPanel(String[] strings) throws IOException, FontFormatException {

        balloonShown = false;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setOpaque(false);
        Dimension dimension = new Dimension(300, 150);

        setPreferredSize(dimension);
        setSize(dimension);

        for (String string : strings) {
            JLabel label = new JLabel(string);

            labels.add();
        }


        myLabel = new JLabel("");
        myLabel.setHorizontalTextPosition(JLabel.LEFT);
        myLabel.setVerticalAlignment(JLabel.NORTH);
        myLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));


        JLabel checkLabel1 = new JLabel("✓");
        checkLabel1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        checkLabel1.setVerticalAlignment(JLabel.NORTH);

        JLabel checkLabel2 = new JLabel("✓");
        checkLabel2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        checkLabel2.setVerticalAlignment(JLabel.NORTH);

        JLabel myLabel2;
        myLabel2 = new JLabel("Custom text here");
        myLabel2.setHorizontalTextPosition(JLabel.LEFT);
        myLabel2.setVerticalAlignment(JLabel.NORTH);
        myLabel2.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel myLabel3;
        myLabel3 = new JLabel("Custom text here again");
        myLabel3.setHorizontalTextPosition(JLabel.LEFT);
        myLabel3.setVerticalAlignment(JLabel.NORTH);
        myLabel3.setBorder(BorderFactory.createLineBorder(Color.BLACK));

//        custom settings
//        myLabel.setForeground(textColor);

        int heightGap = 14;
        final BorderLayout borderLayout1 = new BorderLayout();
        final BorderLayout borderLayout2 = new BorderLayout();
        borderLayout1.setHgap(10);
        borderLayout2.setHgap(10);

        JPanel line1 = new JPanel();
        line1.setLayout(borderLayout1);
        line1.add(checkLabel1, BorderLayout.WEST);
        line1.add(myLabel, BorderLayout.CENTER);

        JPanel line2 = new JPanel();
        line2.setLayout(borderLayout2);
        line2.add(checkLabel2, BorderLayout.WEST);
        line2.add(myLabel2, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        this.add(line1);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(line2);

        setFocusable(false);
        setVisible(true);
    }

    public void show(Dimension dimension, RelativePoint location){

        if (!balloonShown) {
            balloonShown = true;

            Rectangle infoBounds = new Rectangle((int) location.getPoint().getX(), (int) location.getPoint().getY(), (int) dimension.getWidth(), (int) dimension.getHeight());

            BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createBalloonBuilder(this);
            balloonBuilder
                    .setHideOnClickOutside(false)
                    .setHideOnKeyOutside(false)
                    .setHideOnLinkClick(false)
                    .setHideOnFrameResize(false)
                    .setHideOnAction(false)
                    .setDisposable(this)
                    .setCloseButtonEnabled(true)
                    .setFillColor(new Color(230, 227, 193, 190))
//                    .setBorderColor(new Color(160, 160, 160, 255))
                    .setBorderColor(new Color(0, 0, 0, 0))
                    .setDialogMode(false)
                    .setAnimationCycle(0);

            balloon = balloonBuilder.createBalloon();
            balloon.setBounds(infoBounds);
            balloon.show(location, Balloon.Position.above);
        }
    }

    public void setText(final String text){
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                myLabel.setText(text);
            }
        });
    }


    @Override
    public void dispose() {
        setVisible(false);
    }
}
