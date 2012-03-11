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
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
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
public class CreateFrameworkInstanceDialog extends DialogWrapper implements PanelWithAnchor {

  private JPanel myMainPanel;
  private JComboBox myIntegratorComboBox;
  private JTextField myNameTextField;
  private TextFieldWithBrowseButton myBaseFolderChooser;
  private JPanel myErrorText;
  private JTextField myVersionField;
  private JRadioButton myUseLocallyInstalledFrameworkRadioButton;
  private JRadioButton myDownloadAFrameworkWithRadioButton;
  private TextFieldWithBrowseButton myTargetFolder;
  private JComboBox myComboBox1;
  private JBLabel myBaseFolderLabel;
  private JBLabel myFolderLabel;
  private JBLabel myProfilesLabel;
  private JButton myDownloadButton;
  private JCheckBox myClearFolderBeforeDowloadCheckBox;
  private JComponent myAnchor;

  private void createUIComponents() {
    myErrorText = new MyErrorText();
  }


  public CreateFrameworkInstanceDialog(FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                                       String frameworkInstanceName) {
    super(true);
    setTitle("OSGi Framework Instance");
    setModal(true);

    if (frameworkInstanceName != null) {
      myNameTextField.setText(frameworkInstanceName);
    }

    FrameworkIntegrator[] integrators = frameworkIntegratorRegistry.getFrameworkIntegrators();
    myIntegratorComboBox.removeAllItems();
    for (FrameworkIntegrator integrator : integrators) {
      myIntegratorComboBox.addItem(integrator);
    }

    myIntegratorComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myIntegratorComboBox.getSelectedItem() != null && myNameTextField.getText().length() == 0) {
          myNameTextField.setText(((FrameworkIntegrator)myIntegratorComboBox.getSelectedItem()).getDisplayName());
        }
      }
    });

    myNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        updateUiState();
      }
    });

    myIntegratorComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateUiState();
      }
    });

    myUseLocallyInstalledFrameworkRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateUiState();
      }
    });

    myDownloadAFrameworkWithRadioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateUiState();
      }
    });

    myDownloadButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        downloadFramework();
      }
    });

    FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
    myBaseFolderChooser.addBrowseFolderListener("Choose framework instance base folder", "", null,
                                                fileChooserDescriptor);
    myBaseFolderChooser.getTextField().setEditable(false);
    myBaseFolderChooser.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(DocumentEvent e) {
        updateUiState();
      }
    });

    myTargetFolder.addBrowseFolderListener("Choose folder where to download the framework to", "", null, fileChooserDescriptor);
    myTargetFolder.getTextField().setEditable(false);
    myTargetFolder.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        updateUiState();
      }
    });
    init();

    // add a check right here!
    updateUiState();

    setAnchor(myBaseFolderLabel);
  }


  @Override
  public JComponent getPreferredFocusedComponent() {
    // OSMORC-111 - focus the name field
    return myNameTextField;
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  private void updateUiState() {
    boolean isDownload = isDownload();
    myBaseFolderLabel.setEnabled(!isDownload);
    myBaseFolderChooser.setEnabled(!isDownload);

    myTargetFolder.setEnabled(isDownload);
    myFolderLabel.setEnabled(isDownload);
    myComboBox1.setEnabled(isDownload);
    myProfilesLabel.setEnabled(isDownload);
    myClearFolderBeforeDowloadCheckBox.setEnabled(isDownload);


    final FrameworkIntegrator integrator = (FrameworkIntegrator)myIntegratorComboBox.getSelectedItem();
    boolean hasFolderSelected = !StringUtil.isEmpty(getBaseFolder());
    boolean hasIntegratorSelected = integrator != null;

    myDownloadButton.setEnabled(isDownload && hasFolderSelected && hasIntegratorSelected);


    boolean isFrameworkDefinitionValid = false;
    if (integrator != null /* && baseFolderChooser.getText().length() > 0 */) {
      FrameworkInstanceDefinition definition = new FrameworkInstanceDefinition();
      definition.setName(getName());
      definition.setFrameworkIntegratorName(getIntegratorName());
      definition.setBaseFolder(getBaseFolder());
      definition.setVersion(getVersion());
      definition.setDownloadedByPaxRunner(isDownload());
      String errorInfoText = integrator.getFrameworkInstanceManager().checkValidity(definition);
      ((MyErrorText)myErrorText).setError(errorInfoText);
      isFrameworkDefinitionValid = (errorInfoText == null || errorInfoText.length() == 0);
    }
    setOKActionEnabled(myNameTextField.getText().length() > 0 && integrator != null &&
                       isFrameworkDefinitionValid);
  }


  public String getIntegratorName() {
    FrameworkIntegrator integrator = (FrameworkIntegrator)myIntegratorComboBox.getSelectedItem();
    return integrator != null ? integrator.getDisplayName() : "";
  }


  public void setIntegratorName(String value) {
    int count = myIntegratorComboBox.getItemCount();
    for (int i = 0; i < count; i++) {
      FrameworkIntegrator integrator = (FrameworkIntegrator)myIntegratorComboBox.getItemAt(i);
      if (integrator.getDisplayName().equals(value)) {
        myIntegratorComboBox.setSelectedIndex(i);
        break;
      }
    }
  }

  public String getVersion() {
    return myVersionField.getText().trim();
  }

  public void setVersion(String value) {
    myVersionField.setText(value);
  }

  public String getBaseFolder() {
    return isDownload() ? myTargetFolder.getText() : myBaseFolderChooser.getText();
  }

  public void setBaseFolder(String value) {
    myBaseFolderChooser.setText(value);
    myTargetFolder.setText(value);
  }


  public String getName() {
    return myNameTextField.getText();
  }

  public boolean isDownload() {
    return myDownloadAFrameworkWithRadioButton.isSelected();
  }

  public void setDownload(boolean download) {
    if (download) {
      myDownloadAFrameworkWithRadioButton.setSelected(true);
    }
    else {
      myUseLocallyInstalledFrameworkRadioButton.setSelected(true);
    }
  }

  /**
   * Downloads the selected framework.
   */
  private void downloadFramework() {

    PaxFrameworkDownloader paxFrameworkDownloader =
      new PaxFrameworkDownloader(getIntegratorName().toLowerCase(), getVersion(), myTargetFolder.getText().trim(), "",
                                 myClearFolderBeforeDowloadCheckBox.isSelected(), new PaxFrameworkDownloader.DownloaderCallback() {
        @Override
        public void downloadFinished(boolean successful, @Nullable final String errorMessage) {
          if (!successful) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              @Override
              public void run() {
                // this is currently broken.
                //Messages.showErrorDialog(myDownloadButton, errorMessage, "Error when downloading framework");
                // so put it into the error text field for now.
                ((MyErrorText)myErrorText).setError(errorMessage);
              }
            });
          }
          else {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              @Override
              public void run() {
                ((MyErrorText)myErrorText).setStatus("Download successful.");
              }
            });
          }
        }
      });
    paxFrameworkDownloader.download();
  }

  @Override
  protected String getHelpId() {
    return "reference.settings.project.osgi.new.framework.instance";
  }

  @Override
  public JComponent getAnchor() {
    return myAnchor;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    this.myAnchor = anchor;
    myBaseFolderLabel.setAnchor(anchor);
    myFolderLabel.setAnchor(anchor);
    myProfilesLabel.setAnchor(anchor);
  }
}
