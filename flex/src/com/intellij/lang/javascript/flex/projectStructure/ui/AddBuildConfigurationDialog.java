package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.UISettings;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.PlatformIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Collection;

public class AddBuildConfigurationDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JTextField myNameTextField;
  private JComboBox myTargetPlatformCombo;
  private NonFocusableCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;
  private JLabel myUpDownHint;
  private final Collection<String> myUsedNames;

  public AddBuildConfigurationDialog(final Project project,
                                     final String dialogTitle,
                                     final Collection<String> usedNames,
                                     BuildConfigurationNature defaultNature,
                                     boolean hintEnabled) {
    super(project);
    myUsedNames = usedNames;
    setTitle(dialogTitle);
    initCombos();
    myTargetPlatformCombo.setSelectedItem(defaultNature.targetPlatform);
    myPureActionScriptCheckBox.setSelected(defaultNature.pureAS);
    myOutputTypeCombo.setSelectedItem(defaultNature.outputType);
    if (hintEnabled) {
      myUpDownHint.setIcon(PlatformIcons.UP_DOWN_ARROWS);
      myUpDownHint.setToolTipText(FlexBundle.message("bc.dialog.up.down.tooltip"));
      final AnAction arrow = new AnAction() {
        @Override
        public void actionPerformed(AnActionEvent e) {
          if (e.getInputEvent() instanceof KeyEvent) {
            KeyEvent keyEvent = (KeyEvent)e.getInputEvent();
            final int code = keyEvent.getKeyCode();
            scrollBy(code == KeyEvent.VK_DOWN ? 1 : code == KeyEvent.VK_UP ? -1 : 0, (keyEvent.getModifiers() & Event.SHIFT_MASK) != 0);
          }
        }
      };
      final KeyboardShortcut up = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), null);
      final KeyboardShortcut upShift = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), null);
      final KeyboardShortcut down = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), null);
      final KeyboardShortcut downShift = new KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), null);
      arrow.registerCustomShortcutSet(new CustomShortcutSet(up, down, upShift, downShift), myNameTextField);
    }
    else {
      myUpDownHint.setVisible(false);
    }
    init();
  }

  private void scrollBy(final int delta, boolean shiftPressed) {
    if (delta == 0) return;
    JComboBox combo = shiftPressed ? myOutputTypeCombo : myTargetPlatformCombo;
    final int size = combo.getModel().getSize();
    int next = combo.getSelectedIndex() + delta;
    if (next < 0 || next >= size) {
      if (!UISettings.getInstance().CYCLE_SCROLLING) {
        return;
      }
      next = (next + size) % size;
    }
    combo.setSelectedIndex(next);
  }

  private void initCombos() {
    TargetPlatform.initCombo(myTargetPlatformCombo);
    OutputType.initCombo(myOutputTypeCombo);
  }

  public void setBCNameEditable(final boolean editable) {
    myNameTextField.setEditable(editable);
  }

  public void setBCName(final String name) {
    myNameTextField.setText(name);
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameTextField.isEditable() ? myNameTextField : myTargetPlatformCombo;
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected ValidationInfo doValidate() {
    final String name = getName();

    if (name.isEmpty()) {
      return new ValidationInfo("Empty name", myNameTextField);
    }

    for (final String usedName : myUsedNames) {
      if (name.equals(usedName)) {
        return new ValidationInfo(MessageFormat.format("Name ''{0}'' is already used", name), myNameTextField);
      }
    }

    return null;
  }

  public String getName() {
    return myNameTextField.getText().trim();
  }

  public BuildConfigurationNature getNature() {
    TargetPlatform targetPlatform = (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
    boolean isPureAs = myPureActionScriptCheckBox.isSelected();
    OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();
    return new BuildConfigurationNature(targetPlatform, isPureAs, outputType);
  }
}

