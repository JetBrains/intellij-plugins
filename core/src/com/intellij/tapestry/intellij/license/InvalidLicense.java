package com.intellij.tapestry.intellij.license;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.tapestry.intellij.util.Icons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class InvalidLicense extends DialogWrapper {

    private JPanel contentPane;
    private JButton _buttonOK;
    private JButton _buttonCancel;
    private JTextArea _textPane;
    private JTextArea _license;
    private JButton _buttonGetLicense;
    private JLabel _image;
    private boolean _ok;

    public InvalidLicense(String message) {
        super(true);

        setModal(true);
        getRootPane().setDefaultButton(_buttonOK);
        setTitle("Loomy License");

        //Para alterar
        _image.setIcon(new ImageIcon(Icons.class.getResource("/com/intellij/tapestry/core/icons/g5004.png")));

        _textPane.setText(_textPane.getText().replace("${message}", message));

        _buttonOK.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onOK();
                    }
                }
        );

        _buttonCancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onCancel();
                    }
                }
        );

        _buttonGetLicense.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        BrowserUtil.launchBrowser("http://www.intellij-software.com");
                    }
                }
        );

        contentPane.registerKeyboardAction(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        onCancel();
                    }
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        init();
    }


    @Nullable
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected JComponent createSouthPanel() {
        return null;
    }

    public String getLicense() {
        return _license.getText();
    }

    public boolean isOk() {
        return _ok;
    }

    private void onOK() {
        _ok = true;

        dispose();
    }

    private void onCancel() {
        _license.setText("");
        _ok = false;

        dispose();
    }
}
