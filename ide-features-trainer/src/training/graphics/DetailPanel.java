package training.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by karashevich on 14/01/15.
 */
public class DetailPanel extends JPanel implements Disposable{
    private final int magicConst = 15;
    private final int buttonGap = 7;

    private Color backGroundColor = new Color(0, 0 ,0, 190);
    private Color textColor = new Color(245, 245, 245, 255);
    private final String customFontPath = "roboto.ttf";
    @Nullable
    private Balloon balloon;
    private boolean balloonShown;

    private JLabel myLabel;
    private JButton btn;
    private JButton replayButton;

    public DetailPanel(Dimension dimension) throws IOException, FontFormatException {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        balloonShown = false;

        if( UIUtil.isUnderDarcula()) {
            backGroundColor = new Color(120, 120, 120, 63);
            textColor = new Color(15, 15, 15, 255);
        }

        setOpaque(false);
        setPreferredSize(dimension);
        setSize(dimension);
        if( UIUtil.isUnderDarcula()) {
            setBackground(new Color(120, 120 ,120, 63));
        } else {
            setBackground(new Color(0, 0 ,0, 63));
        }

        if( UIUtil.isUnderDarcula()) {
            textColor = new Color(235, 235, 235);
        }
        myLabel = new JLabel();
        myLabel.setForeground(textColor);
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(customFontPath));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);

        myLabel.setFont(customFont.deriveFont(14.0F));
        myLabel.setText("");
        myLabel.setFocusable(false);

        btn = new RoundedCornerButton("");
        btn.setUI(new RoundedCornerButtonUI());
        btn.setFont(customFont.deriveFont(13.0F));
        btn.setBorderPainted(false);
        if( UIUtil.isUnderDarcula()) {
            btn.setForeground(Color.WHITE);
        } else {
            btn.setForeground(Color.WHITE);
        }
        btn.setRolloverEnabled(false);
        btn.setFocusable(false);
        btn.setBackground((new Color(0,0,0,0)));

        btn.setContentAreaFilled(false);

        replayButton = new RoundedCornerButton("Show again");
        replayButton.setUI(new RoundedCornerButtonUI());
        replayButton.setFont(customFont.deriveFont(13.0F));
        replayButton.setBorderPainted(false);
        if( UIUtil.isUnderDarcula()) {
            replayButton.setForeground(Color.WHITE);
        } else {
            replayButton.setForeground(Color.WHITE);
        }
        replayButton.setRolloverEnabled(false);
        replayButton.setFocusable(false);
        replayButton.setBackground((new Color(0, 0, 0, 0)));
        replayButton.setContentAreaFilled(false);
        replayButton.setVisible(false);


        Box vertBox = Box.createVerticalBox();
        Box lineBox = Box.createHorizontalBox();
//
        lineBox.add(Box.createHorizontalStrut(magicConst));
        lineBox.add(myLabel);
        lineBox.add(Box.createHorizontalGlue());
        lineBox.add(replayButton);
        lineBox.add(Box.createHorizontalStrut(buttonGap));
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
        if(UIUtil.isUnderDarcula()) {
            backGroundColor = new Color(111, 176, 111, 190);
        } else {
            backGroundColor = new Color(58, 126, 58, 210);
        }
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


    public void addButtonAction(final Runnable action) throws InterruptedException {
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btn.removeActionListener(this);
                action.run();
            }
        });
    }

    public void removeButtonActions(){
        final ActionListener[] actionListeners = btn.getActionListeners();
        for (int i = 0; i < actionListeners.length; i++) {
            btn.removeActionListener(actionListeners[i]);
        }
    }

    public void showReplayButton(){
        if (replayButton.isVisible()) {
            //do nothing
        } else {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    replayButton.setVisible(true);
                }
            });
        }
    }

    public void hideReplayButton(){
        if (replayButton.isVisible()) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run() {
                    replayButton.setVisible(false);
                }
            });
        } else {
            //do nothing
        }
    }

    public void setReplayButton(String s){
        final String newString = s;
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                replayButton.setText(newString);
            }
        });
        showButton();
    }

    public void addReplayButtonAction(final Runnable action) throws InterruptedException {
        replayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                replayButton.removeActionListener(this);
                action.run();
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

    @Override
    public void dispose() {
//        System.err.println("disposed");
        setVisible(false);
    }

//    public void setVisible(boolean visibility){
//        this.set
//    }


    public void showBalloon(Dimension dimension, RelativePoint location){

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
                    .setCloseButtonEnabled(false)
                    .setBorderColor(new Color(0, 0, 0, 0))
                    .setDialogMode(false)
                    .setFillColor(new Color(0, 0, 0, 0))
                    .setAnimationCycle(0);

            balloon = balloonBuilder.createBalloon();
            balloon.setBounds(infoBounds);
            balloon.show(location, Balloon.Position.above);
        }
    }

    public void hideBalloon(){

        if(balloon != null){
            balloonShown = false;
            balloon.hide();
        }
    }
}
