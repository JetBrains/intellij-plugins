import javax.swing.*;
import java.awt.*;

public class Test{
    private static JButton glassButton;
    private static JPanel glass;

    public static void main(String[] args) {

        final Dimension myDimension = new Dimension(100, 100);

        JButton button = new JButton("Test");
        button.setPreferredSize(myDimension);

        glassButton = new JButton("Block");
        JPanel panel = new JPanel();
        panel.add(button);

        glass = new JPanel();
        glass.setOpaque(false);
        glass.add(glassButton);

        JFrame frame = new JFrame();
        frame.setGlassPane(glass);
        glass.setVisible(true);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }
}