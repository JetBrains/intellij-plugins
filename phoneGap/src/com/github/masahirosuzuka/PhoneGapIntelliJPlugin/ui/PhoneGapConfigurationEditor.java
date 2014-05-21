package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.PhoneGapRunConfiguration;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings.PHONEGAP_PLATFORM_ANDROID;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings.PHONEGAP_PLATFORM_IOS;
import static com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings.PHONEGAP_PLATFORM_RIPPLE;

/**
 * PhoneGapConfigurationEditor.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/04/06.
 */

public class PhoneGapConfigurationEditor extends SettingsEditor<PhoneGapRunConfiguration> {

    private JPanel component;
    private JRadioButton phoneGapRadioButton;
    private JRadioButton cordovaRadioButton;
    private JRadioButton androidRadioButton;
    private JRadioButton iOSRadioButton;
    private JRadioButton windowsPhoneRadioButton;
    private JRadioButton rippleButton;
    @SuppressWarnings("UnusedDeclaration")
    private JCheckBox weinreCheckBox;
    @SuppressWarnings("UnusedDeclaration")
    private JCheckBox logCatCheckBox;
    private JTextArea runScript;
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private Project myProject;

    public PhoneGapConfigurationEditor(Project project) {
        myProject = project;

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                updateRunScript();
            }
        };
        phoneGapRadioButton.addActionListener(actionListener);
        cordovaRadioButton.addActionListener(actionListener);
        androidRadioButton.addActionListener(actionListener);
        iOSRadioButton.addActionListener(actionListener);
        rippleButton.addActionListener(actionListener);

        cordovaRadioButton.setEnabled(PhoneGapSettings.getInstance().isCordovaAvailable());
        phoneGapRadioButton.setEnabled(PhoneGapSettings.getInstance().isPhoneGapAvailable());
    }

    @Override
    protected void resetEditorFrom(PhoneGapRunConfiguration phoneGapRunConfiguration) {
        setDefaults();
        if (phoneGapRunConfiguration == null) { // Creating new configuration
            return;
        }


        if (phoneGapRunConfiguration.getExecutableType() == PhoneGapRunConfiguration.Type.PHONEGAP) {
            phoneGapRadioButton.setSelected(true);
        }

        if (phoneGapRunConfiguration.getExecutableType() == PhoneGapRunConfiguration.Type.CORDOVA) {
            cordovaRadioButton.setSelected(true);
        }

        if (PhoneGapSettings.PHONEGAP_PLATFORM_ANDROID.equals(phoneGapRunConfiguration.getPlatform())) {
            androidRadioButton.setSelected(true);
        } else if (PhoneGapSettings.PHONEGAP_PLATFORM_IOS.equals(phoneGapRunConfiguration.getPlatform())) {
            iOSRadioButton.setSelected(true);
        } else if (PhoneGapSettings.PHONEGAP_PLATFORM_RIPPLE.equals(phoneGapRunConfiguration.getPlatform())) {
            rippleButton.setSelected(true);
        }

        updateRunScript();
    }

    private void setDefaults() {
        phoneGapRadioButton.setSelected(true);// PhoneGap is default SDK but user can choose Cordova
        androidRadioButton.setSelected(true);
        iOSRadioButton.setSelected(false);
        windowsPhoneRadioButton.setSelected(false);
        updateRunScript();
    }

    @Override
    protected void applyEditorTo(PhoneGapRunConfiguration phoneGapRunConfiguration) throws ConfigurationException {
        phoneGapRunConfiguration.setExecutableType(getExecutableType());
        phoneGapRunConfiguration.setPlatform(getPlatform());
        phoneGapRunConfiguration.setCommand(getCommand());
    }


    private String getPlatform() {
        if (androidRadioButton.isSelected()) return PHONEGAP_PLATFORM_ANDROID;
        if (iOSRadioButton.isSelected()) return PHONEGAP_PLATFORM_IOS;
        if (rippleButton.isSelected()) return PHONEGAP_PLATFORM_RIPPLE;

        //default
        return PHONEGAP_PLATFORM_ANDROID;
    }

    private String getCommand() {
        return "run";
    }


    @NotNull
    private PhoneGapRunConfiguration.Type getExecutableType() {
        if (phoneGapRadioButton.isSelected()) return PhoneGapRunConfiguration.Type.PHONEGAP;
        if (cordovaRadioButton.isSelected()) return PhoneGapRunConfiguration.Type.CORDOVA;

        return PhoneGapRunConfiguration.Type.PHONEGAP;
    }


    @NotNull
    @Override
    protected JComponent createEditor() {
        return component;
    }

    private void updateRunScript() {
        if (getPlatform().equals("ripple")) {
            runScript.setText("node server.js");
            return;
        }
        runScript.setText(getExecutableType().getName() + " " + getCommand() + " " + getPlatform());
    }
}
