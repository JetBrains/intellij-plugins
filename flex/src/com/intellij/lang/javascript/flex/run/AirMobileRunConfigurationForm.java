package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.ExecutionBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ModulesComboboxWrapper;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.intellij.lang.javascript.flex.run.AirMobileRunnerParameters.*;

public class AirMobileRunConfigurationForm extends SettingsEditor<AirMobileRunConfiguration> {

  private final Project myProject;
  private ModulesComboboxWrapper myModulesComboboxWrapper;
  private JSClassChooserDialog.PublicInheritor myMainClassFilter;

  private JPanel myMainPanel;
  private JComboBox myModuleComboBox;

  private JRadioButton myMainClassRadioButton;
  private JRadioButton myAirDescriptorRadioButton;
  private JRadioButton myExistingPackageRadioButton;

  private JSReferenceEditor myMainClassEditor;
  private ComboboxWithBrowseButton myAirDescriptorComboWithBrowse;
  private JLabel myRootDirLabel;
  private TextFieldWithBrowseButton myRootDirTextWithBrowse;
  private TextFieldWithBrowseButton myExistingPackageTextWithBrowse;

  private JRadioButton myOnEmulatorRadioButton;
  private JRadioButton myOnAndroidDeviceRadioButton;
  private JRadioButton myOnIOSDeviceRadioButton;

  private JPanel myEmulatorScreenSizePanel;
  private JComboBox myEmulatorCombo;
  private JTextField myScreenWidth;
  private JTextField myScreenHeight;
  private JTextField myFullScreenWidth;
  private JTextField myFullScreenHeight;

  private JLabel myPackageFileNameLabel;
  private JTextField myPackageFileNameTextField;
  private JButton myPackagingOptionsButton;

  private JLabel myDebugOverLabel;
  private JPanel myDebugTransportPanel;
  private JRadioButton myDebugOverNetworkRadioButton;
  private JRadioButton myDebugOverUSBRadioButton;
  private JTextField myUsbDebugPortTextField;

  private JLabel myAdlOptionsLabel;
  private RawCommandLineEditor myAdlOptionsField;
  private FlexSdkComboBoxWithBrowseButton myDebuggerSdkCombo;

