package com.jetbrains.plugins.meteor.ide.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.jetbrains.plugins.meteor.MeteorBundle;
import com.jetbrains.plugins.meteor.MeteorUIUtil;
import com.jetbrains.plugins.meteor.ide.action.MeteorImportPackagesAsExternalLibAction.CodeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class MeteorImportPackagesDialog extends DialogWrapper {

  private final Project myProject;
  @NlsSafe
  private final String myDefaultPath;
  private final Collection<? extends CodeType> myDefaultCodes;
  private JBCheckBox myImportServer;
  private JBCheckBox myImportClient;
  private JBCheckBox myImportCordova;
  private JBCheckBox myImportNpm;

  private TextFieldWithBrowseButton myPathField;

  public MeteorImportPackagesDialog(@NotNull Project project,
                                    @Nullable String defaultPath,
                                    @NotNull Collection<? extends CodeType> defaultCodes) {
    super(project);
    myProject = project;
    myDefaultPath = defaultPath;
    myDefaultCodes = defaultCodes;

    setTitle(MeteorBundle.message("action.meteor.import.packages.dialog.title"));
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPathField = MeteorUIUtil.createPackagesField(myProject);
    myImportClient = new JBCheckBox(MeteorBundle.message("checkbox.import.client"));
    myImportServer = new JBCheckBox(MeteorBundle.message("checkbox.import.server"));
    myImportCordova = new JBCheckBox(MeteorBundle.message("checkbox.import.cordova"));
    myImportNpm= new JBCheckBox(MeteorBundle.message("checkbox.import.npm"));

    for (CodeType code : myDefaultCodes) {
      JBCheckBox currentSelected = null;
      switch (code) {
        case CLIENT:
          currentSelected = myImportClient;
          break;
        case CORDOVA:
          currentSelected = myImportCordova;
          break;
        case SERVER:
          currentSelected = myImportServer;
          break;
        case NPM:
          currentSelected = myImportNpm;
          break;
        default:
      }

      if (currentSelected != null) {
        currentSelected.setSelected(true);
      }
    }

    FormBuilder builder =
      FormBuilder
        .createFormBuilder()
        .addLabeledComponent(MeteorBundle.message("action.meteor.import.packages.dialog.path.title"), myPathField)
        .addComponent(myImportClient)
        .addComponent(myImportNpm)
        .addComponent(myImportServer)
        .addComponent(myImportCordova);


    myPathField.setText(myDefaultPath);
    JPanel panel = builder.getPanel();
    panel.setPreferredSize(JBUI.size(600, 40));

    return panel;
  }

  public String getPath() {
    return myPathField.getText();
  }

  public Collection<CodeType> getCodeTypes() {
    Collection<CodeType> result = new ArrayList<>();
    if (myImportClient.isSelected()) result.add(CodeType.CLIENT);
    if (myImportServer.isSelected()) result.add(CodeType.SERVER);
    if (myImportCordova.isSelected()) result.add(CodeType.CORDOVA);
    if (myImportNpm.isSelected()) result.add(CodeType.NPM);

    return result;
  }


  @Override
  @Nullable
  protected ValidationInfo doValidate() {
    if (StringUtil.isEmptyOrSpaces(myPathField.getText())) {
      return new ValidationInfo(MeteorBundle.message("dialog.message.meteor.packages.folder.empty"));
    }
    return null;
  }
}
