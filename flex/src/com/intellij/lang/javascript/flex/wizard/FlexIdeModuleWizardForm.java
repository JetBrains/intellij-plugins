package com.intellij.lang.javascript.flex.wizard;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor.ProjectModifiableModelProvider;

public class FlexIdeModuleWizardForm {

  private JPanel myMainPanel;
  private JComboBox myTargetPlatformCombo;
  private NonFocusableCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;

  private JLabel myTargetPlayerLabel;
  private JComboBox myTargetPlayerCombo;

  private JCheckBox mySampleAppCheckBox;
  private JTextField mySampleAppTextField;
  private JCheckBox myHtmlWrapperCheckBox;
  private JCheckBox myEnableHistoryCheckBox;
  private JCheckBox myCheckPlayerVersionCheckBox;
  private JCheckBox myExpressInstallCheckBox;
  private FlexSdkComboBoxWithBrowseButton mySdkCombo;
  private JLabel mySdkLabel;

  private FlexProjectConfigurationEditor myFlexConfigEditor;
  private LibraryTableBase.ModifiableModelEx myGlobalLibrariesModifiableModel;

  private boolean myClassNameChangedByUser;
  private boolean mySettingModuleNameFromCode;
  private boolean myNeedToDisposeFlexEditor;

  public FlexIdeModuleWizardForm() {
    mySdkLabel.setLabelFor(mySdkCombo.getChildComponent());
    TargetPlatform.initCombo(myTargetPlatformCombo);
    OutputType.initCombo(myOutputTypeCombo);

    final ActionListener listener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myTargetPlatformCombo.addActionListener(listener);
    myOutputTypeCombo.addActionListener(listener);
    mySampleAppCheckBox.addActionListener(listener);
    myHtmlWrapperCheckBox.addActionListener(listener);
    myCheckPlayerVersionCheckBox.addActionListener(listener);

    mySdkCombo.addComboboxListener(new FlexSdkComboBoxWithBrowseButton.Listener() {
      public void stateChanged() {
        BCUtils.updateAvailableTargetPlayers(mySdkCombo.getSelectedSdk(), myTargetPlayerCombo);
      }
    });

    myPureActionScriptCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final String sampleApp = mySampleAppTextField.getText().trim();
        if (sampleApp.endsWith(".as") || sampleApp.endsWith("mxml")) {
          mySettingModuleNameFromCode = true;
          mySampleAppTextField
            .setText(FileUtil.getNameWithoutExtension(sampleApp) + (myPureActionScriptCheckBox.isSelected() ? ".as" : ".mxml"));
          mySettingModuleNameFromCode = false;
        }
      }
    });

    mySampleAppTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        if (!mySettingModuleNameFromCode) {
          myClassNameChangedByUser = true;
        }
      }
    });
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void setEnabled(final boolean enabled) {
    UIUtil.setEnabled(myMainPanel, enabled, true);
    if (enabled) {
      updateControls();
    }
  }

  public void reset(final String moduleName) {
    myTargetPlayerCombo.setSelectedItem(TargetPlatform.Web);
    myPureActionScriptCheckBox.setSelected(false);
    myOutputTypeCombo.setSelectedItem(OutputType.Application);
    BCUtils.updateAvailableTargetPlayers(mySdkCombo.getSelectedSdk(), myTargetPlayerCombo);
    mySampleAppCheckBox.setSelected(true);
    onModuleNameChanged(moduleName);
    myHtmlWrapperCheckBox.setSelected(true);
    myEnableHistoryCheckBox.setSelected(true);
    myCheckPlayerVersionCheckBox.setSelected(true);
    myExpressInstallCheckBox.setSelected(true);

    myClassNameChangedByUser = false;
    updateControls();
  }

  private void updateControls() {
    final boolean web = myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Web;
    final boolean app = myOutputTypeCombo.getSelectedItem() == OutputType.Application;

    final boolean createMainClassWasEnabled = mySampleAppCheckBox.isEnabled();
    final boolean createHtmlWrapperWasEnabled = myHtmlWrapperCheckBox.isEnabled();

    myTargetPlayerLabel.setEnabled(web);
    myTargetPlayerCombo.setEnabled(web);
    mySampleAppCheckBox.setEnabled(app);
    mySampleAppTextField.setEnabled(app);
    myHtmlWrapperCheckBox.setEnabled(web && app);

    if (createMainClassWasEnabled != mySampleAppCheckBox.isEnabled()) {
      mySampleAppCheckBox.setSelected(mySampleAppCheckBox.isEnabled());
    }

    mySampleAppTextField.setEnabled(mySampleAppCheckBox.isEnabled() && mySampleAppCheckBox.isSelected());

    if (createHtmlWrapperWasEnabled != myHtmlWrapperCheckBox.isEnabled()) {
      myHtmlWrapperCheckBox.setSelected(myHtmlWrapperCheckBox.isEnabled());
    }

    myEnableHistoryCheckBox.setEnabled(myHtmlWrapperCheckBox.isSelected() && web && app);
    myCheckPlayerVersionCheckBox.setEnabled(myHtmlWrapperCheckBox.isSelected() && web && app);
    myExpressInstallCheckBox.setEnabled(myHtmlWrapperCheckBox.isSelected() && myCheckPlayerVersionCheckBox.isSelected() && web && app);
  }

  public void disposeUIResources() {
    if (myGlobalLibrariesModifiableModel != null) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          myGlobalLibrariesModifiableModel.commit();
          myGlobalLibrariesModifiableModel = null;
        }
      });
    }

    if (myNeedToDisposeFlexEditor) {
      Disposer.dispose(myFlexConfigEditor);
      myFlexConfigEditor = null;
    }
  }

  private void createUIComponents() {
    final FlexProjectConfigurationEditor currentFlexEditor =
      FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();

    final Condition<Sdk> condition = new Condition<Sdk>() {
      public boolean value(final Sdk sdk) {
        return sdk != null && sdk.getSdkType() == FlexSdkType2.getInstance();
      }
    };

    mySdkCombo = new FlexSdkComboBoxWithBrowseButton(condition) {
      protected Sdk[] getAllSdks() {
        if (currentFlexEditor != null) {
          return FlexProjectConfigurationEditor.getEditableFlexSdks(currentFlexEditor.getProject());
        }
        else {
          return super.getAllSdks();
        }
      }
    };

    myNeedToDisposeFlexEditor = currentFlexEditor == null;

    if (currentFlexEditor != null) {
      myGlobalLibrariesModifiableModel = null;
      myFlexConfigEditor = currentFlexEditor;
    }
    else {
      final ProjectModifiableModelProvider provider = new ProjectModifiableModelProvider() {
        public Module[] getModules() {
          return new Module[0];
        }

        public ModifiableRootModel getModuleModifiableModel(final Module module) {
          assert false;
          return null;
        }

        public void addListener(final FlexIdeBCConfigurator.Listener listener, final Disposable parentDisposable) {
          // neither module nor BC are removed here
        }

        public void commitModifiableModels() throws ConfigurationException {
          assert false;
        }

        public LibraryTableBase.ModifiableModelEx getLibrariesModifiableModel(final String level) {
          if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(level)) {
            if (myGlobalLibrariesModifiableModel == null) {
              myGlobalLibrariesModifiableModel =
                (LibraryTableBase.ModifiableModelEx)ApplicationLibraryTable.getApplicationTable().getModifiableModel();
            }
            return myGlobalLibrariesModifiableModel;
          }
          else {
            throw new UnsupportedOperationException();
          }
        }

        public Sdk[] getAllSdks() {
          return FlexProjectConfigurationEditor.getPersistedFlexSdks();
        }
      };

      myFlexConfigEditor = new FlexProjectConfigurationEditor(null, provider);
    }
  }

  public void applyTo(final FlexModuleBuilder moduleBuilder) {
    moduleBuilder.setTargetPlatform((TargetPlatform)myTargetPlatformCombo.getSelectedItem());
    moduleBuilder.setPureActionScript(myPureActionScriptCheckBox.isSelected());
    moduleBuilder.setOutputType((OutputType)myOutputTypeCombo.getSelectedItem());
    moduleBuilder.setFlexSdk(mySdkCombo.getSelectedSdk());
    moduleBuilder.setTargetPlayer((String)myTargetPlayerCombo.getSelectedItem());
    moduleBuilder.setCreateSampleApp(mySampleAppCheckBox.isEnabled() && mySampleAppCheckBox.isSelected());
    moduleBuilder.setSampleAppName(mySampleAppTextField.getText().trim());
    moduleBuilder.setCreateHtmlWrapperTemplate(myHtmlWrapperCheckBox.isEnabled() && myHtmlWrapperCheckBox.isSelected());
    moduleBuilder.setHtmlWrapperTemplateParameters(myEnableHistoryCheckBox.isSelected(), myCheckPlayerVersionCheckBox.isSelected(),
                                                   myExpressInstallCheckBox.isEnabled() && myExpressInstallCheckBox.isSelected());
  }

  public void onModuleNameChanged(final String moduleName) {
    if (!myClassNameChangedByUser) {
      final StringBuilder builder = new StringBuilder();
      for (int i = 0; i < moduleName.length(); i++) {
        final char ch = moduleName.charAt(i);
        if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '_' || ch == '$') {
          builder.append(ch);
        }
      }
      if (builder.length() == 0) {
        builder.append("Main");
      }
      if (Character.isDigit(builder.charAt(0))) {
        builder.insert(0, "Main_");
      }
      builder.replace(0, 1, String.valueOf(Character.toUpperCase(builder.charAt(0))));
      builder.append(myPureActionScriptCheckBox.isSelected() ? ".as" : ".mxml");

      mySettingModuleNameFromCode = true;
      mySampleAppTextField.setText(builder.toString());
      mySettingModuleNameFromCode = false;
    }
  }

  public boolean validate() throws ConfigurationException {
    final Sdk sdk = null;//myFlexSdkPanel.getCurrentSdk();
    if (sdk == null) {
      throw new ConfigurationException("Flex SDK is not set");
    }

    if (myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Mobile &&
        StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.5") < 0) {
      throw new ConfigurationException(FlexBundle.message("sdk.does.not.support.air.mobile", sdk.getVersionString()));
    }

    if (mySampleAppCheckBox.isSelected()) {
      final String fileName = mySampleAppTextField.getText().trim();
      if (fileName.isEmpty()) {
        throw new ConfigurationException(FlexBundle.message("sample.app.name.empty"));
      }

      final String extension = FileUtil.getExtension(fileName).toLowerCase();
      if (!"mxml".equals(extension) && !"as".equals(extension)) {
        throw new ConfigurationException(FlexBundle.message("sample.app.incorrect.extension"));
      }
    }

    return true;
  }
}
