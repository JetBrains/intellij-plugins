package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BindablePropertiesForm implements EventBinder {
  private static final String PROPERTY_PLACEHOLDER = "{property}";
  private static final String PROPERTY_UPPERCASE_PLACEHOLDER = "{PROPERTY}";

  private JPanel myMainPanel;
  private JCheckBox myBindEventCheckBox;
  private JCheckBox myEventConstantCheckBox;
  private JTextField myEventConstantTextField;
  private JTextField myEventTextField;
  private JSCodeStyleSettings myCodeStyleSettings;

  public BindablePropertiesForm(final Project project, final boolean suggestEventConstant) {
    myEventConstantCheckBox.setVisible(suggestEventConstant);
    myEventConstantTextField.setVisible(suggestEventConstant);
    
    myCodeStyleSettings = CodeStyleSettingsManager.getSettings(project).getCustomSettings(JSCodeStyleSettings.class);
    myEventTextField.setText(constructEventName(PROPERTY_PLACEHOLDER));
    myEventConstantTextField.setText(constructEventConstantName(PROPERTY_UPPERCASE_PLACEHOLDER));

    myBindEventCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        if (myEventTextField.isEnabled()) {
          IdeFocusManager.getInstance(project).requestFocus(myEventTextField, true);
        }
      }
    });

    myEventConstantCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        if (myEventConstantTextField.isEnabled()) {
          IdeFocusManager.getInstance(project).requestFocus(myEventConstantTextField, true);
        }
      }
    });
        
    updateControls();
  }

  private void updateControls() {
    myEventTextField.setEnabled(myBindEventCheckBox.isSelected());
    myEventConstantCheckBox.setEnabled(myBindEventCheckBox.isSelected());
    myEventConstantTextField.setEnabled(myEventConstantCheckBox.isEnabled() && myEventConstantCheckBox.isSelected());
  }

  private static String constructEventName(final String property) {
    return property + "Changed";
  }

  private static String constructEventConstantName(final String property) {
    return property + "_CHANGED_EVENT";
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public boolean isBindEvent() {
    return myBindEventCheckBox.isSelected();
  }

  public String getEventName(final String parameterName) {
    return replaceTokens(myEventTextField.getText().trim(),
                         JSResolveUtil.transformVarNameToAccessorName(parameterName, myCodeStyleSettings));
  }

  public boolean isCreateEventConstant() {
    return myEventConstantCheckBox.isSelected();
  }

  public String getEventConstantName(final String parameterName) {
    return replaceTokens(myEventConstantTextField.getText().trim(),
                         JSResolveUtil.transformVarNameToAccessorName(parameterName, myCodeStyleSettings));
  }

  private static String replaceTokens(final String s, final String parameterName) {
    return s.replace(PROPERTY_PLACEHOLDER, parameterName).replace(PROPERTY_UPPERCASE_PLACEHOLDER, parameterName.toUpperCase());
  }
}
