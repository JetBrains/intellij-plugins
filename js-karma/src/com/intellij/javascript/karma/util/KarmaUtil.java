package com.intellij.javascript.karma.util;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class KarmaUtil {

  private KarmaUtil() {
  }

  public static void restart(@NotNull RunContentDescriptor descriptor) {
    Runnable restarter = descriptor.getRestarter();
    ProcessHandler processHandler = descriptor.getProcessHandler();
    if (restarter != null && processHandler != null) {
      if (processHandler.isStartNotified() && !processHandler.isProcessTerminating() && !processHandler.isProcessTerminated()) {
        restarter.run();
      }
    }

  }

}
