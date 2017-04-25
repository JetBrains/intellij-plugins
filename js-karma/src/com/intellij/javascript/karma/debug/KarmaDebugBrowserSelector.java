package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableList;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.CapturedBrowser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class KarmaDebugBrowserSelector {
  private static final Key<WebBrowser> WEB_BROWSER_KEY = Key.create("KARMA_WEB_BROWSER_ID");

  private final ImmutableList<CapturedBrowser> myCapturedBrowsers;
  private final ExecutionEnvironment myEnvironment;
  private final ConsoleView myConsoleView;

  protected KarmaDebugBrowserSelector(@NotNull Collection<CapturedBrowser> browsers,
                                      @NotNull ExecutionEnvironment environment,
                                      @NotNull ConsoleView consoleView) {
    myCapturedBrowsers = ImmutableList.copyOf(browsers);
    myEnvironment = environment;
    myConsoleView = consoleView;
  }

  @Nullable
  public DebuggableWebBrowser selectDebugEngine() {
    List<DebuggableWebBrowser> allDebuggableActiveBrowsers = toDebuggableWebBrowsers(WebBrowserManager.getInstance().getActiveBrowsers());
    List<DebuggableWebBrowser> capturedDebuggableActiveBrowsers = filterCaptured(allDebuggableActiveBrowsers);

    if (capturedDebuggableActiveBrowsers.size() == 1) {
      DebuggableWebBrowser debuggableWebBrowser = capturedDebuggableActiveBrowsers.get(0);
      setWebBrowserToReuse(null);
      return ObjectUtils.assertNotNull(debuggableWebBrowser);
    }

    WebBrowser browserToReuse = getWebBrowserToReuse();
    if (browserToReuse != null) {
      DebuggableWebBrowser debuggableBrowser = DebuggableWebBrowser.create(browserToReuse);
      if (debuggableBrowser != null) {
        return debuggableBrowser;
      }
    }

    if (capturedDebuggableActiveBrowsers.isEmpty() && allDebuggableActiveBrowsers.size() == 1) {
      return ObjectUtils.assertNotNull(allDebuggableActiveBrowsers.get(0));
    }
    printSupportedBrowsers(allDebuggableActiveBrowsers, capturedDebuggableActiveBrowsers);
    return null;
  }

  @NotNull
  private static List<DebuggableWebBrowser> toDebuggableWebBrowsers(@NotNull List<WebBrowser> browsers) {
    List<DebuggableWebBrowser> debuggableWebBrowsers = new SmartList<>();
    for (WebBrowser browser : browsers) {
      DebuggableWebBrowser debuggableBrowser = DebuggableWebBrowser.create(browser);
      if (debuggableBrowser != null) {
        debuggableWebBrowsers.add(debuggableBrowser);
      }
    }
    return ImmutableList.copyOf(debuggableWebBrowsers);
  }

  @NotNull
  private List<DebuggableWebBrowser> filterCaptured(@NotNull List<DebuggableWebBrowser> debuggableBrowsers) {
    List<DebuggableWebBrowser> captured = ContainerUtil.filter(debuggableBrowsers, debuggableBrowser -> {
      String browserName = debuggableBrowser.getWebBrowser().getName();
      for (CapturedBrowser capturedBrowser : myCapturedBrowsers) {
        if (StringUtil.containsIgnoreCase(capturedBrowser.getName(), browserName)) {
          return true;
        }
      }
      return false;
    });
    return ImmutableList.copyOf(captured);
  }

  @Nullable
  private WebBrowser getWebBrowserToReuse() {
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(myEnvironment.getRunProfile(), KarmaRunConfiguration.class);
    if (runConfiguration != null) {
      WebBrowser browser = WEB_BROWSER_KEY.get(runConfiguration);
      // browser can be removed or deactivated in "Settings | Web Browsers"
      if (!WebBrowserManager.getInstance().getActiveBrowsers().contains(browser)) {
        WEB_BROWSER_KEY.set(runConfiguration, null);
        return null;
      }
      return browser;
    }
    return null;
  }

  private void setWebBrowserToReuse(@Nullable WebBrowser browser) {
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(myEnvironment.getRunProfile(), KarmaRunConfiguration.class);
    if (runConfiguration != null) {
      WEB_BROWSER_KEY.set(runConfiguration, browser);
    }
  }

  private void printSupportedBrowsers(@NotNull List<DebuggableWebBrowser> allDebuggableActiveBrowsers,
                                      @NotNull List<DebuggableWebBrowser> capturedDebuggableActiveBrowsers) {
    if (capturedDebuggableActiveBrowsers.isEmpty()) {
      if (allDebuggableActiveBrowsers.isEmpty()) {
        myConsoleView.print("No supported browsers found.\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        myConsoleView.print("JavaScript debugging is currently supported in Chrome or Firefox.\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      }
      else {
        printVariants(allDebuggableActiveBrowsers);
      }
    }
    else {
      printVariants(capturedDebuggableActiveBrowsers);
    }
  }

  private void printVariants(@NotNull Collection<DebuggableWebBrowser> browsers) {
    myConsoleView.print("Debug karma tests in:\n", ConsoleViewContentType.SYSTEM_OUTPUT);
    for (DebuggableWebBrowser browser : browsers) {
      myConsoleView.print(" * ", ConsoleViewContentType.SYSTEM_OUTPUT);
      myConsoleView.printHyperlink(browser.getWebBrowser().getName(), new DebugHyperlinkInfo(browser.getWebBrowser()));
      myConsoleView.print("\n", ConsoleViewContentType.SYSTEM_OUTPUT);
    }
  }

  private class DebugHyperlinkInfo implements HyperlinkInfo {

    private final WebBrowser myWebBrowser;

    public DebugHyperlinkInfo(@NotNull WebBrowser webBrowser) {
      myWebBrowser = webBrowser;
    }

    @Override
    public void navigate(Project project) {
      setWebBrowserToReuse(myWebBrowser);
      ExecutionUtil.restart(myEnvironment);

    }
  }
}
