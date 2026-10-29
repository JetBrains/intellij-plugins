package com.intellij.plugins.serialmonitor.ui.console;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.console.DuplexConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ExecutionDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.actions.SaveHistoryToFileAction;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Shows the bytes given to {@link #append} in a terminal, or in a hex view the user can switch to, with actions to scroll,
 * pause, save and clear the output.
 */
@ApiStatus.Internal
public class JeditermDuplexConsoleView extends DuplexConsoleView<JeditermConsoleView, HexConsoleView>
  implements Disposable {

  private final @NlsSafe @NotNull String myName;
  private final @NotNull String myStateStorageKey;
  private final @NotNull ToggleAction mySwitchConsoleAction;

  /**
   * @param name            the name of the terminal, and the default name of the saved history
   * @param input           receives what the user types into the terminal, which is dropped if {@code null}
   * @param stateStorageKey the key under which the choice between the terminal and the hex view is remembered
   */
  public JeditermDuplexConsoleView(@NotNull Project project,
                                   @NlsSafe @NotNull String name,
                                   @Nullable Consumer<byte[]> input,
                                   @NotNull String stateStorageKey) {
    super(new JeditermConsoleView(project, name, input), new HexConsoleView(project, true), primaryByDefault(stateStorageKey));
    myName = name;
    myStateStorageKey = stateStorageKey;
    mySwitchConsoleAction = new SwitchConsoleViewAction();
  }

  private static @NotNull String primaryByDefault(@NotNull String stateStorageKey) {
    if (!PropertiesComponent.getInstance().isValueSet(stateStorageKey)) {
      PropertiesComponent.getInstance().setValue(stateStorageKey, true);
    }
    return stateStorageKey;
  }

  @Override
  public @NotNull Presentation getSwitchConsoleActionPresentation() {
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
  @Override
  public @NotNull AnAction @NotNull [] createConsoleActions() {

    return new AnAction[]{
      mySwitchConsoleAction,
      getPrimaryConsoleView().getScrollToTheEndToolbarAction(),
      new MyScrollToTheEndToolbarAction(getSecondaryConsoleView().getEditor()),
      getPrimaryConsoleView().getPrintTimestampsToggleAction(),
      new PauseAction(),
      new SaveHistoryToFileAction(getPrimaryConsoleView().getTerminalTextBuffer(), this::getHistoryFileName, this::historySaved),
      new ClearAllAction()};
  }

  public boolean isTimestamped() {
    return getPrimaryConsoleView().isTimestamped();
  }

  public void append(byte[] dataChunk) {
    getPrimaryConsoleView().output(dataChunk);
    getSecondaryConsoleView().output(dataChunk);
  }

  protected @NotNull String getHistoryFileName() {
    return myName + ".log";
  }

  protected void historySaved(int lines) {
  }

  private class SwitchConsoleViewAction extends ToggleAction implements DumbAware {

    private static Supplier<String> getDynamicText(JeditermDuplexConsoleView consoleView) {
      return () -> {
        return consoleView.isPrimaryConsoleEnabled() ?
          SerialMonitorBundle.message("switch.console.view.to.hex.title") :
          SerialMonitorBundle.message("switch.console.view.off.hex.title");
      };
    }

    private SwitchConsoleViewAction() {
      super(getDynamicText(JeditermDuplexConsoleView.this), () -> "", SerialMonitorIcons.HexSerial);
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
      PropertiesComponent.getInstance().setValue(myStateStorageKey, Boolean.toString(!flag));
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
        enabled = e.getData(ExecutionDataKeys.CONSOLE_VIEW) != null;
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

  private class PauseAction extends ToggleAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }

    private PauseAction() {
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
}
