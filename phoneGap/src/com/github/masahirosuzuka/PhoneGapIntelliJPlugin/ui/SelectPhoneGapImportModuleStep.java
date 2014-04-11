package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui;

import com.intellij.codeInspection.ModifiableModel;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.projectImport.ProjectImportWizardStep;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Masahiro Suzuka on 2014/04/10.
 */
public class SelectPhoneGapImportModuleStep extends ProjectImportWizardStep {

  private JPanel component;
  private TextFieldWithBrowseButton projectRootPathField;
  private JList moduleList;

  public String projectRootPath;

  public SelectPhoneGapImportModuleStep(final WizardContext context) {
    super(context);
    projectRootPathField.setText(context.getProjectFileDirectory());
    projectRootPathField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        projectRootPath = projectRootPathField.getText();
      }
    });

    //Fix me!!
    String[] list = new String[]{".cordova", "hooks", "marges", "platforms", "plugins", "www"};
    moduleList.setListData(list);
  }

  public void updateStep() {

  }

  @Override
  public void updateDataModel() {
    //System.out.println("Finish");
  }

  @Override
  public JComponent getComponent() {
    return component;
  }
}