  public AirMobileRunConfigurationForm(final Project project) {
    myProject = project;
    myModulesComboboxWrapper = new ModulesComboboxWrapper(myModuleComboBox);

    myModuleComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final Module module = myModulesComboboxWrapper.getSelectedModule();
        AirRunConfigurationForm.resetAirRootDirPath(module, myRootDirTextWithBrowse);
        myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
        AirRunConfigurationForm.updateMainClassField(myProject, myModulesComboboxWrapper, myMainClassEditor, myMainClassFilter);
      }
    });

    initWhatToLaunchControls();

    myExistingPackageTextWithBrowse
      .addBrowseFolderListener("AIR Mobile Package", null, myProject, new FileChooserDescriptor(true, false, false, false, false, false) {
        @Override
        public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || "apk".equalsIgnoreCase(file.getExtension()));
        }
      });

    AirRunConfigurationForm
      .initAirDescriptorCombo(myProject, myAirDescriptorComboWithBrowse, myModulesComboboxWrapper, myRootDirTextWithBrowse);

    myRootDirTextWithBrowse.addBrowseFolderListener("AIR Application Root Directory", null, project,
                                                    new FileChooserDescriptor(false, true, false, false, false, false));
    myAdlOptionsField.setDialogCaption("AIR Debug Launcher Options");

    initEmulatorRelatedControls();

    myOnIOSDeviceRadioButton.setVisible(false); // until supported

    initPackagingOptionsButton();

    final ActionListener debugTransportListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateDebugTransportRelatedControls();
      }
    };
    myDebugOverNetworkRadioButton.addActionListener(debugTransportListener);
    myDebugOverUSBRadioButton.addActionListener(debugTransportListener);

    myUsbDebugPortTextField
      .setVisible(false); // just thought that nobody needs non-standard port, so let's UI be lighter. Remove this line on first request.

    myDebuggerSdkCombo.showModuleSdk(true);

    updateControls();
    AirRunConfigurationForm.updateMainClassField(myProject, myModulesComboboxWrapper, myMainClassEditor, myMainClassFilter);

    myPackagingOptionsButton.setVisible(false);
  }

  private void initEmulatorRelatedControls() {
    myEmulatorCombo.setModel(new DefaultComboBoxModel(AirMobileRunnerParameters.Emulator.values()));

    myEmulatorCombo.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, ((AirMobileRunnerParameters.Emulator)value).name, index, isSelected, cellHasFocus);
      }
    });

    myEmulatorCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateEmulatorRelatedControls();
      }
    });

    final ActionListener targetDeviceListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();

        if (myOnEmulatorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myEmulatorCombo, true);
        }
      }
    };

    myOnEmulatorRadioButton.addActionListener(targetDeviceListener);
    myOnAndroidDeviceRadioButton.addActionListener(targetDeviceListener);
    myOnIOSDeviceRadioButton.addActionListener(targetDeviceListener);
  }

  private void initPackagingOptionsButton() {
    /*
    myPackagingOptionsButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final MobileAirPackageOptionsDialog dialog =
          new MobileAirPackageOptionsDialog(myProject, myModulesComboboxWrapper.getSelectedModule(), getRunTarget());
        dialog.show();
        if (dialog.isOK()) {
          // todo update run config
        }
      }
    });
    */
  }

  private void initWhatToLaunchControls() {
    final ActionListener actionListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        final Component toFocus = myMainClassRadioButton.isSelected()
                                  ? myMainClassEditor.getChildComponent()
                                  : myAirDescriptorRadioButton.isSelected()
                                    ? myAirDescriptorComboWithBrowse.getComboBox()
                                    : myExistingPackageTextWithBrowse.getTextField();
        IdeFocusManager.getInstance(myProject).requestFocus(toFocus, true);
        myPackageFileNameTextField.setText(getSuggestedPackageFileName());
      }
    };
    myMainClassRadioButton.addActionListener(actionListener);
    myAirDescriptorRadioButton.addActionListener(actionListener);
    myExistingPackageRadioButton.addActionListener(actionListener);

    myMainClassEditor.addDocumentListener(new DocumentAdapter() {
      public void documentChanged(final DocumentEvent e) {
        myPackageFileNameTextField.setText(getSuggestedPackageFileName());
      }
    });

    ((JTextComponent)myAirDescriptorComboWithBrowse.getComboBox().getEditor().getEditorComponent()).getDocument().addDocumentListener(
      new com.intellij.ui.DocumentAdapter() {
        protected void textChanged(final javax.swing.event.DocumentEvent e) {
          myPackageFileNameTextField.setText(getSuggestedPackageFileName());
        }
      });
  }


  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }


  private void updateControls() {
    myMainClassEditor.setEnabled(myMainClassRadioButton.isSelected());
    myAirDescriptorComboWithBrowse.setEnabled(myAirDescriptorRadioButton.isSelected());
    myRootDirLabel.setEnabled(myAirDescriptorRadioButton.isSelected());
    myRootDirTextWithBrowse.setEnabled(myAirDescriptorRadioButton.isSelected());
    myExistingPackageTextWithBrowse.setEnabled(myExistingPackageRadioButton.isSelected());

    final boolean runOnEmulator = myOnEmulatorRadioButton.isSelected();
    myEmulatorCombo.setEnabled(runOnEmulator);
    UIUtil.setEnabled(myEmulatorScreenSizePanel, runOnEmulator, true);
    myAdlOptionsLabel.setEnabled(runOnEmulator);
    myAdlOptionsField.setEnabled(runOnEmulator);

    if (runOnEmulator) {
      updateEmulatorRelatedControls();
    }

    updatePackagingRelatedControls();
    updateDebugTransportRelatedControls();
  }

  private void updateEmulatorRelatedControls() {
    final AirMobileRunnerParameters.Emulator emulator = (AirMobileRunnerParameters.Emulator)myEmulatorCombo.getSelectedItem();
    if (emulator.adlAlias == null) {
      myScreenWidth.setEditable(true);
      myScreenHeight.setEditable(true);
      myFullScreenWidth.setEditable(true);
      myFullScreenHeight.setEditable(true);
    }
    else {
      myScreenWidth.setEditable(false);
      myScreenHeight.setEditable(false);
      myFullScreenWidth.setEditable(false);
      myFullScreenHeight.setEditable(false);
      myScreenWidth.setText(String.valueOf(emulator.screenWidth));
      myScreenHeight.setText(String.valueOf(emulator.screenHeight));
      myFullScreenWidth.setText(String.valueOf(emulator.fullScreenWidth));
      myFullScreenHeight.setText(String.valueOf(emulator.fullScreenHeight));
    }
  }

  private void updatePackagingRelatedControls() {
    final boolean enabled = (myMainClassRadioButton.isSelected() || myAirDescriptorRadioButton.isSelected())
                            && !myOnEmulatorRadioButton.isSelected();
    myPackageFileNameLabel.setEnabled(enabled);
    myPackageFileNameTextField.setEnabled(enabled);
    myPackagingOptionsButton.setEnabled(enabled);

    if (enabled) {
      final String fileNameLowercased = myPackageFileNameTextField.getText().toLowerCase();
      // todo update extension apk/ipa
    }
  }

  private void updateDebugTransportRelatedControls() {
    final boolean enabled = !myOnEmulatorRadioButton.isSelected();
    myDebugOverLabel.setEnabled(enabled);
    UIUtil.setEnabled(myDebugTransportPanel, enabled, true);
    if (enabled) {
      myUsbDebugPortTextField.setEnabled(myDebugOverUSBRadioButton.isSelected());
    }
  }

  protected void resetEditorFrom(final AirMobileRunConfiguration config) {
    final AirMobileRunnerParameters params = config.getRunnerParameters();

    myModulesComboboxWrapper.configure(myProject, params.getModuleName());

    myMainClassRadioButton.setSelected(params.getAirMobileRunMode() == AirMobileRunMode.MainClass);
    myMainClassEditor.setText(params.getMainClassName());

    myAirDescriptorRadioButton.setSelected(params.getAirMobileRunMode() == AirMobileRunMode.AppDescriptor);
    myAirDescriptorComboWithBrowse.getComboBox().getEditor().setItem(FileUtil.toSystemDependentName(params.getAirDescriptorPath()));
    myRootDirTextWithBrowse.setText(FileUtil.toSystemDependentName(params.getAirRootDirPath()));

    myExistingPackageRadioButton.setSelected(params.getAirMobileRunMode() == AirMobileRunMode.ExistingPackage);
    myExistingPackageTextWithBrowse.setText(FileUtil.toSystemDependentName(params.getExistingPackagePath()));

    myOnEmulatorRadioButton.setSelected(params.getAirMobileRunTarget() == AirMobileRunTarget.Emulator);
    myEmulatorCombo.setSelectedItem(params.getEmulator());
    if (params.getEmulator().adlAlias == null) {
      myScreenWidth.setText(String.valueOf(params.getScreenWidth()));
      myScreenHeight.setText(String.valueOf(params.getScreenHeight()));
      myFullScreenWidth.setText(String.valueOf(params.getFullScreenWidth()));
      myFullScreenHeight.setText(String.valueOf(params.getFullScreenHeight()));
    }

    myOnAndroidDeviceRadioButton.setSelected(params.getAirMobileRunTarget() == AirMobileRunTarget.AndroidDevice);
    myOnIOSDeviceRadioButton.setSelected(params.getAirMobileRunTarget() == AirMobileRunTarget.iOSDevice);

    myPackageFileNameTextField.setText(params.getMobilePackageFileName());

    myDebugOverNetworkRadioButton.setSelected(params.getDebugTransport() == AirMobileDebugTransport.Network);
    myDebugOverUSBRadioButton.setSelected(params.getDebugTransport() == AirMobileDebugTransport.USB);
    myUsbDebugPortTextField.setText(String.valueOf(params.getUsbDebugPort()));

    myAdlOptionsField.setText(params.getAdlOptions());

    final Module module = myModulesComboboxWrapper.getSelectedModule();
    myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
    myDebuggerSdkCombo.setSelectedSdkRaw(params.getDebuggerSdkRaw());

    updateControls();
  }

  private String getSuggestedPackageFileName() {
    if (myMainClassRadioButton.isSelected()) {
      return myMainClassEditor.getText().trim() + ".apk";
    }
    else if (myAirDescriptorRadioButton.isSelected()) {
      final String descriptorPath = getAirDescriptorPath();
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(descriptorPath);
      final String fileName = descriptorPath.substring(descriptorPath.lastIndexOf('/') + 1);
      return file == null || file.isDirectory()
             ? FileUtil.getNameWithoutExtension(fileName) + ".apk"
             : MobileAirUtil.getAppName(file) + ".apk";
    }

    return "";
  }

  protected void applyEditorTo(final AirMobileRunConfiguration config) throws ConfigurationException {
    final AirMobileRunnerParameters params = config.getRunnerParameters();

    params.setModuleName(myModulesComboboxWrapper.getSelectedText());
    params.setAirMobileRunTarget(getRunTarget());

    final AirMobileRunnerParameters.Emulator emulator = (AirMobileRunnerParameters.Emulator)myEmulatorCombo.getSelectedItem();
    params.setEmulator(emulator);
    params.setExistingPackagePath(FileUtil.toSystemIndependentName(myExistingPackageTextWithBrowse.getText().trim()));

    if (emulator.adlAlias == null) {
      try {
        params.setScreenWidth(Integer.parseInt(myScreenWidth.getText()));
        params.setScreenHeight(Integer.parseInt(myScreenHeight.getText()));
        params.setFullScreenWidth(Integer.parseInt(myFullScreenWidth.getText()));
        params.setFullScreenHeight(Integer.parseInt(myFullScreenHeight.getText()));
      }
      catch (NumberFormatException e) {/**/}
    }

    params.setMobilePackageFileName(myPackageFileNameTextField.getText().trim());

    params.setDebugTransport(myDebugOverNetworkRadioButton.isSelected()
                             ? AirMobileDebugTransport.Network
                             : AirMobileDebugTransport.USB);
    try {
      final int port = Integer.parseInt(myUsbDebugPortTextField.getText().trim());
      if (port > 0 && port < 65535) {
        params.setUsbDebugPort(port);
      }
    }
    catch (NumberFormatException ignore) {/*ignore*/}

    params.setAirMobileRunMode(myMainClassRadioButton.isSelected()
                               ? AirMobileRunMode.MainClass
                               : myAirDescriptorRadioButton.isSelected()
                                 ? AirMobileRunMode.AppDescriptor
                                 : AirMobileRunMode.ExistingPackage);
    params.setAirDescriptorPath(getAirDescriptorPath());
    params.setAirRootDirPath(FileUtil.toSystemIndependentName(myRootDirTextWithBrowse.getText().trim()));
    params.setMainClassName(myMainClassEditor.getText().trim());
    params.setAdlOptions(myAdlOptionsField.getText().trim());
    params.setDebuggerSdkRaw(myDebuggerSdkCombo.getSelectedSdkRaw());
  }

  private String getAirDescriptorPath() {
    return FileUtil.toSystemIndependentName((String)myAirDescriptorComboWithBrowse.getComboBox().getEditor().getItem()).trim();
  }

  private AirMobileRunTarget getRunTarget() {
    return myOnEmulatorRadioButton.isSelected()
           ? AirMobileRunTarget.Emulator
           : myOnAndroidDeviceRadioButton.isSelected()
             ? AirMobileRunTarget.AndroidDevice
             : AirMobileRunTarget.iOSDevice;
  }

  protected void disposeEditor() {
  }

  private void createUIComponents() {
    myDebuggerSdkCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_RELATED_SDK);
    myMainClassFilter = new JSClassChooserDialog.PublicInheritor(myProject, FlexCompilerSettingsEditor.SPRITE_CLASS_NAME, null, true);
    myMainClassEditor =
      JSReferenceEditor.forClassName("", myProject, null, GlobalSearchScope.EMPTY_SCOPE, null, myMainClassFilter, ExecutionBundle.message(
        "choose.main.class.dialog.title"));
  }
}
