package com.intellij.plugins.serialmonitor.ui.console;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.console.DuplexConsoleView;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.AbstractToggleUseSoftWrapsAction;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.plugins.serialmonitor.SerialMonitorException;
import com.intellij.plugins.serialmonitor.SerialPortProfile;
import com.intellij.plugins.serialmonitor.SerialProfileService;
import com.intellij.plugins.serialmonitor.service.JsscSerialService;
import com.intellij.plugins.serialmonitor.service.SerialConnectionListener;
import com.intellij.plugins.serialmonitor.service.SerialSettingsChangeListener;
import com.intellij.plugins.serialmonitor.ui.SerialMonitor;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.actions.ConnectDisconnectAction;
import com.intellij.plugins.serialmonitor.ui.actions.EditSettingsAction;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.components.JBLoadingPanel;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Dmitry_Cherkas
 */
public class SerialMonitorDuplexConsoleView extends DuplexConsoleView<ConsoleViewImpl, HexConsoleView>
  implements Disposable, SerialSettingsChangeListener
{

  private static final String STATE_STORAGE_KEY = "SerialMonitorDuplexConsoleViewState";
  private final @NotNull Project myProject;

  @NotNull private SerialPortProfile myPortProfile;
  @NotNull private final String myName;
  @NotNull private final ToggleAction mySwitchConsoleAction;
  @NotNull private final JBLoadingPanel myLoadingPanel;
  private SerialConnectionListener myListener;
  private Charset myCharset = StandardCharsets.US_ASCII;

  public SerialMonitorDuplexConsoleView(@NotNull Project project,
                                        @NlsSafe @NotNull final String name,
                                        @NotNull SerialPortProfile portProfile,
                                        @NotNull JBLoadingPanel loadingPanel) {
    super(
      new ConsoleViewImpl(project, GlobalSearchScope.allScope(project), true, true),
      new HexConsoleView(project, true), STATE_STORAGE_KEY);
    myProject = project;
    mySwitchConsoleAction = new SwitchConsoleViewAction();
    myLoadingPanel = loadingPanel;
    myName = name;
    myPortProfile = portProfile;
    getPrimaryConsoleView().setEmulateCarriageReturn(false);
    ApplicationManager.getApplication().getMessageBus().connect(this).subscribe(SerialSettingsChangeListener.TOPIC, this);
  }

  public void setPortStateListener(SerialConnectionListener stateListener) {
    myListener = stateListener;
  }

  @Override
  @NotNull
  public Presentation getSwitchConsoleActionPresentation() {
    return mySwitchConsoleAction.getTemplatePresentation();
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
      new MyToggleUseSoftWrapsAction(),
      new MyScrollToTheEndToolbarAction(getPrimaryConsoleView().getEditor()),
      new MyScrollToTheEndToolbarAction(getSecondaryConsoleView().getEditor()),
      new ClearAllAction(this),
      new EditSettingsAction(myName, this)};
  }

  public boolean isConnected() {
    return serialService().isConnected(myPortProfile.getPortName());
  }


  public synchronized void openConnectionTab(boolean doConnect) {
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
        try {
          myCharset = Charset.forName(myPortProfile.getEncoding());
        }
        catch (Throwable e) {
          myCharset = StandardCharsets.US_ASCII;
        }

        if (isPortValid()) {
          // try to connect only when settings are known to be valid
          serialService().connect(myPortProfile, this::append, myListener);
          myListener.updateStatus(SerialConnectionListener.PortStatus.CONNECTED);
        }
        else {
          throw new SerialMonitorException(SerialMonitorBundle.message("serial.port.not.found", myName));
        }
      }
      else {
        serialService().close(myPortProfile.getPortName());
      }
    }
    catch (SerialMonitorException sme) {
      SerialMonitor.Companion.errorNotification(sme.getMessage(), myProject);
      myListener.updateStatus(SerialConnectionListener.PortStatus.FAILURE);
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


  @NotNull
  private static JsscSerialService serialService() {
    return JsscSerialService.getInstance();
  }

  public boolean isLoading() {
    return myLoadingPanel.isLoading();
  }

  public boolean isPortValid() {
    return serialService().isPortValid(myPortProfile.getPortName());
  }

  private class SwitchConsoleViewAction extends ToggleAction implements DumbAware {

    private SwitchConsoleViewAction() {
      super(SerialMonitorBundle.messagePointer("switch.console.view.title"), () -> "", SerialMonitorIcons.HexSerial);
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

  private static class ClearAllAction extends DumbAwareAction {

    private final ConsoleView myConsoleView;

    private ClearAllAction(ConsoleView consoleView) {
      super(ExecutionBundle.messagePointer("clear.all.from.console.action.name"),
            SerialMonitorBundle.messagePointer("action.clear.contents.console.description"), AllIcons.Actions.GC);
      myConsoleView = consoleView;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      boolean enabled = myConsoleView != null && myConsoleView.getContentSize() > 0;
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
      final ConsoleView consoleView = myConsoleView != null ? myConsoleView : e.getData(LangDataKeys.CONSOLE_VIEW);
      if (consoleView != null) {
        consoleView.clear();
      }
    }
  }

  public void append(byte[] dataChunk) {
    //    todo  quick and dirty fix for https://bitbucket.org/dmitry_cherkas/intellij-serial-monitor/issues/1
    //todo crlf
    String text = new String(dataChunk, getCharset()).replaceAll("\r", "");
    getPrimaryConsoleView().print(text, ConsoleViewContentType.NORMAL_OUTPUT);
    getSecondaryConsoleView().output(dataChunk);
  }

  private class MyToggleUseSoftWrapsAction extends AbstractToggleUseSoftWrapsAction {
    private MyToggleUseSoftWrapsAction() {
      super(SoftWrapAppliancePlaces.CONSOLE, false);
      ActionUtil.copyFrom(this, IdeActions.ACTION_EDITOR_USE_SOFT_WRAPS);
    }

    @Override
    protected @Nullable Editor getEditor(@NotNull AnActionEvent e) {
      return getPrimaryConsoleView().getEditor();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
      if (isPrimaryConsoleEnabled()) {
        super.update(e);
      }
      else {
        e.getPresentation().setEnabled(false);
      }
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
  public void settingsChanged() {
    SerialPortProfile savedProfile = SerialProfileService.getInstance().getProfiles().get(myName);
    if(!myPortProfile.equals(savedProfile)) {
      myPortProfile = savedProfile;
      reconnect();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    Application application = ApplicationManager.getApplication();
    application.executeOnPooledThread(() -> {
      try {
        serialService().close(myPortProfile.getPortName());
      }
      catch (SerialMonitorException ignored) {
      }
    });
  }
}
