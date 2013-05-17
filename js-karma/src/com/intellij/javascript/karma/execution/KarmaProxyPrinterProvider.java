package com.intellij.javascript.karma.execution;

import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.TestProxyPrinterProvider;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.javascript.testFramework.util.EscapeUtils;
import com.intellij.javascript.testFramework.util.StacktracePrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class KarmaProxyPrinterProvider implements TestProxyPrinterProvider {

  private BaseTestsOutputConsoleView myConsoleView;

  @Override
  public Printer getPrinterByType(@NotNull String nodeType, @Nullable String arguments) {
    BaseTestsOutputConsoleView consoleView = myConsoleView;
    if (consoleView == null) {
      return null;
    }
    if ("browser".equals(nodeType) && arguments != null) {
      List<String> args = EscapeUtils.split(arguments, ',');
      if (args.size() == 2) {
        File basePath = new File(args.get(0));
        String browserName = args.get(1);
        if (basePath.isAbsolute() && basePath.isDirectory()) {
          return new StacktracePrinter(consoleView, basePath, browserName);
        }
      }
    }
    return null;
  }

  public void setConsoleView(@NotNull BaseTestsOutputConsoleView consoleView) {
    myConsoleView = consoleView;
  }
}
