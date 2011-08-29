package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.SdkEntry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EventDispatcher;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

/**
 * @author ksafonov
 */
public class FlexSdkChooserPanel {

  private final Project myProject;

  private JLabel mySdkLabel;
  private SdkPathCombo mySdkPathCombo;
  private JButton myEditButton;
  private JLabel myInfoLabel;
  @SuppressWarnings("UnusedDeclaration") private JPanel myContentPane;

  private final EventDispatcher<ChangeListener> myEventDispatcher;
  @Nullable
  private SdkEntry mySdk;
  private boolean myMute;

  public FlexSdkChooserPanel(Project project) {
    myProject = project;
    myEventDispatcher = EventDispatcher.create(ChangeListener.class);
    mySdkLabel.setLabelFor(mySdkPathCombo.getChildComponent());

    mySdkPathCombo.addListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (myMute) {
          return;
        }
        updateSdkFromPath();
        myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(FlexSdkChooserPanel.this));
      }
    });

    // TODO edit button text
    myEditButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        editSdk();
      }
    });
  }

  private void editSdk() {
    //if (myWorkingSdk == null) {
    //  Sdk existingSdk = findExistingSdk(getSdkPath(), FlexIdeUtils.getSdkType());
    //  if (existingSdk != null) {
    //    try {
    //      myWorkingSdk = (ProjectJdkImpl)existingSdk.clone();
    //    }
    //    catch (CloneNotSupportedException ignored) {
    //    }
    //  }
    //  else {
    //    myWorkingSdk = createSdk(getSdkPath());
    //  }
    //}
    //
    //JdkConfigurable c = new JdkConfigurable(myWorkingSdk, mySdksModel, null, new History(EMPTY_NAVIGATOR), myProject) {
    //  @Override
    //  protected SdkEditor createSdkEditor(ProjectSdksModel sdksModel, History history, ProjectJdkImpl projectJdk) {
    //    return new SdkEditor(sdksModel, history, projectJdk) {
    //      @Override
    //      protected boolean showTabForType(OrderRootType type) {
    //        return type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
    //      }
    //
    //      @Override
    //      protected TextFieldWithBrowseButton createHomeComponent() {
    //        TextFieldWithBrowseButton c = new TextFieldWithBrowseButton();
    //        c.getButton().setVisible(false);
    //        return c;
    //      }
    //    };
    //  }
    //
    //  @Override
    //  public String getDisplayName() {
    //    return FlexIdeUtils.getSdkType().getPresentableName();
    //  }
    //
    //  @Override
    //  public void updateName() {
    //    // ignore
    //  }
    //};
    //c.setNameFieldShown(false);
    //boolean modified = ShowSettingsUtil.getInstance().editConfigurable(myProject, c);
    //if (modified) {
    //  mySdkRootsModified = true;
    //}
  }

  private void updateSdkFromPath() {
    String homePath = getSdkPath();
    if (StringUtil.isEmpty(homePath) || !FlexIdeUtils.getSdkType().isValidSdkHome(homePath)) {
      mySdk = null;
    }
    else {
      mySdk = new SdkEntry();
      mySdk.setHomePath(homePath);
    }
    updateInfoLabel();
  }

  private void updateInfoLabel() {
    if (mySdk == null) {
      if (StringUtil.isEmpty(getSdkPath())) {
        myInfoLabel.setText("");
      }
      else {
        myInfoLabel.setText("SDK not found");
      }
    }
    else {
      myInfoLabel.setText("");
    }
  }

  private String getSdkPath() {
    return mySdkPathCombo.getText();
  }

  public void addListener(ChangeListener listener) {
    myEventDispatcher.addListener(listener);
  }

  public void removeListener(ChangeListener listener) {
    myEventDispatcher.removeListener(listener);
  }

  public void reset(@Nullable SdkEntry sdk) {
    mySdk = sdk != null ? sdk.getCopy() : null;
    String sdkHome = mySdk != null ? FileUtil.toSystemDependentName(StringUtil.notNullize(sdk.getHomePath())) : "";
    myMute = true;
    try {
      mySdkPathCombo.setText(sdkHome);
    }
    finally {
      myMute = false;
    }
    updateInfoLabel();
  }

  @Nullable
  public SdkEntry getCurrentSdk() {
    return mySdk;
  }

  private void createUIComponents() {
    mySdkPathCombo = new SdkPathCombo(myProject, FlexIdeUtils.getSdkType(), "flex.sdk.combo");
  }
}
