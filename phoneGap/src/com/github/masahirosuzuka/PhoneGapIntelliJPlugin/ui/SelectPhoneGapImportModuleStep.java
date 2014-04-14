package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.projectImport.ProjectImportWizardStep;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Masahiro Suzuka on 2014/04/10.
 */
public class SelectPhoneGapImportModuleStep extends ProjectImportWizardStep {

  private JPanel component;
  private TextFieldWithBrowseButton projectRootPathField;
  private JList moduleList;

  public SelectPhoneGapImportModuleStep(final WizardContext context) {
    super(context);
    projectRootPathField.setText(context.getProjectFileDirectory());

    //Fix me!!
    String[] PhoneGapFolders = new String[]{PhoneGapSettings.PHONEGAP_FOLDERS_CORDOVA,
        PhoneGapSettings.PHONEGAP_FOLDERS_HOOKS,
        PhoneGapSettings.PHONEGAP_FOLDERS_MERGES,
        PhoneGapSettings.PHONEGAP_FOLDERS_NODE_MODULES,
        PhoneGapSettings.PHONEGAP_FOLDERS_PLATFORMS,
        PhoneGapSettings.PHONEGAP_FOLDERS_PLUGINS,
        PhoneGapSettings.PHONEGAP_FOLDERS_WWW };

    moduleList.setListData(PhoneGapFolders);
  }

  public void updateStep() {

  }

  @Override
  public void updateDataModel() {

  }

  @Override
  public JComponent getComponent() {
    return component;
  }
}
