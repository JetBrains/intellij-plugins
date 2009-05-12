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
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class CreateFrameworkInstanceDialog extends DialogWrapper
{
  private void createUIComponents()
  {
    _errorText = new ErrorText();
  }


  public CreateFrameworkInstanceDialog(FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                                       String frameworkInstanceName)
  {
    super(true);
    setTitle("Create new Framework Instance");
    setModal(true);

    if (frameworkInstanceName != null)
    {
      _nameTextField.setText(frameworkInstanceName);
    }

    FrameworkIntegrator[] integrators = frameworkIntegratorRegistry.getFrameworkIntegrators();
    _integratorComboBox.removeAllItems();
    for (FrameworkIntegrator integrator : integrators)
    {
      _integratorComboBox.addItem(integrator);
    }

    _integratorComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (_integratorComboBox.getSelectedItem() != null && _nameTextField.getText().length() == 0)
        {
          _nameTextField.setText(((FrameworkIntegrator) _integratorComboBox.getSelectedItem()).getDisplayName());
        }
      }
    });

    _nameTextField.getDocument().addDocumentListener(new DocumentAdapter()
    {
      protected void textChanged(DocumentEvent e)
      {
        checkButtonOKEnabled();
      }
    });

    _integratorComboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        checkButtonOKEnabled();
      }
    });

    FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    _baseFolderChooser.addBrowseFolderListener("Choose framework instance base folder", "", null,
        fileChooserDescriptor);
    _baseFolderChooser.getTextField().setEditable(false);
    _baseFolderChooser.getTextField().getDocument().addDocumentListener(new DocumentAdapter()
    {
      protected void textChanged(DocumentEvent e)
      {
        checkButtonOKEnabled();
      }
    });
    init();

    // add a check right here!
    checkButtonOKEnabled();
  }

  @Override
  public JComponent getPreferredFocusedComponent()
  {
    // OSMORC-111 - focus the name field
    return _nameTextField;
  }

  @Nullable
  protected JComponent createCenterPanel()
  {
    return _mainPanel;
  }

  private void checkButtonOKEnabled()
  {
    final FrameworkIntegrator integrator = (FrameworkIntegrator) _integratorComboBox.getSelectedItem();
    boolean isFrameworkDefinitionValid = false;
    if (integrator != null /* && baseFolderChooser.getText().length() > 0 */)
    {
      FrameworkInstanceDefinition definition = new FrameworkInstanceDefinition();
      definition.setName(getName());
      definition.setFrameworkIntegratorName(getIntegratorName());
      definition.setBaseFolder(getBaseFolder());
      String errorInfoText = integrator.getFrameworkInstanceManager().checkValidity(definition);
      ((ErrorText) _errorText).setError(errorInfoText);
      isFrameworkDefinitionValid = (errorInfoText == null || errorInfoText.length() == 0);
    }
    setOKActionEnabled(_nameTextField.getText().length() > 0 && integrator != null &&
        isFrameworkDefinitionValid);
  }


  public String getIntegratorName()
  {
    FrameworkIntegrator integrator = (FrameworkIntegrator) _integratorComboBox.getSelectedItem();
    return integrator != null ? integrator.getDisplayName() : "";
  }

  public String getBaseFolder()
  {
    return _baseFolderChooser.getText();
  }

  public String getName()
  {
    return _nameTextField.getText();
  }

  // This is actually built into DialogWrapper, but it does not resize properly on long strings, so i had
  // to duplicate it. Relayout of the dialog should fix OSMORC-111
  private static class ErrorText extends JPanel
  {

    public void setError(String text)
    {
      if (text == null)
      {
        myLabel.setText("");
        myLabel.setIcon(null);
        setBorder(null);
      }
      else
      {
        myLabel.setText((new StringBuilder()).append("<html><body><font color=red><left>").append(text)
            .append("</left></b></font></body></html>").toString());
        myLabel.setIcon(IconLoader.getIcon("/actions/lightning.png"));
        myLabel.setBorder(new EmptyBorder(2, 2, 0, 0));
        myPrefSize = myLabel.getPreferredSize();
      }
      revalidate();
    }

    public Dimension getPreferredSize()
    {
      return myPrefSize != null ? myPrefSize : super.getPreferredSize();
    }

    private final JLabel myLabel = new JLabel();
    private Dimension myPrefSize;

    public ErrorText()
    {
      myLabel.setVerticalAlignment(JLabel.TOP);
      setLayout(new BorderLayout());
      setBorder(null);
      UIUtil.removeQuaquaVisualMarginsIn(this);
      add(myLabel, "Center");
    }
  }

  private JPanel _mainPanel;
  private JComboBox _integratorComboBox;
  private JTextField _nameTextField;
  private TextFieldWithBrowseButton _baseFolderChooser;
  private JPanel _errorText;
}
