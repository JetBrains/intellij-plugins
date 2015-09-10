package training.editor.eduUI;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;


/**
 * Created by karashevich on 30/06/15.
 */
public class ShortcutLabel extends JLabel {

    public ShortcutLabel(String text, Font font, Color fontColor, Color backgroundColor, Color borderColor) {

        super();

        this.setBorder(new FilledBorderWithText(borderColor, 4, backgroundColor, font, fontColor, text));
        this.setFont(font);
        this.setText(text);
    }

    class FilledBorderWithText extends AbstractBorder {

        private final Color color;
        private final Color colorBck;
        private final int gap;

        private Font font;
        private Color fontColor;
        private String text;

        public FilledBorderWithText(Color c, int g, @Nullable Color colorBck, Font f, Color fontColor, String s) {
            color = c;
            gap = g;
            this.colorBck = colorBck;
            this.fontColor = fontColor;
            font = f;
            text = s;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            final RoundRectangle2D.Double aDouble = new RoundRectangle2D.Double(x, y, width - 1, height - 1, gap, gap);
            g2d.draw(aDouble);
            if (colorBck != null) {
                g2d.setColor(colorBck);
                g2d.fill(aDouble);
            }
            g2d.setFont(font);
            g2d.setColor(fontColor);
            g2d.drawString(text, x + gap/2 , y + height - gap);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return (getBorderInsets(c, new Insets(gap, gap, gap, gap)));
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = gap / 2;
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

}

