package com.jetbrains.profiler;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Maxim
 * Date: 28.08.2010
 * Time: 21:28:04
 */
public class DefaultProfilerExecutor extends Executor {
  @NonNls
  public static final String EXECUTOR_ID = "Profile";

  private static final Icon TOOL_WINDOW_ICON = IconLoader.getIcon("/general/toolWindowDebugger.png");
  public static final Icon ICON;

  static {
    Icon icon = IconLoader.findIcon("/actions/profuleCPU.png", DefaultProfilerExecutor.class, true);
    if (icon == null) icon = IconLoader.findIcon("/actions/profileCPU.png");
    ICON = icon;
  }

  private static final Icon DISABLED_ICON = IconLoader.getIcon("/process/disabledDebug.png");

  @Override
  public String getToolWindowId() {
    return "Profiler";
  }

  @Override
  public Icon getToolWindowIcon() {
    return TOOL_WINDOW_ICON;
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return ICON;
  }

  @Override
  public Icon getDisabledIcon() {
    return DISABLED_ICON;
  }

  @Override
  public String getDescription() {
    return "Profile ActionScript application"; // TODO: profiler description
  }

  @NotNull
  @Override
  public String getActionName() {
    return "Profile";
  }

  @NotNull
  @Override
  public String getId() {
    return EXECUTOR_ID;
  }

  @NotNull
  @Override
  public String getStartActionText() {
    return "&Profile";
  }

  @Override
  public String getContextActionId() {
    return "ProfileClass";
  }

  @Override
  public String getHelpId() {
    return null;  //TODO:
  }

  public static Executor getProfileExecutorInstance() {
    return ExecutorRegistry.getInstance().getExecutorById(EXECUTOR_ID);
  }
}
