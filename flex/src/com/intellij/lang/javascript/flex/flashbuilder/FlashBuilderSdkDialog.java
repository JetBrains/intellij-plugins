package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdkPanel;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class FlashBuilderSdkDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JLabel myLabel;
  private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;
  private JPanel myFlexSdkPanelHolder;
  private FlexSdkPanel myFlexSdkPanel;

  public FlashBuilderSdkDialog(final Project project,
                               final @Nullable FlexProjectConfigurationEditor flexConfigEditor,
                               final int projectsCount) {
    super(project);

    final boolean flexIde = PlatformUtils.isFlexIde();
    assert flexIde == (flexConfigEditor != null);

    myLabel.setText(PlatformUtils.isFlexIde()
                    ? FlexBundle.message("sdk.home.for.imported.projects", projectsCount)
                    : FlexBundle.message("sdk.for.imported.projects", projectsCount));
    myFlexSdkComboWithBrowse.setVisible(!flexIde);

    if (flexIde) {
      myFlexSdkPanel = new FlexSdkPanel(flexConfigEditor);
      Disposer.register(myDisposable, myFlexSdkPanel);
      myFlexSdkPanel.setSdkLabelVisible(false);
      myFlexSdkPanel.setEditButtonVisible(false);
      myFlexSdkPanel.reset();
      myFlexSdkPanel.setNotNullCurrentSdkIfPossible();

      myFlexSdkPanelHolder.add(myFlexSdkPanel.getContentPane(), BorderLayout.CENTER);
    }

    setTitle(FlexBundle.message("flash.builder.project.import.title"));
    init();
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction()};
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Nullable
  public Sdk getSdk() {
    assert !PlatformUtils.isFlexIde();
    return myFlexSdkComboWithBrowse.getSelectedSdk();
  }

  @Nullable
  public String getSdkHome() {
    assert PlatformUtils.isFlexIde();
    final Sdk sdk = myFlexSdkPanel.getCurrentSdk();
    return sdk == null ? null : sdk.getHomePath();
  }
}
