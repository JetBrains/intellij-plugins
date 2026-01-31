/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.frameworkintegration.impl.equinox.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.equinox.EquinoxRunProperties;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;
import org.osmorc.run.ui.GenericRunPropertiesEditor;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class EquinoxRunPropertiesEditor implements FrameworkRunPropertiesEditor {
  private JPanel myMainPanel;
  private GenericRunPropertiesEditor myGenericRunPropertiesEditor;
  private JRadioButton myJustTheBundlesRadioButton;
  private JRadioButton myProductRadioButton;
  private JTextField myProductTextField;
  private JRadioButton myApplicationRadioButton;
  private JTextField myApplicationTextField;

  public EquinoxRunPropertiesEditor() {
    ActionListener updater = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updatePresentation();
      }
    };
    myJustTheBundlesRadioButton.addActionListener(updater);
    myProductRadioButton.addActionListener(updater);
    myApplicationRadioButton.addActionListener(updater);
  }

  private void createUIComponents() {
    myGenericRunPropertiesEditor = new GenericRunPropertiesEditor();
  }

  private void updatePresentation() {
    if (myJustTheBundlesRadioButton.isSelected()) {
      myProductTextField.setText("");
      myProductTextField.setEnabled(false);
      myApplicationTextField.setText("");
      myApplicationTextField.setEnabled(false);
    }
    else if (myProductRadioButton.isSelected()) {
      myApplicationTextField.setText("");
      myApplicationTextField.setEnabled(false);
      myProductTextField.setEnabled(true);
    }
    else if (myApplicationRadioButton.isSelected()) {
      myProductTextField.setText("");
      myProductTextField.setEnabled(false);
      myApplicationTextField.setEnabled(true);
    }
  }

  @Override
  public JPanel getUI() {
    return myMainPanel;
  }

  @Override
  public void resetEditorFrom(@NotNull OsgiRunConfiguration runConfiguration) {
    myGenericRunPropertiesEditor.resetEditorFrom(runConfiguration);

    Map<String, String> properties = runConfiguration.getAdditionalProperties();
    String product = EquinoxRunProperties.getEquinoxProduct(properties);
    String application = EquinoxRunProperties.getEquinoxApplication(properties);
    if (!StringUtil.isEmptyOrSpaces(product)) {
      myProductRadioButton.setSelected(true);
      myProductTextField.setText(product);
    }
    else if (!StringUtil.isEmptyOrSpaces(application)) {
      myApplicationRadioButton.setSelected(true);
      myApplicationTextField.setText(application);
    }
    else {
      myJustTheBundlesRadioButton.setSelected(true);
    }

    updatePresentation();
  }

  @Override
  public void applyEditorTo(@NotNull OsgiRunConfiguration runConfiguration) throws ConfigurationException {
    myGenericRunPropertiesEditor.applyEditorTo(runConfiguration);

    Map<String, String> properties = new HashMap<>();
    EquinoxRunProperties.setEquinoxProduct(properties, myProductTextField.getText());
    EquinoxRunProperties.setEquinoxApplication(properties, myApplicationTextField.getText());
    runConfiguration.putAdditionalProperties(properties);
  }
}
