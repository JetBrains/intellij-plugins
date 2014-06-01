package com.google.jstestdriver.idea.util;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;

public class JstdUtil {

  private JstdUtil() {}

  public static boolean isActiveDescriptor(@NotNull RunContentDescriptor descriptor) {
    ProcessHandler processHandler = descriptor.getProcessHandler();
    return processHandler != null
           && processHandler.isStartNotified()
           && !processHandler.isProcessTerminating()
           && !processHandler.isProcessTerminated();
  }

  public static void restart(@NotNull RunContentDescriptor descriptor) {
    Runnable restarter = descriptor.getRestarter();
    if (restarter != null && isActiveDescriptor(descriptor)) {
      restarter.run();
    }
  }

}
