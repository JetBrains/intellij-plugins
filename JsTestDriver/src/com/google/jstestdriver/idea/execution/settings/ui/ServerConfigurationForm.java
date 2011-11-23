package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.server.JstdServerFetchResult;
import com.google.jstestdriver.idea.server.JstdServerInfo;
import com.google.jstestdriver.idea.server.JstdServerUtils;
import com.google.jstestdriver.idea.ui.ToolPanel;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.google.jstestdriver.idea.util.TextChangeListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ServerConfigurationForm extends AbstractRunSettingsSection {

  private JPanel myRootComponent;
  private JRadioButton myRunningInIDERadioButton;
  private JRadioButton myAtAddressRadioButton;
  private JTextField myServerAddress;
  private JButton myTestConnectionButton;
  private JLabel myTestConnectionResult;

  public ServerConfigurationForm() {
    myRunningInIDERadioButton.addActionListener(createSwitchServerTypeAction(ServerType.INTERNAL));
    myAtAddressRadioButton.addActionListener(createSwitchServerTypeAction(ServerType.EXTERNAL));
    myTestConnectionResult.setText("");
    myTestConnectionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String serverUrl = myAtAddressRadioButton.isSelected() ? myServerAddress.getText() : "http://localhost:" + ToolPanel.serverPort;
        myTestConnectionButton.setEnabled(false);
        myTestConnectionResult.setText("Connecting to " + serverUrl + " ...");
        final ModalityState currentModalityState = ModalityState.current();
        JstdServerUtils.asyncFetchServerInfo(serverUrl, new Consumer<JstdServerFetchResult>() {
          @Override
          public void consume(final JstdServerFetchResult serverFetchResult) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    StringBuilder str = new StringBuilder("<html>");
                    if (serverFetchResult.isError()) {
                      str.append(buildErrorTextParagraph(serverFetchResult.getErrorMessage()));
                    } else {
                      JstdServerInfo serverInfo = serverFetchResult.getServerInfo();
                      int capturedBrowsers = serverInfo.getCapturedBrowsers().size();
                      final String browserMessage;
                      if (capturedBrowsers == 0) {
                        browserMessage = "no captured browsers";
                      } else if (capturedBrowsers == 1) {
                        browserMessage = "1 captured browser";
                      } else {
                        browserMessage = capturedBrowsers + " captured browsers";
                      }
                      str.append(buildOKTextParagraph("Connection to " + serverInfo.getServerUrl() + " is OK, "
                          + browserMessage));
                    }
                    str.append("</html>");
                    myTestConnectionResult.setText(str.toString());
                    myTestConnectionButton.setEnabled(true);
                  }
                }, currentModalityState);
          }
        });
      }
    });
    SwingUtils.addTextChangeListener(myServerAddress, new TextChangeListener() {
      @Override
      public void textChanged(String oldText, @NotNull String newText) {
        myTestConnectionResult.setText("");
      }
    });
  }

  private String buildTextParagraph(String color, String text) {
    return "<p style='color:" + StringUtil.escapeXml(color) + "'>" + StringUtil.escapeXml(text) + "</p>";
  }

  private String buildErrorTextParagraph(String text) {
    return buildTextParagraph("red", text);
  }

  private String buildOKTextParagraph(String text) {
    return buildTextParagraph("green", text);
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

  private void selectServerType(ServerType serverType) {
    myServerAddress.setEnabled(serverType == ServerType.EXTERNAL);
    myAtAddressRadioButton.setSelected(serverType == ServerType.EXTERNAL);
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    return myRootComponent;
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    selectServerType(runSettings.getServerType());
    if (runSettings.isExternalServerType()) {
      myServerAddress.setText(runSettings.getServerAddress());
    }
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    if (myAtAddressRadioButton.isSelected()) {
      runSettingsBuilder.setServerType(ServerType.EXTERNAL);
      runSettingsBuilder.setServerAddress(ObjectUtils.notNull(myServerAddress.getText(), ""));
    } else {
      runSettingsBuilder.setServerType(ServerType.INTERNAL);
    }
  }

}
