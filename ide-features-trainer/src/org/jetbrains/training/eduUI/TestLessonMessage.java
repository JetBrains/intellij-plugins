package org.jetbrains.training.eduUI;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 30/06/15.
 */
public class TestLessonMessage {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                final ShortcutLabel shortcutPanel = new ShortcutLabel("Ctrl + /", new Font(Font.SANS_SERIF, Font.BOLD, 10), Color.BLACK, Color.WHITE, Color.BLACK);
                final JLabel jl = new JLabel();
                jl.setText("<html>Comment out any line with </html>");
                jl.setBackground(Color.PINK);
                jl.repaint();

                LessonMessage lm = new LessonMessage(jl, shortcutPanel);

                frame.add(lm.getPanel());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);


            }
        });
    }

}
