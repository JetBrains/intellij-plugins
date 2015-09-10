package training.graphics;

import training.util.MyClassLoader;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.IOException;

/**
 * Created by karashevich on 04/01/15.
 */

public class TransparentPanel extends JFrame
    {
        DetailPanel p1;

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

            Image image = MyClassLoader.getInstance().getImageResourceAsStream("checkmark.png");  //this generates an image file
            ImageIcon checkmarkIcon = new ImageIcon(image);


            // Create a JPanel
            JPanel p1 = new JPanel();
            JLabel jLabel = new JLabel("Test JLabel");
            jLabel.setIcon(checkmarkIcon);
            jLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            jLabel.setBorder(new LineBorder(Color.BLACK, 1));
            p1.add(jLabel);

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
