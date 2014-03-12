package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableList;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.CapturedBrowser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.List;

public class KarmaDebugBrowserSelector {

  private static final Key<WebBrowser> WEB_BROWSER_KEY = Key.create("KARMA_WEB_BROWSER_ID");

  private final Project myProject;
  private final ImmutableList<CapturedBrowser> myCapturedBrowsers;
  private final ExecutionEnvironment myEnv;
  private final ProgramRunner myProgramRunner;

  protected KarmaDebugBrowserSelector(@NotNull Project project,
                                      @NotNull Collection<CapturedBrowser> browsers,
                                      @NotNull ExecutionEnvironment env,
                                      @NotNull ProgramRunner programRunner) {
    myProject = project;
    myCapturedBrowsers = ImmutableList.copyOf(browsers);
    myEnv = env;
    myProgramRunner = programRunner;
  }

  @Nullable
  public DebuggableWebBrowser selectDebugEngine() {
    List<WebBrowser> activeCapturedBrowsers = getActiveCapturedBrowsers();
    List<DebuggableWebBrowser> debuggableActiveCapturedBrowsers = toDebuggableWebBrowsers(activeCapturedBrowsers);
    if (debuggableActiveCapturedBrowsers.size() == 1) {
      DebuggableWebBrowser debuggableWebBrowser = ContainerUtil.getFirstItem(debuggableActiveCapturedBrowsers);
      if (debuggableWebBrowser != null) {
        setWebBrowserToReuse(null);
        return debuggableWebBrowser;
      }
    }
    WebBrowser browserToReuse = getWebBrowserToReuse();
    if (browserToReuse != null) {
      DebuggableWebBrowser debuggableBrowser = DebuggableWebBrowser.create(browserToReuse);
      if (debuggableBrowser != null) {
        return debuggableBrowser;
      }
    }
    showBrowserSelectionUi(debuggableActiveCapturedBrowsers);
    return null;
  }

  @NotNull
  private static List<DebuggableWebBrowser> toDebuggableWebBrowsers(@NotNull List<WebBrowser> browsers) {
    List<DebuggableWebBrowser> debuggableWebBrowsers = ContainerUtil.newArrayList();
    for (WebBrowser browser : browsers) {
      DebuggableWebBrowser debuggableBrowser = DebuggableWebBrowser.create(browser);
      if (debuggableBrowser != null) {
        debuggableWebBrowsers.add(debuggableBrowser);
      }
    }
    return debuggableWebBrowsers;
  }

  @NotNull
  private List<WebBrowser> getActiveCapturedBrowsers() {
    List<WebBrowser> capturedBrowsers = ContainerUtil.newArrayList();
    List<WebBrowser> activeBrowsers = WebBrowserManager.getInstance().getActiveBrowsers();
    for (WebBrowser browser : activeBrowsers) {
      boolean matched = false;
      for (CapturedBrowser capturedBrowser : myCapturedBrowsers) {
        if (StringUtil.containsIgnoreCase(capturedBrowser.getName(), browser.getFamily().getName())) {
          matched = true;
          break;
        }
      }
      if (matched) {
        capturedBrowsers.add(browser);
      }
    }
    return capturedBrowsers;
  }

  @Nullable
  private WebBrowser getWebBrowserToReuse() {
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(myEnv.getRunProfile(), KarmaRunConfiguration.class);
    if (runConfiguration != null) {
      return WEB_BROWSER_KEY.get(runConfiguration);
    }
    return null;
  }

  private void setWebBrowserToReuse(@Nullable WebBrowser browser) {
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(myEnv.getRunProfile(), KarmaRunConfiguration.class);
    if (runConfiguration != null) {
      WEB_BROWSER_KEY.set(runConfiguration, browser);
    }
  }

  private void showBrowserSelectionUi(@NotNull Collection<DebuggableWebBrowser> debuggableActiveCapturedBrowsers) {
    final String message;
    if (debuggableActiveCapturedBrowsers.isEmpty()) {
      List<WebBrowser> activeBrowsers = WebBrowserManager.getInstance().getActiveBrowsers();
      List<DebuggableWebBrowser> debuggableActiveBrowsers = toDebuggableWebBrowsers(activeBrowsers);
      if (debuggableActiveBrowsers.isEmpty()) {
        message = "<html><body>" +
                  "No supported browsers found." +
                  "<p/>" +
                  "JavaScript debugging is currently supported in Chrome or Firefox" +
                  "</body></html>";
      }
      else {
        message = formatBrowserSelectionHtml(debuggableActiveBrowsers);
      }
    }
    else {
      message = formatBrowserSelectionHtml(debuggableActiveCapturedBrowsers);
    }

    ToolWindowManager.getInstance(myProject).notifyByBalloon(
      myEnv.getExecutor().getToolWindowId(),
      MessageType.WARNING,
      message,
      null,
      new HyperlinkAdapter() {
        @Override
        protected void hyperlinkActivated(HyperlinkEvent e) {
          WebBrowser browser = WebBrowserManager.getInstance().findBrowserById(e.getDescription());
          if (browser != null) {
            setWebBrowserToReuse(browser);
            if (!ExecutorRegistry.getInstance().isStarting(myProject, myEnv.getExecutor().getId(), myProgramRunner.getRunnerId())) {
              ExecutionManager executionManager = ExecutionManager.getInstance(myProject);
              executionManager.restartRunProfile(myProgramRunner, myEnv, myEnv.getContentToReuse());
            }
          }
        }
      }
    );
  }

  @NotNull
  private static String formatBrowserSelectionHtml(@NotNull Collection<DebuggableWebBrowser> browsers) {
    StringBuilder builder = new StringBuilder("<html><body>");
    builder.append("<div style='padding-top:4px; padding-bottom:4px'>");
    builder.append("Karma tests can be debugged in Google Chrome or Mozilla Firefox only");
    builder.append("</div>");

    builder.append("<table align='center' cellspacing='0' cellpadding='0' style='border: none;padding-bottom:2px'>");
    builder.append("<tr>");
    for (DebuggableWebBrowser browser : browsers) {
      builder.append("<td>");
      builder.append("<div style='padding-right:7px;padding-left:7px'>");
      builder.append("<a href='").append(browser.getWebBrowser().getId()).append("'>")
        .append("Debug in ").append(browser.getWebBrowser().getName())
        .append("</a>");
      builder.append("</div>");
      builder.append("</td>");
    }
    builder.append("</tr>");
    builder.append("</table>");
    builder.append("</body></html>");
    return builder.toString();
  }

}
