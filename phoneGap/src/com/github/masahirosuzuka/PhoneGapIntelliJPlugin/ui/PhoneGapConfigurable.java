package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * PhoneGapSettingDialog.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/05/05.
 */
@SuppressWarnings("UnusedDeclaration")
public class PhoneGapConfigurable implements Configurable {

    private JTextField myPhoneGapExecutablePathField;
    private JTextField myNodeJSInstallDirField;
    private JTextField myAndroidDirField;
    private JTextField myIosDirField;
    private JPanel myComponents;
    private JTextField myCordovaExecutablePathField;

    private final PhoneGapSettings mySettings = PhoneGapSettings.getInstance();
    private UIController myUIController;

    private class UIController {

        public void reset(PhoneGapSettings.State state) {
            myPhoneGapExecutablePathField.setText(state.phoneGapExecutablePath);
            myCordovaExecutablePathField.setText(state.cordovaExecutablePath);
        }

        public boolean isModified() {
            return !getState().equals(mySettings.getState());
        }

        private PhoneGapSettings.State getState() {
            PhoneGapSettings.State state = new PhoneGapSettings.State();
            state.cordovaExecutablePath = myCordovaExecutablePathField.getText();
            state.phoneGapExecutablePath = myPhoneGapExecutablePathField.getText();
            return state;
        }
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "PhoneGap/Cordova";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "phoneGap/cordova";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (myUIController == null) {
            myUIController = new UIController();
            myUIController.reset(mySettings.getState());
        }
        return myComponents;
    }

    @Override
    public boolean isModified() {
        return myUIController.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        mySettings.loadState(myUIController.getState());
    }

    @Override
    public void reset() {
        myUIController.reset(mySettings.getState());
    }

    @Override
    public void disposeUIResources() {

    }
}
