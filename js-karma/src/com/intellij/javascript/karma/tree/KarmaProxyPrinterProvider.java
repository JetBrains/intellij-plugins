package com.intellij.javascript.karma.tree;

import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.TestProxyPrinterProvider;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.javascript.karma.KarmaConfig;
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
  private KarmaConfig myKarmaConfig;

  public KarmaProxyPrinterProvider(@NotNull KarmaConfig karmaConfig) {
    myKarmaConfig = karmaConfig;
  }

  @Override
  public Printer getPrinterByType(@NotNull String nodeType, @NotNull String nodeName, @Nullable String arguments) {
    BaseTestsOutputConsoleView consoleView = myConsoleView;
    if (consoleView == null) {
      return null;
    }
    if ("browser".equals(nodeType)) {
      //return getBrowserPrinter(nodeName);
    }
    if ("browserError".equals(nodeType)) {
      return new BrowserErrorPrinter(myConsoleView, myKarmaConfig);
    }
    return null;
  }

  @NotNull
  private Printer getBrowserPrinter(@NotNull String browserName) {
    return null;
  }

  public void setConsoleView(@NotNull BaseTestsOutputConsoleView consoleView) {
    myConsoleView = consoleView;
  }
}
