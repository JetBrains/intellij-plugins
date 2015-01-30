package org.jetbrains.training.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by karashevich on 04/01/15.
 */

public class TransparentPanel extends JFrame
    {
        DetailPanel p1;

        public TransparentPanel()
        {
            createAndShowGUI();
        }



        private void createAndShowGUI()
        {
            // Set title and default close operation
            setTitle("Transparent Panel");
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            // Set undecorated style to support background picture
            setUndecorated(true);
            setResizable(true);

            // Set some layout, say FlowLayout
            setLayout(new FlowLayout());

            // Create a JPanel
            p1=new DetailPanel(new Dimension(500,60));

            //Label settings
            JLabel myLabel = new JLabel();
            myLabel.setForeground(new Color(245, 245, 245, 255));
            Font font = myLabel.getFont();
            Font newFont = new Font(font.getName(), font.getStyle(), 18);
            myLabel.setFont(newFont);

            p1.setLayout(new GridBagLayout());
            p1.add(myLabel, new GridBagConstraints());
            p1.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    p1.setVisible(false);
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });

            myLabel.setText("Welcome to IntelliJ IDEA");


            // Add the panels to the JFrame
            add(p1);

            // Set the size of the JFrame and
            // make it visible
            setSize(700,400);
            setVisible(true);

            this.setBackground(new Color(250, 250, 250, 195));

        }


        public static void main(String args[]) {
            // Run in the EDT
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new TransparentPanel();
                }
            });
        }
}
