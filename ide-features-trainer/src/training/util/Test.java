package training.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

/**
 * Created by karashevich on 30/07/15.
 */
public class Test
{
    private static boolean isBlocked = false;
    private static MouseListener ml;

    public static void main(String[] args)
    {
        JButton button = new JButton("Test");
        button.setPreferredSize(new Dimension(100, 100));

        final JButton glassButton = new JButton("Block");

        JPanel panel = new JPanel();
        panel.add(button);

        final JPanel glass = new JPanel();
        glass.setOpaque(false);
        glass.add(glassButton);

        final JFrame frame = new JFrame();
        frame.setGlassPane(glass);
        glass.setVisible(true);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);

        glassButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (isBlocked) {
                    glass.removeMouseListener(ml);
                    glassButton.setText("Block");
                } else {
                    ml = new MouseAdapter() { };
                    glass.addMouseListener(ml);
                    glassButton.setText("Unblock");
                }

                isBlocked = !isBlocked;
            }
        });
    }
}