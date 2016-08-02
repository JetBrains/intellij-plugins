package org.jetbrains.appcode.reveal;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.cidr.execution.AppCodeRunConfiguration;
import com.jetbrains.cidr.xcode.frameworks.AppleSdk;
import com.jetbrains.cidr.xcode.model.XCBuildConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class RevealRunConfigurationEditor<T extends AppCodeRunConfiguration> extends SettingsEditor<T> {
    private HyperlinkLabel myRevealNotFoundOrIncompatible;
    private JBLabel myNotAvailable;

    private JBCheckBox myInjectCheckBox;
    private JBLabel myInjectHint;
    private JBCheckBox myInstallCheckBox;
    private JBLabel myInstallHint;

    private JComboBox applicationListComboBox;

    boolean isFound;
    boolean isAvailable;

    @Override
    protected void resetEditorFrom(AppCodeRunConfiguration s) {
        RevealRunConfigurationExtension.RevealSettings settings = getRevealSettings(s);

        myInjectCheckBox.setSelected(settings.autoInject);
        myInstallCheckBox.setSelected(settings.autoInstall);

        XCBuildConfiguration xcBuildConfiguration = s.getConfiguration();
        AppleSdk sdk = xcBuildConfiguration == null ? null : xcBuildConfiguration.getBaseSdk();
        boolean found = Reveal.getRevealLib(sdk) != null;
        boolean compatible = Reveal.isCompatible();

        String notFoundText = null;
        if (!found) {
            notFoundText = "Reveal.app not found. You can download and install it from ";
        }
        else if (!compatible) {
            notFoundText = "Incompatible version of Reveal.app. You can download the latest version from ";
        }
        if (notFoundText != null) {
            myRevealNotFoundOrIncompatible.setHyperlinkText(notFoundText, "revealapp.com", "");
        }

        isFound = found && compatible;
        isAvailable = isAvailableForPlatform(s);

        updateControls();
    }

    @Override
    protected void applyEditorTo(AppCodeRunConfiguration s) throws ConfigurationException {
        RevealRunConfigurationExtension.RevealSettings settings = s.getRevealSettings(s);

        settings.autoInject = myInjectCheckBox.isSelected();
        settings.autoInstall = myInstallCheckBox.isSelected();

        s.setRevealSettings(s, settings);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        FormBuilder builder = new FormBuilder();

        myRevealNotFoundOrIncompatible = new HyperlinkLabel();
        myRevealNotFoundOrIncompatible.setIcon(AllIcons.RunConfigurations.ConfigurationWarning);
        myRevealNotFoundOrIncompatible.setHyperlinkTarget("http://revealapp.com");

        myNotAvailable = new JBLabel("<html>" +
                "Reveal integration is only available for iOS applications.<br>" +
                "OS X targets are not yet supported.<br>" +
                "</html>");

        myInjectCheckBox = new JBCheckBox("Inject Reveal library on launch");
        myInstallCheckBox = new JBCheckBox("Upload Reveal library on the device if necessary");

        myInjectHint = new JBLabel(UIUtil.ComponentStyle.SMALL);
        myInstallHint = new JBLabel(UIUtil.ComponentStyle.SMALL);

        myInjectCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateControls();
            }
        });

        builder.addComponent(myNotAvailable);
        builder.addComponent(myInjectCheckBox, UIUtil.DEFAULT_VGAP * 3);
        builder.setIndent(UIUtil.DEFAULT_HGAP * 4);
        builder.addComponent(myInjectHint);
        builder.setIndent(UIUtil.DEFAULT_HGAP);
        builder.addComponent(myInstallCheckBox);
        builder.setIndent(UIUtil.DEFAULT_HGAP * 5);
        builder.addComponent(myInstallHint);

        JPanel controls = builder.getPanel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(controls, BorderLayout.NORTH);
        panel.add(Box.createGlue(), BorderLayout.CENTER);
        panel.add(myRevealNotFoundOrIncompatible, BorderLayout.SOUTH);
        return panel;
    }

    private void updateControls() {
        boolean controlsEnabled = isFound && isAvailable;

        myRevealNotFoundOrIncompatible.setVisible(!isFound);
        myNotAvailable.setVisible(!isAvailable);

        updateStatusAndHint(myInjectCheckBox, myInjectHint,
                controlsEnabled,
                "Library is injected on launch using DYLD_INSERT_LIBRARIES variable");

        boolean installButtonEnabled = controlsEnabled && myInjectCheckBox.isSelected();
        updateStatusAndHint(myInstallCheckBox, myInstallHint,
                installButtonEnabled,
                "It's not necessary to configure the project manually,<br>" +
                        "library is signed and uploaded automatically"
        );
    }

    private static void updateStatusAndHint(JComponent comp, JBLabel label, boolean enabled, String text) {
        comp.setEnabled(enabled);
        label.setEnabled(enabled);
        StringBuilder fontString = new StringBuilder();
        Color color = enabled ? UIUtil.getLabelForeground() : UIUtil.getLabelDisabledForeground();
        if (color != null) {
            fontString.append("<font color=#");
            UIUtil.appendColor(color, fontString);
            fontString.append(">");
        }
        label.setText("<html>" + fontString + text + "</html>");
    }

}
