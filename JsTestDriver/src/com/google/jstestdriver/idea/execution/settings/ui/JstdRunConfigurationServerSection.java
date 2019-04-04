package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.server.JstdServerFetchResult;
import com.google.jstestdriver.idea.server.JstdServerInfo;
import com.google.jstestdriver.idea.server.JstdServerUtils;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.google.jstestdriver.idea.util.TextChangeListener;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.SwingHelper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JstdRunConfigurationServerSection extends AbstractRunSettingsSection {

  private final JRadioButton myInternalServerRadioButton;
  private final JRadioButton myExternalServerRadioButton;
  private final JTextField myExternalServerUrl;
  private final JButton myTestConnectionButton;
  private final JLabel myTestConnectionResult;
  private final JPanel myExternalServerPanel;

  private final JPanel myRoot;

  public JstdRunConfigurationServerSection() {
    myInternalServerRadioButton = new JRadioButton("\u001BRunning in IDE");
    myInternalServerRadioButton.addActionListener(createSwitchServerTypeAction(ServerType.INTERNAL));
    myExternalServerRadioButton = new JRadioButton("\u001BAt address:");
    myExternalServerRadioButton.addActionListener(createSwitchServerTypeAction(ServerType.EXTERNAL));
    ButtonGroup group = new ButtonGroup();
    group.add(myExternalServerRadioButton);
    group.add(myInternalServerRadioButton);

    myExternalServerUrl = new JTextField();
    myTestConnectionButton = new JButton("\u001BTest Connection");
    myTestConnectionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        testConnectionToExternalServer();
      }
    });
    myTestConnectionResult = new JLabel();
    myExternalServerPanel = createExternalServerPanel(myExternalServerUrl, myTestConnectionButton, myTestConnectionResult);

    JPanel panel = SwingHelper.newVerticalPanel(
      Component.LEFT_ALIGNMENT,
      myInternalServerRadioButton,
      myExternalServerRadioButton,
      Box.createVerticalStrut(3),
      myExternalServerPanel
    );
    myRoot = SwingHelper.wrapWithHorizontalStretch(panel);

    SwingUtils.addTextChangeListener(myExternalServerUrl, new TextChangeListener() {
      @Override
      public void textChanged(String oldText, @NotNull String newText) {
        myTestConnectionResult.setText("");
      }
    });
  }

  private ActionListener createSwitchServerTypeAction(final ServerType serverType) {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        myTestConnectionResult.setText("");
        selectServerType(serverType);
      }
    };
  }

  private void selectServerType(@NotNull ServerType serverType) {
    boolean external = serverType == ServerType.EXTERNAL;
    myExternalServerRadioButton.setSelected(external);
    myInternalServerRadioButton.setSelected(!external);
    UIUtil.setEnabled(myExternalServerPanel, external, true);
  }

  private void testConnectionToExternalServer() {
    if (!myExternalServerRadioButton.isSelected()) {
      return;
    }
    String serverUrl = myExternalServerUrl.getText();
    myTestConnectionButton.setEnabled(false);
    myTestConnectionResult.setForeground(UIUtil.getLabelForeground());
    myTestConnectionResult.setText("Connecting to " + serverUrl + " ...");
    JstdServerUtils.asyncFetchServerInfo(serverUrl, serverFetchResult -> UIUtil.invokeLaterIfNeeded(() -> {
      if (serverFetchResult.isError()) {
        myTestConnectionResult.setForeground(JBColor.RED);
        myTestConnectionResult.setText(serverFetchResult.getErrorMessage());
      }
      else {
        JstdServerInfo serverInfo = serverFetchResult.getServerInfo();
        int capturedBrowsers = serverInfo.getCapturedBrowsers().size();
        final String browserMessage;
        if (capturedBrowsers == 0) {
          browserMessage = "no captured browsers found";
        }
        else if (capturedBrowsers == 1) {
          browserMessage = "1 captured browser found";
        }
        else {
          browserMessage = capturedBrowsers + " captured browsers found";
        }
        myTestConnectionResult.setForeground(UIUtil.getLabelForeground());
        myTestConnectionResult.setText("Connected successfully, " + browserMessage);
      }
      myTestConnectionButton.setEnabled(true);
    }));
  }

  @NotNull
  private static JPanel createExternalServerPanel(@NotNull JTextField externalServerUrl,
                                                  @NotNull JButton testConnectionButton,
                                                  @NotNull JLabel testConnectionResult) {
    JPanel up = new FormBuilder().addLabeledComponent("S&erver URL:", externalServerUrl).getPanel();
    JPanel down = new FormBuilder().addLabeledComponent(testConnectionButton, testConnectionResult).getPanel();
    JPanel panel = SwingHelper.newLeftAlignedVerticalPanel(up, Box.createVerticalStrut(7), down);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
    return panel;
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    selectServerType(runSettings.getServerType());
    if (runSettings.isExternalServerType()) {
      myExternalServerUrl.setText(runSettings.getServerAddress());
    }
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    if (myExternalServerRadioButton.isSelected()) {
      runSettingsBuilder.setServerType(ServerType.EXTERNAL);
      runSettingsBuilder.setServerAddress(StringUtil.notNullize(myExternalServerUrl.getText()));
    } else {
      runSettingsBuilder.setServerType(ServerType.INTERNAL);
    }
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    return myRoot;
  }
}
