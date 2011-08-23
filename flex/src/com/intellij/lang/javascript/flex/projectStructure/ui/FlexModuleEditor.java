package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author ksafonov
 */
public class FlexModuleEditor implements ModuleConfigurationEditor {
  private JPanel myContentPane;
  private SdkPathCombo mySdkPathCombo;
  private JLabel mySdkLabel;
  private JLabel myInfoLabel;
  private JPanel myContentEntriesHolder;
  private final ModuleConfigurationState myState;

  private final SdkModel.Listener myListener;
  private final ProjectSdksModel mySdksModel;
  private String myInitialSdkHome;
  private final CommonContentEntriesEditor myEntriesEditor;

  public FlexModuleEditor(ModuleConfigurationState state) {
    myState = state;
    mySdksModel = ProjectStructureConfigurable.getInstance(myState.getProject()).getProjectJdksModel();
    mySdkLabel.setLabelFor(mySdkPathCombo.getChildComponent());
    myEntriesEditor = new CommonContentEntriesEditor(state.getRootModel().getModule().getName(), state, true, true);
    myEntriesEditor.getComponent().setBorder(new EmptyBorder(10, 0, 0, 0));
    myContentEntriesHolder.add(myEntriesEditor.createComponent(), BorderLayout.CENTER);

    myListener = new SdkModel.Listener() {
      @Override
      public void sdkAdded(Sdk sdk) {
      }

      @Override
      public void beforeSdkRemove(Sdk sdk) {
        if (!isModified() && findExistingSdk(mySdkPathCombo.getText(), FlexIdeUtils.getSdkType()) == null) {
          mySdkPathCombo.setText("");
          myInitialSdkHome = "";
        }
      }

      @Override
      public void sdkChanged(Sdk sdk, String previousName) {
      }

      @Override
      public void sdkHomeSelected(Sdk sdk, String newSdkHome) {
      }
    };
    mySdksModel.addListener(myListener);
    mySdkPathCombo.addListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateInfoLabel();
      }
    });
  }

  private void updateInfoLabel() {
    final String sdkPath = mySdkPathCombo.getText();
    if (StringUtil.isEmpty(sdkPath)) {
      myInfoLabel.setText("");
    }
    else {
      if (!FlexIdeUtils.getSdkType().isValidSdkHome(sdkPath)) {
        myInfoLabel.setText("SDK not found");
      }
      else {
        String flexVersion = FlexSdkUtils.readFlexSdkVersion(LocalFileSystem.getInstance().findFileByPath(sdkPath));
        myInfoLabel.setText("Flex SDK version: " + flexVersion);
        // TODO AIR version
      }
    }
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Flex";
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getHelpTopic() {
    return null; // TODO
  }

  @Override
  public JComponent createComponent() {
    return myContentPane;
  }

  @Override
  public boolean isModified() {
    return !pathsEqual(myInitialSdkHome, mySdkPathCombo.getText()) || myEntriesEditor.isModified();
  }

  private static boolean pathsEqual(@Nullable String path1, @Nullable String path2) {
    path1 = StringUtil.trimEnd(FileUtil.toSystemIndependentName(StringUtil.notNullize(path1)), "/");
    path2 = StringUtil.trimEnd(FileUtil.toSystemIndependentName(StringUtil.notNullize(path2)), "/");
    return Comparing.equal(path1, path2, SystemInfo.isFileSystemCaseSensitive);
  }

  @Override
  public void reset() {
    final Sdk sdk = myState.getRootModel().getSdk();
    String sdkHome = sdk != null ? FileUtil.toSystemDependentName(StringUtil.notNullize(sdk.getHomePath())) : "";
    mySdkPathCombo.setText(sdkHome);
    myInitialSdkHome = sdkHome;
    updateInfoLabel();
    myEntriesEditor.reset();
  }

  @Override
  public void saveData() {
  }

  @Override
  public void moduleStateChanged() {
    // TODO
  }

  @Override
  public void apply() throws ConfigurationException {
    if (!pathsEqual(myInitialSdkHome, mySdkPathCombo.getText())) {
      String newSdkHome = FileUtil.toSystemIndependentName(mySdkPathCombo.getText());
      newSdkHome = StringUtil.trimEnd(newSdkHome, "/");
      final SdkType sdkType = FlexIdeUtils.getSdkType();
      if (StringUtil.isEmpty(newSdkHome) || !sdkType.isValidSdkHome(newSdkHome)) {
        myState.getRootModel().setSdk(null);
      }
      else {
        Sdk existingSdk = findExistingSdk(newSdkHome, sdkType);

        if (existingSdk == null) {
          String newSdkName =
            SdkConfigurationUtil.createUniqueSdkName(sdkType, newSdkHome, mySdksModel.getProjectSdks().values());
          final ProjectJdkImpl newSdk = new ProjectJdkImpl(newSdkName, sdkType);
          newSdk.setHomePath(newSdkHome);

          if (!sdkType.setupSdkPaths(newSdk, mySdksModel)) return;
          JdkListConfigurable.getInstance(myState.getProject()).addJdkNode(newSdk, false);
          mySdksModel.doAdd(newSdk, null);
          myState.getRootModel().setSdk(newSdk);
        }
        else {
          if (myState.getRootModel().getSdk() != existingSdk) {
            myState.getRootModel().setSdk(existingSdk);
          }
        }
      }
      mySdkPathCombo.saveHistory();
    }

    if (myEntriesEditor.isModified()) {
      myEntriesEditor.apply();
    }
  }

  private Sdk findExistingSdk(String newSdkHome, SdkType sdkType) {
    Sdk existingSdk = null;
    for (Sdk sdk : mySdksModel.getSdks()) {
      if (sdk.getSdkType() == sdkType && pathsEqual(sdk.getHomePath(), newSdkHome)) {
        existingSdk = sdk;
        break;
      }
    }
    return existingSdk;
  }

  @Override
  public void disposeUIResources() {
    ProjectStructureConfigurable.getInstance(myState.getProject()).getProjectJdksModel().removeListener(myListener);
    myEntriesEditor.disposeUIResources();
  }

  private void createUIComponents() {
    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
    descriptor.setTitle(FlexBundle.message("select.flex.sdk"));
    descriptor.setDescription(FlexBundle.message("select.sdk.description"));
    mySdkPathCombo = new SdkPathCombo(myState.getProject(), FlexIdeUtils.getSdkType(), "flex.sdk.combo");
  }
}
