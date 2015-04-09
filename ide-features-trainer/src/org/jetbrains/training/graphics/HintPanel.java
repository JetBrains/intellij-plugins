package org.jetbrains.training.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.View;
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
    private ArrayList<CheckLabel> checkLabels;

    @Nullable
    private Balloon balloon;
    private boolean balloonShown;

    final private int GAP_HEIGHT = 10;
    final private int BOUNDS = 12;


    public HintPanel(String[] strings) throws IOException, FontFormatException {

        balloonShown = false;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder());

        setOpaque(false);
//        this.setBorder(BorderFactory.createEmptyBorder(BOUNDS, BOUNDS, BOUNDS, BOUNDS));

        checkLabels = new ArrayList<CheckLabel>(strings.length);
        this.add(Box.createRigidArea(new Dimension(0, BOUNDS)));
        for (String string : strings) {
            checkLabels.add(new CheckLabel(false, string));
            this.add(checkLabels.get(checkLabels.size() - 1).getContainer());
        }
//        this.add(Box.createRigidArea(new Dimension(0, BOUNDS)));

        Dimension dimension = calcDimension();
        if (dimension != null) {
            setPreferredSize(dimension);
            setSize(dimension);
        }

        setFocusable(false);
        setVisible(true);
    }

    public Dimension getDimension(){
        return this.getPreferredSize();
    }

    public void show(RelativePoint location){

        if (!balloonShown) {
            balloonShown = true;

            Rectangle infoBounds = new Rectangle((int) location.getPoint().getX(), (int) location.getPoint().getY(), (int) this.getPreferredSize().getWidth(), (int) this.getPreferredSize().getHeight());

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
                    .setAnimationCycle(0)
                    .setBorderInsets(new Insets(0,0,0,0));

            balloon = balloonBuilder.createBalloon();
            balloon.setBounds(infoBounds);
            balloon.show(location, Balloon.Position.above);
        }
    }

    private class CheckLabel{

        private boolean check;
        private String text;
        private JLabel checkLabel;
        private JLabel label;
        final private int GAP_CONST = 10;
        final private int LABEL_WIDTH = 300;
        final private int CHECK_LABEL_WIDTH = 20;
        private Dimension preferredSize;


        public CheckLabel(boolean check, String text) {

            this.check = check;
            this.text = text;
            this.label = new JLabel(text);
            preferredSize = getPreferredSize(label.getText(), true, LABEL_WIDTH);
            label.setPreferredSize(preferredSize);
            label.setMaximumSize(preferredSize);

            this.checkLabel = new JLabel("-");
            setAppearance();

        }

        private Dimension getPreferredSize(String html, boolean width, int prefSize) {
            JLabel resizer = new JLabel();

            resizer.setText(html);


            View view = (View) resizer.getClientProperty(
                    javax.swing.plaf.basic.BasicHTML.propertyKey);

            view.setSize(width?prefSize:0,width?0:prefSize);

            float w = view.getPreferredSpan(View.X_AXIS);
            float h = view.getPreferredSpan(View.Y_AXIS);

            return new java.awt.Dimension((int) Math.ceil(w),
                    (int) Math.ceil(h));
        }

        private void setAppearance(){
            if (label != null) {
//                label.setHorizontalTextPosition(JLabel.LEFT);
//                label.setVerticalAlignment(JLabel.NORTH);
//                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }
        }

        public void setCheck(boolean isDone){
           if (this.check != isDone) {
                if(isDone)
                    UIUtil.invokeLaterIfNeeded(new Runnable() {
                        @Override
                        public void run() {
                            checkLabel.setText("<html><font color=\"green\"><b>✓</b></font></html>");
                        }
                    });
                else
                    UIUtil.invokeLaterIfNeeded(new Runnable() {
                        @Override
                        public void run() {
                            checkLabel.setText("-");
                        }
                    });
               this.check = isDone;
           }
        }

        public boolean isCheck() {
            return check;
        }

        public int getGAP_CONST() {
            return GAP_CONST;
        }

        public JLabel getLabel() {
            return label;
        }

        public String getText() {
            return text;
        }

        public void setText(final String text){
            this.text = text;
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    label.setText(text);
                }
            });
        }

        public Container getContainer(){
            Container container = new Container();
            final BorderLayout borderLayout = new BorderLayout();
            borderLayout.setHgap(GAP_CONST);
            container.setLayout(borderLayout);
            container.add(checkLabel, BorderLayout.WEST);
            container.add(label, BorderLayout.CENTER);

            container.setMaximumSize(container.getPreferredSize());
            return container;
        }

        @Nullable
        public Dimension getPreferredSize(){
            int myWidth = (int) preferredSize.getWidth() + GAP_CONST + CHECK_LABEL_WIDTH;
            int myHeight = (int)preferredSize.getHeight();
            return new Dimension(myWidth, myHeight);
        }
    }

    @Nullable
    public Dimension calcDimension(){
        if (checkLabels == null) return null;
        int myWidth = 0;
        int myHeight = 0;
        int myBounds = BOUNDS;
        if (checkLabels.size() > 0) {
            for (CheckLabel checkLabel : checkLabels) {
                myWidth = Math.max((int) checkLabel.getPreferredSize().getWidth(), myWidth);
                myHeight += (int) checkLabel.getPreferredSize().getHeight() + GAP_HEIGHT;
            }
            myHeight -= GAP_HEIGHT;
        }

        myWidth += myBounds * 2;
        myHeight += myBounds * 2;

        return new Dimension(myWidth, myHeight);
    }


    public void setAllAsDone(){
        if (checkLabels != null && checkLabels.size() > 0) {
            for (CheckLabel checkLabel : checkLabels) {
                checkLabel.setCheck(true);
            }
        }
    }

    @Override
    public void dispose() {
        balloon.hide();
    }
}
