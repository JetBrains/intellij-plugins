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

/**
 * Created by karashevich on 07/04/15.
 */
public class HintPanel extends JPanel implements Disposable {

    private Color backGroundColor = new Color(0, 0 ,0, 190);
    private Color textColor = new Color(150, 150, 150, 190);
    private JLabel myLabel;

    @Nullable
    private Balloon balloon;
    private boolean balloonShown;

    public HintPanel(Dimension dimension) throws IOException, FontFormatException {

        balloonShown = false;

        setOpaque(false);
        setPreferredSize(dimension);
        setSize(dimension);
        myLabel = new JLabel("");
//        custom settings
//        myLabel.setForeground(textColor);

        this.add(myLabel);

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
                    .setBorderColor(new Color(160, 160, 160, 255))
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
