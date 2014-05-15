package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.util.PhoneGapSettings;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.projectImport.ProjectImportWizardStep;

import javax.swing.*;

/**
 * SelectPhoneGapImportModuleStep.java
 *
 * Created by Masahiro Suzuka on 2014/04/10.
 */
public class SelectPhoneGapImportModuleStep extends ProjectImportWizardStep {

  private JPanel component;
  private TextFieldWithBrowseButton projectRootPathField;
  private JList folderList;

  public SelectPhoneGapImportModuleStep(final WizardContext context) {
    super(context);
    projectRootPathField.addBrowseFolderListener("Application working dir", "Application Working dir",
        context.getProject(), new FileChooserDescriptor(false, true, false, false, false, false));
    projectRootPathField.setText(context.getProjectFileDirectory());

    //Fix me!!
    String[] phoneGapFolders = new String[]{
        PhoneGapSettings.PHONEGAP_FOLDERS_CORDOVA,
        PhoneGapSettings.PHONEGAP_FOLDERS_HOOKS,
        PhoneGapSettings.PHONEGAP_FOLDERS_MERGES,
        PhoneGapSettings.PHONEGAP_FOLDERS_NODE_MODULES + "option",
        PhoneGapSettings.PHONEGAP_FOLDERS_PLATFORMS,
        PhoneGapSettings.PHONEGAP_FOLDERS_PLUGINS,
        PhoneGapSettings.PHONEGAP_FOLDERS_WWW
    };

    folderList.setListData(phoneGapFolders);
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
