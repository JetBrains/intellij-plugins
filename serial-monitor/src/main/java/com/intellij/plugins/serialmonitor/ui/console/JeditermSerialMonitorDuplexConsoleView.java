package com.intellij.plugins.serialmonitor.ui.console;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.plugins.serialmonitor.SerialMonitorCollector;
import com.intellij.plugins.serialmonitor.SerialMonitorException;
import com.intellij.plugins.serialmonitor.SerialPortProfile;
import com.intellij.plugins.serialmonitor.service.PortStatus;
import com.intellij.plugins.serialmonitor.service.SerialPortService;
import com.intellij.plugins.serialmonitor.ui.SerialMonitor;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.actions.ConnectDisconnectAction;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class JeditermSerialMonitorDuplexConsoleView extends JeditermDuplexConsoleView {

  private static final String STATE_STORAGE_KEY = "SerialMonitorDuplexConsoleViewState";

  private final @NotNull SerialPortService.SerialConnection myConnection;
  private final @NotNull SerialPortProfile myPortProfile;
  private final @NotNull JBLoadingPanel myLoadingPanel;
  private Charset myCharset = StandardCharsets.US_ASCII;

  public SerialPortService.@NotNull SerialConnection getConnection() {
    return myConnection;
  }

  //todo auto reconnect while build
  //todo interoperability with other plugins

  public static @NotNull JeditermSerialMonitorDuplexConsoleView create(@NotNull Project project,
                                                                       @NotNull SerialPortProfile portProfile,
                                                                       @NotNull JBLoadingPanel loadingPanel) {
    SerialPortService.SerialConnection connection =
      ApplicationManager.getApplication().getService(SerialPortService.class)
        .newConnection(portProfile.getPortName());
    JeditermSerialMonitorDuplexConsoleView consoleView =
      new JeditermSerialMonitorDuplexConsoleView(project, connection, portProfile, loadingPanel);
    connection.setDataListener(consoleView::append);
    Disposer.register(consoleView, connection);
    return consoleView;
  }

  private JeditermSerialMonitorDuplexConsoleView(
    @NotNull Project project,
    SerialPortService.@NotNull SerialConnection connection,
    @NotNull SerialPortProfile portProfile,
    @NotNull JBLoadingPanel loadingPanel) {
    super(project, connection.getPortName(), connection::write, STATE_STORAGE_KEY);
    myLoadingPanel = loadingPanel;
    myPortProfile = portProfile;
    myConnection = connection;
  }

  @Override
  public @NotNull AnAction @NotNull [] createConsoleActions() {
    return ArrayUtil.prepend(new ConnectDisconnectAction(this), super.createConsoleActions());
  }

  @Override
  protected @NotNull String getHistoryFileName() {
    return Path.of(myPortProfile.defaultName()).getFileName() + ".log";
  }

  @Override
  protected void historySaved(int lines) {
    SerialMonitorCollector.INSTANCE.logSave(lines);
  }

  public @NotNull PortStatus getStatus() {
    return myConnection.getStatus();
  }

  public synchronized void connect(boolean doConnect) {
    myLoadingPanel.startLoading();
    ApplicationManager.getApplication().executeOnPooledThread(
      () -> {
        performConnect(doConnect);
        ApplicationManager.getApplication().invokeLater(myLoadingPanel::stopLoading);
      }
    );
  }

  private void performConnect(boolean doConnect) {
    try {
      if (doConnect) {
        myConnection.closeSilently(true);
        myCharset = Charset.availableCharsets().getOrDefault(myPortProfile.getEncoding(), StandardCharsets.US_ASCII);
        if (myConnection.getStatus() == PortStatus.DISCONNECTED || myConnection.getStatus() == PortStatus.READY) {
          // try to connect only when settings are known to be valid
          getPrimaryConsoleView().configure(getCharset(), myPortProfile.getNewLine());
          myConnection.connect(myPortProfile);
        }
        else {
          throw new SerialMonitorException(SerialMonitorBundle.message("serial.port.not.found", myPortProfile.getPortName()));
        }
      }
      else {
        myConnection.close(true);
      }
    }
    catch (SerialMonitorException sme) {
      SerialMonitor.Companion.errorNotification(sme.getMessage(), this);
    }
  }

  public void reconnect() {
    myLoadingPanel.startLoading();
    ApplicationManager.getApplication().executeOnPooledThread(
      () -> {
        try {
          performConnect(false);
          performConnect(true);
        }
        finally {
          ApplicationManager.getApplication().invokeLater(myLoadingPanel::stopLoading);
        }
      }
    );
  }

  public @NotNull Charset getCharset() {
    return myCharset;
  }

  public boolean isLoading() {
    return myLoadingPanel.isLoading();
  }
}
