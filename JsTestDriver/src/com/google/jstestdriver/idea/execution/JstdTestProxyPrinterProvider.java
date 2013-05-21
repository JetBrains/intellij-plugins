package com.google.jstestdriver.idea.execution;

import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.TestProxyPrinterProvider;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.javascript.testFramework.util.BrowserStacktraceLineWithHyperlinkProvider;
import com.intellij.javascript.testFramework.util.HyperlinkPrinter;
import com.intellij.javascript.testFramework.util.LineWithHyperlinkProvider;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
* @author Sergey Simonchik
*/
public class JstdTestProxyPrinterProvider implements TestProxyPrinterProvider {

  private static final String DEFAULT_PATH_PREFIX = "/test/";

  private BaseTestsOutputConsoleView myConsoleView;

  @Override
  public Printer getPrinterByType(@NotNull String nodeType, @NotNull String nodeName, @Nullable String arguments) {
    BaseTestsOutputConsoleView consoleView = myConsoleView;
    if (consoleView == null) {
      return null;
    }
    if ("browser".equals(nodeType) && arguments != null) {
      final File basePath = new File(arguments);
      if (basePath.isAbsolute() && basePath.isDirectory()) {
        LineWithHyperlinkProvider hyperlinkProvider = new BrowserStacktraceLineWithHyperlinkProvider(nodeName, new Function<String, File>() {
          @Override
          public File fun(String path) {
            return findFileByPath(basePath, path);
          }
        });
        return new HyperlinkPrinter(myConsoleView, HyperlinkPrinter.ERROR_CONTENT_TYPE, hyperlinkProvider);
      }
    }
    if ("browserError".equals(nodeType) && arguments != null) {
      File basePath = new File(arguments);
      if (basePath.isDirectory() && basePath.isAbsolute()) {
        JsErrorLineWithHyperlinkProvider hyperlinkProvider = new JsErrorLineWithHyperlinkProvider(basePath);
        return new HyperlinkPrinter(myConsoleView, HyperlinkPrinter.ERROR_CONTENT_TYPE, hyperlinkProvider);
      }
    }
    return null;
  }

  @Nullable
  private static File findFileByPath(@NotNull File basePath, @NotNull String urlStr) {
    File file = findFileByBasePath(basePath, urlStr);
    if (file != null) {
      return file;
    }
    try {
      URL url = new URL(urlStr);
      String path = url.getPath();
      if (path.startsWith(DEFAULT_PATH_PREFIX)) {
        path = path.substring(DEFAULT_PATH_PREFIX.length());
      }
      return findFileByBasePath(basePath, path);
    } catch (MalformedURLException ignored) {
    }
    return null;
  }

  @Nullable
  private static File findFileByBasePath(@NotNull File basePath, @NotNull String subPath) {
    File file = new File(basePath, subPath);
    if (!file.isFile()) {
      File absoluteFile = new File(subPath);
      if (absoluteFile.isAbsolute() && absoluteFile.isFile()) {
        file = absoluteFile;
      }
    }
    return file.isFile() ? file : null;
  }

  public void setConsoleView(@NotNull BaseTestsOutputConsoleView consoleView) {
    myConsoleView = consoleView;
  }
}
