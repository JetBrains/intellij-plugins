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
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class CreateFrameworkInstanceDialog extends DialogWrapper {

    private void createUIComponents() {
        _errorText = new MyErrorText();
    }


    public CreateFrameworkInstanceDialog(FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                                         String frameworkInstanceName) {
        super(true);
        setTitle("OSGi Framework Instance");
        setModal(true);

        if (frameworkInstanceName != null) {
            _nameTextField.setText(frameworkInstanceName);
        }

        FrameworkIntegrator[] integrators = frameworkIntegratorRegistry.getFrameworkIntegrators();
        _integratorComboBox.removeAllItems();
        for (FrameworkIntegrator integrator : integrators) {
            _integratorComboBox.addItem(integrator);
        }

        _integratorComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_integratorComboBox.getSelectedItem() != null && _nameTextField.getText().length() == 0) {
                    _nameTextField.setText(((FrameworkIntegrator) _integratorComboBox.getSelectedItem()).getDisplayName());
                }
            }
        });

        _nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                checkButtonOKEnabled();
            }
        });

        _integratorComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkButtonOKEnabled();
            }
        });

        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        _baseFolderChooser.addBrowseFolderListener("Choose framework instance base folder", "", null,
                fileChooserDescriptor);
        _baseFolderChooser.getTextField().setEditable(false);
        _baseFolderChooser.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                checkButtonOKEnabled();
            }
        });
        init();

        // add a check right here!
        checkButtonOKEnabled();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        // OSMORC-111 - focus the name field
        return _nameTextField;
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return _mainPanel;
    }

    private void checkButtonOKEnabled() {
        final FrameworkIntegrator integrator = (FrameworkIntegrator) _integratorComboBox.getSelectedItem();
        boolean isFrameworkDefinitionValid = false;
        if (integrator != null /* && baseFolderChooser.getText().length() > 0 */) {
            FrameworkInstanceDefinition definition = new FrameworkInstanceDefinition();
            definition.setName(getName());
            definition.setFrameworkIntegratorName(getIntegratorName());
            definition.setBaseFolder(getBaseFolder());
            String errorInfoText = integrator.getFrameworkInstanceManager().checkValidity(definition);
            ((MyErrorText) _errorText).setError(errorInfoText);
            isFrameworkDefinitionValid = (errorInfoText == null || errorInfoText.length() == 0);
        }
        setOKActionEnabled(_nameTextField.getText().length() > 0 && integrator != null &&
                isFrameworkDefinitionValid);
    }


    public String getIntegratorName() {
        FrameworkIntegrator integrator = (FrameworkIntegrator) _integratorComboBox.getSelectedItem();
        return integrator != null ? integrator.getDisplayName() : "";
    }

    public void setIntegratorName(String value) {
        int count = _integratorComboBox.getItemCount();
        for (int i = 0; i < count; i++) {
            FrameworkIntegrator integrator = (FrameworkIntegrator) _integratorComboBox.getItemAt(i);
            if (integrator.getDisplayName().equals(value)) {
                _integratorComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    public String getBaseFolder() {
        return _baseFolderChooser.getText();
    }

    public void setBaseFolder(String value) {
        _baseFolderChooser.setText(value);
    }


    public String getName() {
        return _nameTextField.getText();
    }

  @Override
  protected String getHelpId() {
    return "reference.settings.project.osgi.new.framework.instance";
  }

  private JPanel _mainPanel;
    private JComboBox _integratorComboBox;
    private JTextField _nameTextField;
    private TextFieldWithBrowseButton _baseFolderChooser;
    private JPanel _errorText;

}
