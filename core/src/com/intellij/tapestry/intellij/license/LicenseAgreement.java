package com.intellij.tapestry.intellij.license;

import javax.swing.*;
import java.awt.event.*;

public class LicenseAgreement extends JDialog {

    private JPanel _mainPanel;
    private JButton _buttonOK;
    private JButton _buttonCancel;
    private JCheckBox _isAgree;
    private JTextArea _licenseAgreement;
    private boolean _ok;

    public LicenseAgreement() {
        setContentPane(_mainPanel);
        setModal(true);
        setTitle("License Agreement");
        _buttonOK.setEnabled(false);

        _buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        _buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        _isAgree.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _buttonOK.setEnabled(_isAgree.isSelected());
                    }
                }
        );

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        _mainPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        _licenseAgreement.setText("Somewhere in the future a license agreement text will show up here.");
    }

    public JPanel getMainPanel() {
        return _mainPanel;
    }

    public boolean isOk() {
        return _ok;
    }

    private void onOK() {
        _ok = true;

        dispose();
    }

    private void onCancel() {
        _ok = false;

        dispose();
    }
}
