package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.ui.UISettings;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Collection;

public class AddBuildConfigurationDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JTextField myBCNameTextField;
  private JLabel myUpDownHint;
  private JComboBox myTargetPlatformCombo;
  private NonFocusableCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;
  private JLabel myTargetDevicesLabel;
  private JCheckBox myAndroidCheckBox;
  private JCheckBox myIOSCheckBox;

  private final Collection<String> myUsedNames;

  public AddBuildConfigurationDialog(final Project project,
                                     final String dialogTitle,
                                     final Collection<String> usedNames,
                                     final BuildConfigurationNature defaultNature,
                                     final boolean bcNameEditable) {
    super(project);
    myUsedNames = usedNames;
    setTitle(dialogTitle);
    initCombos();
    myTargetPlatformCombo.setSelectedItem(defaultNature.targetPlatform);
    myPureActionScriptCheckBox.setSelected(defaultNature.pureAS);
    myOutputTypeCombo.setSelectedItem(defaultNature.outputType);

    myBCNameTextField.setEditable(bcNameEditable);
    myUpDownHint.setVisible(bcNameEditable);

    if (bcNameEditable) {
      myUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
      myUpDownHint.setToolTipText(FlexBundle.message("bc.dialog.up.down.tooltip"));
      final AnAction arrow = new AnAction() {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
          if (e.getInputEvent() instanceof KeyEvent keyEvent) {
            final int code = keyEvent.getKeyCode();
            scrollBy(code == KeyEvent.VK_DOWN ? 1 : code == KeyEvent.VK_UP ? -1 : 0, (keyEvent.getModifiers() & Event.SHIFT_MASK) != 0);
          }
        }
      };
      final KeyboardShortcut up = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), null);
      final KeyboardShortcut upShift = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), null);
      final KeyboardShortcut down = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), null);
      final KeyboardShortcut downShift = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), null);
      arrow.registerCustomShortcutSet(new CustomShortcutSet(up, down, upShift, downShift), myBCNameTextField);
    }

    init();
    updateControls();
  }

  private void updateControls() {
    final boolean mobile = myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Mobile;
    final boolean app = myOutputTypeCombo.getSelectedItem() == OutputType.Application;
    final boolean targetDeviceWasEnabled = myTargetDevicesLabel.isEnabled();

    myTargetDevicesLabel.setEnabled(mobile && app);
    myAndroidCheckBox.setEnabled(mobile && app);
    myIOSCheckBox.setEnabled(mobile && app);

    if (myTargetDevicesLabel.isEnabled() && !targetDeviceWasEnabled) {
      myAndroidCheckBox.setSelected(true);
      myIOSCheckBox.setSelected(true);
    }

    if (!myTargetDevicesLabel.isEnabled()) {
      // disabled but checked for mobile library
      myAndroidCheckBox.setSelected(mobile);
      myIOSCheckBox.setSelected(mobile);
    }
  }

  private void scrollBy(final int delta, boolean shiftPressed) {
    if (delta == 0) return;
    JComboBox combo = shiftPressed ? myOutputTypeCombo : myTargetPlatformCombo;
    final int size = combo.getModel().getSize();
    int next = combo.getSelectedIndex() + delta;
    if (next < 0 || next >= size) {
      if (!UISettings.getInstance().getCycleScrolling()) {
        return;
      }
      next = (next + size) % size;
    }
    combo.setSelectedIndex(next);
  }

  private void initCombos() {
    BCUtils.initTargetPlatformCombo(myTargetPlatformCombo);
    BCUtils.initOutputTypeCombo(myOutputTypeCombo);

    final ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myTargetPlatformCombo.addActionListener(actionListener);
    myOutputTypeCombo.addActionListener(actionListener);
  }

  public void reset(final String bcName, final boolean androidEnabled, final boolean iosEnabled) {
    myBCNameTextField.setText(bcName);
    myAndroidCheckBox.setSelected(androidEnabled);
    myIOSCheckBox.setSelected(iosEnabled);
    updateControls();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myBCNameTextField.isEditable() ? myBCNameTextField : myTargetPlatformCombo;
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected ValidationInfo doValidate() {
    final String name = getBCName();

    if (name.isEmpty()) {
      return new ValidationInfo("Empty name", myBCNameTextField);
    }

    for (final String usedName : myUsedNames) {
      if (name.equals(usedName)) {
        return new ValidationInfo(MessageFormat.format("Name ''{0}'' is already used", name), myBCNameTextField);
      }
    }

    return null;
  }

  public String getBCName() {
    return myBCNameTextField.getText().trim();
  }

  public BuildConfigurationNature getNature() {
    TargetPlatform targetPlatform = (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
    boolean isPureAs = myPureActionScriptCheckBox.isSelected();
    OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();
    return new BuildConfigurationNature(targetPlatform, isPureAs, outputType);
  }

  public boolean isAndroidEnabled() {
    return myAndroidCheckBox.isSelected();
  }

  public boolean isIOSEnabled() {
    return myIOSCheckBox.isSelected();
  }
}

