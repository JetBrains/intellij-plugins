package org.jetbrains.training.sandbox;


import org.jetbrains.training.graphics.DetailPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by karashevich on 04/01/15.
 */

public class TransparentPanel extends JFrame {
    DetailPanel p1;
    JButton b;

    public TransparentPanel() throws IOException, FontFormatException {
        createAndShowGUI();
    }


    private void createAndShowGUI() throws IOException, FontFormatException {
        // Set title and default close operation
        setTitle("Transparent Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set undecorated style to support background picture
        setUndecorated(true);
        setResizable(true);

        // Set some layout, say FlowLayout
        setLayout(new FlowLayout());

        // Create a JPanel
        p1 = new DetailPanel(new Dimension(500, 60));
        p1.setText("Default text");

        // Add the panels to the JFrame
        add(p1);

        // Set the size of the JFrame and
        // make it visible
        setSize(1280, 720);
        setVisible(true);
        this.setBackground(new Color(250, 250, 250, 195));

    }


    public static void main(String args[]) {
        // Run in the EDT
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new TransparentPanel();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FontFormatException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}