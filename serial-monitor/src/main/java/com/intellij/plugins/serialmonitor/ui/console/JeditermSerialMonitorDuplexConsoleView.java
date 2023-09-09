package com.intellij.plugins.serialmonitor.ui.console;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.console.DuplexConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.plugins.serialmonitor.SerialMonitorException;
import com.intellij.plugins.serialmonitor.SerialPortProfile;
import com.intellij.plugins.serialmonitor.service.PortStatus;
import com.intellij.plugins.serialmonitor.service.SerialPortService;
import com.intellij.plugins.serialmonitor.service.SerialPortsListener;
import com.intellij.plugins.serialmonitor.ui.SerialMonitor;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.actions.ConnectDisconnectAction;
import com.intellij.plugins.serialmonitor.ui.actions.EditSettingsAction;
import com.intellij.ui.components.JBLoadingPanel;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class JeditermSerialMonitorDuplexConsoleView extends DuplexConsoleView<JeditermConsoleView, HexConsoleView>
  implements Disposable, SerialPortsListener {

  private static final String STATE_STORAGE_KEY = "SerialMonitorDuplexConsoleViewState";

  @NotNull private final SerialPortService.SerialConnection myConnection;
  @NotNull private final SerialPortProfile myPortProfile;
  @NotNull @NlsSafe private final String myName;
  @NotNull private final ToggleAction mySwitchConsoleAction;
  @NotNull private final JBLoadingPanel myLoadingPanel;
  private Charset myCharset = StandardCharsets.US_ASCII;
  public SerialPortService.@NotNull SerialConnection getConnection() {
    return myConnection;
  }

  @Override
  public void portsStatusChanged() {

  }

  //todo auto reconnect while build
  //todo interoperability with other plugins
  //todo hw lines control

  public static JeditermSerialMonitorDuplexConsoleView create(@NotNull Project project,
                                                              @NlsSafe @NotNull final String name,
                                                              @NotNull SerialPortProfile portProfile,
                                                              @NotNull JBLoadingPanel loadingPanel) {
    SerialPortService.SerialConnection connection =
      ApplicationManager.getApplication().getService(SerialPortService.class)
        .connection(portProfile.getPortName());
    JeditermConsoleView textConsoleView = new JeditermConsoleView(project, connection);
    HexConsoleView hexConsoleView = new HexConsoleView(project, true);
    JeditermSerialMonitorDuplexConsoleView consoleView =
      new JeditermSerialMonitorDuplexConsoleView(connection,
                                                 textConsoleView,
                                                 hexConsoleView,
                                                 name,
                                                 portProfile,
                                                 loadingPanel);
    connection.setDataListener(consoleView::append);
    connection.setConnListener(consoleView);
    ApplicationManager.getApplication().getMessageBus().connect().subscribe(SerialPortsListener.getSERIAL_PORTS_TOPIC(), consoleView);
    return consoleView;
  }

  private JeditermSerialMonitorDuplexConsoleView(
    SerialPortService.@NotNull SerialConnection connection,
    JeditermConsoleView textConsoleView,
    HexConsoleView hexConsoleView,
    @NlsSafe @NotNull final String name,
    @NotNull SerialPortProfile portProfile,
    @NotNull JBLoadingPanel loadingPanel) {
    super(textConsoleView, hexConsoleView, STATE_STORAGE_KEY);
    mySwitchConsoleAction = new SwitchConsoleViewAction();
    myLoadingPanel = loadingPanel;
    myName = name;
    myPortProfile = portProfile;
    myConnection = connection;
  }

  @Override
  @NotNull
  public Presentation getSwitchConsoleActionPresentation() {
    return mySwitchConsoleAction.getTemplatePresentation();
  }

  @Override
  public boolean isOutputPaused() {
    return getPrimaryConsoleView().isOutputPaused();
  }

  @Override
  public boolean canPause() {
    return true;
  }

  /**
   * Allows filtering out inappropriate actions from toolbar.
   */
  @NotNull
  @Override
  public AnAction @NotNull [] createConsoleActions() {

    return new AnAction[]{
      new ConnectDisconnectAction(this),
      mySwitchConsoleAction,
      getPrimaryConsoleView().getScrollToTheEndToolbarAction(),
      new MyScrollToTheEndToolbarAction(getSecondaryConsoleView().getEditor()),
      new SerialPauseAction(),
      new ClearAllAction(),
      new EditSettingsAction(myName, this)};
  }

  public PortStatus getStatus() {
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
        myCharset = Charset.availableCharsets().getOrDefault(myPortProfile.getEncoding(), StandardCharsets.US_ASCII);
        if (myConnection.getStatus() == PortStatus.DISCONNECTED) {
          // try to connect only when settings are known to be valid
          getPrimaryConsoleView().reconnect(getCharset(), myPortProfile.getNewLine(), myPortProfile.getLocalEcho());
          myConnection.connect(myPortProfile.getBaudRate(), myPortProfile.getBits(), myPortProfile.getStopBits(),
                               myPortProfile.getParity());
        }
        else {
          throw new SerialMonitorException(SerialMonitorBundle.message("serial.port.not.found", myName));
        }
      }
      else {
        myConnection.close();
      }
    }
    catch (SerialMonitorException sme) {
      SerialMonitor.Companion.errorNotification(sme.getMessage(), this);
    }
  }

  @NotNull
  public SerialPortProfile getPortProfile() {
    return myPortProfile;
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

  @NotNull
  public Charset getCharset() {
    return myCharset;
  }

  public boolean isLoading() {
    return myLoadingPanel.isLoading();
  }

  private class SwitchConsoleViewAction extends ToggleAction implements DumbAware {

    private SwitchConsoleViewAction() {
      super(SerialMonitorBundle.messagePointer("switch.console.view.title"), () -> "", SerialMonitorIcons.HexSerial);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.EDT;
    }

    @Override
    public boolean isSelected(final @NotNull AnActionEvent event) {
      return !isPrimaryConsoleEnabled();
    }

    @Override
    public void setSelected(final @NotNull AnActionEvent event, final boolean flag) {
      enableConsole(!flag);
      PropertiesComponent.getInstance().setValue(STATE_STORAGE_KEY, Boolean.toString(!flag));
    }
  }

  private class ClearAllAction extends DumbAwareAction {

    private ClearAllAction() {
      super(ExecutionBundle.messagePointer("clear.all.from.console.action.name"),
            SerialMonitorBundle.messagePointer("action.clear.contents.console.description"), AllIcons.Actions.GC);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.EDT;
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
      boolean enabled = getContentSize() > 0;
      if (!enabled) {
        enabled = e.getData(LangDataKeys.CONSOLE_VIEW) != null;
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null && editor.getDocument().getTextLength() == 0) {
          enabled = false;
        }
      }
      e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(final @NotNull AnActionEvent e) {
      clear();
    }
  }

  public void append(byte[] dataChunk) {
    getPrimaryConsoleView().output(dataChunk);
    getSecondaryConsoleView().output(dataChunk);
  }

  private class SerialPauseAction extends ToggleAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }

    private SerialPauseAction() {
      super(() -> SerialMonitorBundle.message("action.pause.text"), () -> SerialMonitorBundle.message("action.pause.description"),
            AllIcons.Actions.Pause);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
      return isOutputPaused();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
      setOutputPaused(state);
    }
  }

  private static class MyScrollToTheEndToolbarAction extends ScrollToTheEndToolbarAction {
    private final Editor myEditor;

    private MyScrollToTheEndToolbarAction(Editor editor) {
      super(editor);
      myEditor = editor;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      if (myEditor.getComponent().isShowing()) {
        super.update(e);
      }
      else {
        e.getPresentation().setVisible(false);
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    Application application = ApplicationManager.getApplication();
    application.executeOnPooledThread(() -> {
      try {
        myConnection.close();
      }
      catch (SerialMonitorException ignored) {
      }
    });
  }
}
