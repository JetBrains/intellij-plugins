package com.intellij.lang.javascript.flex.debug;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class FilterSwfLoadUnloadMessagesAction extends ToggleAction implements DumbAware {

  private static final String FILTER_SWF_LOAD_UNLOAD_MESSAGES_PROPERTY = "flex.debug.filter.swf.load.unload";

  @Override
  public boolean isSelected(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    return project != null && isFilterEnabled(project);
  }

  @Override
  public void setSelected(@NotNull final AnActionEvent e, final boolean state) {
    PropertiesComponent.getInstance(e.getProject()).setValue(FILTER_SWF_LOAD_UNLOAD_MESSAGES_PROPERTY, state, true);
  }

  public static boolean isFilterEnabled(final @NotNull Project project) {
    return PropertiesComponent.getInstance(project).getBoolean(FILTER_SWF_LOAD_UNLOAD_MESSAGES_PROPERTY, true);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }
}
