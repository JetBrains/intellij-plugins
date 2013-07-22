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
package org.osmorc.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for creating/updating a framework instance.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class CreateFrameworkInstanceDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private JComboBox myIntegratorComboBox;
  private JTextField myNameTextField;
  private JTextField myVersionField;
  private TextFieldWithBrowseButton myBaseFolderChooser;
  private JPanel myErrorText;

  public CreateFrameworkInstanceDialog(@NotNull FrameworkIntegratorRegistry registry, @Nullable FrameworkInstanceDefinition framework) {
    super(true);

    setTitle("OSGi Framework Instance");
    setModal(true);

    //noinspection unchecked
    myIntegratorComboBox.setModel(new DefaultComboBoxModel(registry.getFrameworkIntegrators()));
    myIntegratorComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        FrameworkIntegrator integrator = (FrameworkIntegrator)myIntegratorComboBox.getSelectedItem();
        if (integrator != null && StringUtil.isEmptyOrSpaces(myNameTextField.getText())) {
          myNameTextField.setText(integrator.getDisplayName());
        }
        updateUiState();
      }
    });

    myNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateUiState();
      }
    });

    FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    myBaseFolderChooser.addBrowseFolderListener("Choose framework instance base folder", "", null, fileChooserDescriptor);
    myBaseFolderChooser.getTextField().setEditable(false);
    myBaseFolderChooser.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        updateUiState();
      }
    });

    if (framework != null) {
      myNameTextField.setText(framework.getName());
      myIntegratorComboBox.setSelectedItem(FrameworkIntegratorRegistry.getInstance().findIntegratorByName(framework.getName()));
      myBaseFolderChooser.setText(framework.getBaseFolder());
      myVersionField.setText(framework.getVersion());
    }

    init();
    updateUiState();
  }

  private void createUIComponents() {
    myErrorText = new MyErrorText();
  }

  private void updateUiState() {
    FrameworkIntegrator integrator = (FrameworkIntegrator)myIntegratorComboBox.getSelectedItem();
    if (integrator != null) {
      FrameworkInstanceDefinition definition = createDefinition();
      String errorInfoText = integrator.getFrameworkInstanceManager().checkValidity(definition);
      ((MyErrorText)myErrorText).setError(errorInfoText);
      setOKActionEnabled(!StringUtil.isEmptyOrSpaces(myNameTextField.getText()) && StringUtil.isEmpty(errorInfoText));
    }
    else {
      setOKActionEnabled(false);
    }
  }

  @NotNull
  public FrameworkInstanceDefinition createDefinition() {
    FrameworkInstanceDefinition framework = new FrameworkInstanceDefinition();
    framework.setName(myNameTextField.getText().trim());
    FrameworkIntegrator integrator = (FrameworkIntegrator)myIntegratorComboBox.getSelectedItem();
    assert integrator != null;
    framework.setFrameworkIntegratorName(integrator.getDisplayName());
    framework.setBaseFolder(myBaseFolderChooser.getText().trim());
    framework.setVersion(myVersionField.getText().trim());
    return framework;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myIntegratorComboBox;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected String getHelpId() {
    return "reference.settings.project.osgi.new.framework.instance";
  }
}
