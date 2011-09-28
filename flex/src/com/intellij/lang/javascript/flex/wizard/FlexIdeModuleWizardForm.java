package com.intellij.lang.javascript.flex.wizard;

import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeModuleStructureExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdkPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
  private FlexSdkPanel myFlexSdkPanel;

  private JCheckBox mySampleAppCheckBox;
  private JTextField mySampleAppTextField;
  private JCheckBox myHtmlWrapperCheckBox;
  private JCheckBox myEnableHistoryCheckBox;
  private JCheckBox myCheckPlayerVersionCheckBox;
  private JCheckBox myExpressInstallCheckBox;

  private FlexProjectConfigurationEditor myFlexConfigEditor;
  private LibraryTableBase.ModifiableModelEx myGlobalLibrariesModifiableModel;

  private boolean myClassNameChangedByUser;
  private boolean mySettingModuleNameFromCode;
  private boolean myNeedToDisposeFlexEditor = false;
  private Disposable myDisposable;

  public FlexIdeModuleWizardForm() {
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

    myFlexSdkPanel.addListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        BCUtils.updateAvailableTargetPlayers(myFlexSdkPanel, myTargetPlayerCombo);
      }
    }, myDisposable);

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
    myFlexSdkPanel.reset();
    myFlexSdkPanel.setNotNullCurrentSdkIfPossible();
    mySampleAppCheckBox.setSelected(true);
    onModuleNameChanged(moduleName);
    myHtmlWrapperCheckBox.setSelected(true);

    // todo support
    myHtmlWrapperCheckBox.setVisible(false);
    myEnableHistoryCheckBox.setVisible(false);
    myCheckPlayerVersionCheckBox.setVisible(false);
    myExpressInstallCheckBox.setVisible(false);

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

    if (createHtmlWrapperWasEnabled != myHtmlWrapperCheckBox.isEnabled()) {
      myHtmlWrapperCheckBox.setSelected(myHtmlWrapperCheckBox.isEnabled());
    }

    mySampleAppTextField.setEnabled(mySampleAppCheckBox.isEnabled() && mySampleAppCheckBox.isSelected());
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
      myFlexConfigEditor.dispose();
      myFlexConfigEditor = null;
    }

    //myFlexSdkPanel disposed as a child of myDisposable
    Disposer.dispose(myDisposable);
  }

  private void createUIComponents() {
    final FlexProjectConfigurationEditor currentFlexEditor =
      FlexIdeModuleStructureExtension.getInstance().getConfigurator().getConfigEditor();
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

        public LibraryTableBase.ModifiableModelEx getGlobalLibrariesModifiableModel() {
          if (myGlobalLibrariesModifiableModel == null) {
            myGlobalLibrariesModifiableModel =
              (LibraryTableBase.ModifiableModelEx)ApplicationLibraryTable.getApplicationTable().getModifiableModel();
          }
          return myGlobalLibrariesModifiableModel;
        }
      };

      myFlexConfigEditor = new FlexProjectConfigurationEditor(null, provider);
    }

    myFlexSdkPanel = new FlexSdkPanel(myFlexConfigEditor);
    myDisposable = Disposer.newDisposable();
    Disposer.register(myDisposable, myFlexSdkPanel);
    myFlexSdkPanel.setSdkLabelVisible(false);
    myFlexSdkPanel.setEditButtonVisible(false);
  }

  public void applyTo(final FlexIdeModuleBuilder moduleBuilder) {
    moduleBuilder.setTargetPlatform((TargetPlatform)myTargetPlatformCombo.getSelectedItem());
    moduleBuilder.setPureActionScript(myPureActionScriptCheckBox.isSelected());
    moduleBuilder.setOutputType((OutputType)myOutputTypeCombo.getSelectedItem());
    moduleBuilder.setFlexSdk(myFlexSdkPanel.getCurrentSdk());
    moduleBuilder.setTargetPlayer((String)myTargetPlayerCombo.getSelectedItem());
    moduleBuilder.setCreateSampleApp(mySampleAppCheckBox.isEnabled() && mySampleAppCheckBox.isSelected());
    moduleBuilder.setSampleAppName(mySampleAppTextField.getText().trim());
    moduleBuilder.setCreateHtmlWrapperTemplate(myHtmlWrapperCheckBox.isEnabled() && myHtmlWrapperCheckBox.isSelected());
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
    final FlexSdk sdk = myFlexSdkPanel.getCurrentSdk();
    if (sdk == null) {
      throw new ConfigurationException("Flex SDK is not set");
    }
    // todo others

    return true;
  }
}
