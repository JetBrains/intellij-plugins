package com.intellij.javascript.karma.tree;

import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.TestProxyPrinterProvider;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.testFramework.util.BrowserStacktraceLineWithHyperlinkProvider;
import com.intellij.javascript.testFramework.util.HyperlinkPrinter;
import com.intellij.javascript.testFramework.util.LineWithHyperlinkProvider;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaProxyPrinterProvider implements TestProxyPrinterProvider {

  private final KarmaServer myKarmaServer;
  private BaseTestsOutputConsoleView myConsoleView;

  public KarmaProxyPrinterProvider(@NotNull KarmaServer karmaServer) {
    myKarmaServer = karmaServer;
  }

  @Override
  public Printer getPrinterByType(@NotNull String nodeType, @NotNull String nodeName, @Nullable String arguments) {
    BaseTestsOutputConsoleView consoleView = myConsoleView;
    if (consoleView == null) {
      return null;
    }
    if ("browser".equals(nodeType)) {
      return getBrowserPrinter(nodeName);
    }
    if ("browserError".equals(nodeType)) {
      return getBrowserErrorPrinter();
    }
    return null;
  }

  @NotNull
  private Printer getBrowserPrinter(@NotNull String browserName) {
    Function<String, File> fileFinder = new Function<String, File>() {
      @Override
      public File fun(String s) {
        File file = new File(s);
        if (file.isFile() && file.isAbsolute()) {
          return file;
        }
        return null;
      }
    };
    LineWithHyperlinkProvider provider = new BrowserStacktraceLineWithHyperlinkProvider(browserName, fileFinder);
    return new HyperlinkPrinter(myConsoleView, HyperlinkPrinter.ERROR_CONTENT_TYPE, provider);
  }

  @Nullable
  private Printer getBrowserErrorPrinter() {
    KarmaConfig karmaConfig = myKarmaServer.getKarmaConfig();
    if (karmaConfig != null) {
      LineWithHyperlinkProvider provider = new KarmaBrowserErrorLineWithHyperlinkProvider(karmaConfig);
      return new HyperlinkPrinter(myConsoleView, HyperlinkPrinter.ERROR_CONTENT_TYPE, provider);
    }
    return null;
  }

  public void setConsoleView(@NotNull BaseTestsOutputConsoleView consoleView) {
    myConsoleView = consoleView;
  }
}
