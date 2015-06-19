package org.jetbrains.training.util.smalllog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by karashevich on 17/06/15.
 */
public class ClickLabel extends JLabel {


    private Type type;
    private String originalText;
    private SmallLog smallLog;
    private boolean striked;
    private boolean selected;

    private static Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    private static Color bck = new Color(54, 54 , 55, 255);
    private final Color selectedColor = new Color(34, 132, 255, 255);
    private final Color pivotColor = new Color(104, 196, 255, 255);
    private int verticalSpace = 2;


    public ClickLabel(final SmallLog smallLog, String htmlText, String originalText, Type type){
        setFont(font);
        setForeground(Color.white);

        this.originalText = originalText;
        this.type = type;
        this.setText(addHtmlTags(htmlText));
        this.smallLog = smallLog;
        striked = false;
        selected = false;

        setOpaque(true);
        setVisible(true);
        setBackground(bck);
        setBorder(new EmptyBorder(new Insets(verticalSpace, 0, verticalSpace, 0)));
    }

    public ClickLabel(final SmallLog smallLog, String text){
        setFont(font);
        setForeground(Color.white);

        originalText = text;
        this.setText(addHtmlTags(text));
        this.smallLog = smallLog;
        striked = false;
        selected = false;

        setOpaque(true);
        setVisible(true);
        setBackground(bck);
        setBorder(new EmptyBorder(new Insets(verticalSpace, 0, verticalSpace, 0)));
    }

    public Type getType(){ return type;}

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public void setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    public String takeHtmltags(String in){
        if(in.length() > 12 && in.substring(0,6).equals("<html>") && in.substring(in.length() - 7).equals("</html>"))
            return in.substring(6, in.length() - 7);
        else return in;
    }

    public boolean isSelected(){ return selected;}

    public String addHtmlTags(String in){
        return ("<html>" + in + "</html>");
    }

    public String getOriginalText() { return originalText; }

    public Color getSelectedColor() {return selectedColor;}

    public void setSelected(boolean toSelected){
        selected = toSelected;
    }

    public Color getPivotColor() {
        return pivotColor;
    }

    public boolean isStriked() {
        return striked;
    }

    public void setStriked(boolean striked) {
        this.striked = striked;
    }
}

