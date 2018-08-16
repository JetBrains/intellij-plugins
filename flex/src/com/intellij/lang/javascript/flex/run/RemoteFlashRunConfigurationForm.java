package com.intellij.lang.javascript.flex.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.intellij.lang.javascript.flex.run.FlashRunnerParameters.AirMobileDebugTransport;
import static com.intellij.lang.javascript.flex.run.RemoteFlashRunnerParameters.RemoteDebugTarget;

public class RemoteFlashRunConfigurationForm extends SettingsEditor<RemoteFlashRunConfiguration> {

  private JPanel myMainPanel;
  private BCCombo myBCCombo;

  private JRadioButton myOnComputerRadioButton;
  private JRadioButton myOnAndroidDeviceRadioButton;
  private JRadioButton myOnIOSDeviceRadioButton;

  private JPanel myDeviceOptionsPanel;
  private JRadioButton myDebugOverNetworkRadioButton;
  private JRadioButton myDebugOverUSBRadioButton;
  private JTextField myUsbDebugPortTextField;

  private final Project myProject;

  public RemoteFlashRunConfigurationForm(final Project project) {
    myProject = project;

    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myOnComputerRadioButton.addActionListener(listener);
    myOnAndroidDeviceRadioButton.addActionListener(listener);
    myOnIOSDeviceRadioButton.addActionListener(listener);
    myDebugOverNetworkRadioButton.addActionListener(listener);
    myDebugOverUSBRadioButton.addActionListener(listener);
  }

  @Override
  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  @Override
  protected void resetEditorFrom(@NotNull final RemoteFlashRunConfiguration configuration) {
    final RemoteFlashRunnerParameters params = configuration.getRunnerParameters();
    myBCCombo.resetFrom(params);

    myOnComputerRadioButton.setSelected(params.getRemoteDebugTarget() == RemoteDebugTarget.Computer);
    myOnAndroidDeviceRadioButton.setSelected(params.getRemoteDebugTarget() == RemoteDebugTarget.AndroidDevice);
    myOnIOSDeviceRadioButton.setSelected(params.getRemoteDebugTarget() == RemoteDebugTarget.iOSDevice);

    myDebugOverNetworkRadioButton.setSelected(params.getDebugTransport() == AirMobileDebugTransport.Network);
    myDebugOverUSBRadioButton.setSelected(params.getDebugTransport() == AirMobileDebugTransport.USB);
    myUsbDebugPortTextField.setText(String.valueOf(params.getUsbDebugPort()));

    updateControls();
  }

  @Override
  protected void applyEditorTo(@NotNull final RemoteFlashRunConfiguration configuration) throws ConfigurationException {
    final RemoteFlashRunnerParameters params = configuration.getRunnerParameters();
    myBCCombo.applyTo(params);

    final RemoteDebugTarget remoteDebugTarget = myOnComputerRadioButton.isSelected()
                                                ? RemoteDebugTarget.Computer
                                                : myOnAndroidDeviceRadioButton.isSelected()
                                                  ? RemoteDebugTarget.AndroidDevice
                                                  : RemoteDebugTarget.iOSDevice;
    params.setRemoteDebugTarget(remoteDebugTarget);

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
  }

  @Override
  protected void disposeEditor() {
    myBCCombo.dispose();
  }

  private void updateControls() {
    UIUtil.setEnabled(myDeviceOptionsPanel, myOnAndroidDeviceRadioButton.isSelected() || myOnIOSDeviceRadioButton.isSelected(), true);
    myUsbDebugPortTextField.setEnabled(myDebugOverUSBRadioButton.isEnabled() && myDebugOverUSBRadioButton.isSelected());
  }

  private void createUIComponents() {
    myBCCombo = new BCCombo(myProject);
  }
}
