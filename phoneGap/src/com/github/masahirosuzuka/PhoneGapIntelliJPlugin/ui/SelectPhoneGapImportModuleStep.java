package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportWizardStep;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

/**
 * Created by Masahiro Suzuka on 2014/04/10.
 */
public class SelectPhoneGapImportModuleStep extends ProjectImportWizardStep {

  private JPanel component;
  private JTextField projectRootPathField;
  private JTree projectTree;

  public String projectRootPath;

  public SelectPhoneGapImportModuleStep(final WizardContext context) {
    super(context);
    projectRootPathField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectRootPath = projectRootPathField.getText();
      }
    });
  }

  @Override
  public void updateDataModel() {

  }

  @Override
  public JComponent getComponent() {
    return component;
  }
}
