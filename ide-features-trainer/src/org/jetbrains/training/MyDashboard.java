package org.jetbrains.training;

import apple.awt.CColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by karashevich on 30/12/14.
 */
public class MyDashboard extends JFrame{
    private JTextField myTextField;
    private JButton okButton;
    private JPanel myPanel;
    private JButton clearButton;

    public MyDashboard() {
        super("MyDashboard");

        setContentPane(myPanel);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String s = myTextField.getText();
                myTextField.setText(s + " Button pushed!");

            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                myTextField.setText("");
            }
        });
         setVisible(true);
    }

}
