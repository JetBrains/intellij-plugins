package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.browsers.BrowserSpecificSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.chrome.ChromeSettings;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.WebProjectGenerator;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class DartGeneratorPeer implements WebProjectGenerator.GeneratorPeer<DartProjectWizardData> {
  private JPanel myMainPanel;
  private TextFieldWithBrowseButton mySdkPathTextWithBrowse;
  private JBLabel myVersionLabel;

  private JPanel myDartiumSettingsPanel;
  private TextFieldWithBrowseButton myDartiumPathTextWithBrowse;
  private JButton myDartiumSettingsButton;
  private JBCheckBox myCheckedModeCheckBox;

  private ChromeSettings myDartiumSettingsCurrent;

  public DartGeneratorPeer() {
    // set initial values before initDartSdkAndDartiumControls() because listeners should not be triggered on initialization

    final Library[] libraries = ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel().getLibraries();
    final DartSdk sdkInitial = DartSdk.findDartSdkAmongGlobalLibs(libraries);
    mySdkPathTextWithBrowse.setText(sdkInitial == null ? "" : FileUtil.toSystemDependentName(sdkInitial.getHomePath()));

    final WebBrowser dartiumInitial = DartiumUtil.getDartiumBrowser();
    myDartiumSettingsCurrent = new ChromeSettings();
    if (dartiumInitial != null) {
      final BrowserSpecificSettings browserSpecificSettings = dartiumInitial.getSpecificSettings();
      if (browserSpecificSettings instanceof ChromeSettings) {
        myDartiumSettingsCurrent = (ChromeSettings)browserSpecificSettings.clone();
      }
    }

    myDartiumPathTextWithBrowse.setText(dartiumInitial == null
                                        ? ""
                                        : FileUtilRt.toSystemDependentName(StringUtil.notNullize(dartiumInitial.getPath())));


    // now setup controls
    DartSdkUtil.initDartSdkAndDartiumControls(null, mySdkPathTextWithBrowse, myVersionLabel, myDartiumPathTextWithBrowse,
                                              new Computable.PredefinedValueComputable<ChromeSettings>(myDartiumSettingsCurrent),
                                              myDartiumSettingsButton, myCheckedModeCheckBox,
                                              new Computable.PredefinedValueComputable<Boolean>(false));

    final boolean checkedMode = dartiumInitial == null || DartiumUtil.isCheckedMode(myDartiumSettingsCurrent.getEnvironmentVariables());
    myCheckedModeCheckBox.setSelected(checkedMode);
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myMainPanel;
  }

  @Override
  public void buildUI(final @NotNull SettingsStep settingsStep) {
    settingsStep.addSettingsField(DartBundle.message("dart.sdk.path.label"), mySdkPathTextWithBrowse);
    settingsStep.addSettingsField(DartBundle.message("version.label"), myVersionLabel);
    settingsStep.addSettingsField(DartBundle.message("dartium.path.label"), myDartiumSettingsPanel);
    settingsStep.addSettingsField("", myCheckedModeCheckBox);
  }

  @NotNull
  @Override
  public DartProjectWizardData getSettings() {
    final String sdkPath = FileUtil.toSystemIndependentName(mySdkPathTextWithBrowse.getText().trim());
    final String dartiumPath = FileUtil.toSystemIndependentName(myDartiumPathTextWithBrowse.getText().trim());
    return new DartProjectWizardData(sdkPath, dartiumPath, myDartiumSettingsCurrent);
  }

  @Nullable
  @Override
  public ValidationInfo validate() {
    // invalid Dartium path is not a blocking error
    final String message = DartSdkUtil.getErrorMessageIfWrongSdkRootPath(mySdkPathTextWithBrowse.getText().trim());
    return message == null ? null : new ValidationInfo(message, mySdkPathTextWithBrowse);
  }

  @Override
  public boolean isBackgroundJobRunning() {
    return false;
  }

  @Override
  public void addSettingsStateListener(final @NotNull WebProjectGenerator.SettingsStateListener stateListener) {
    // invalid Dartium path is not a blocking error
    mySdkPathTextWithBrowse.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        stateListener.stateChanged(validate() == null);
      }
    });
  }
}
