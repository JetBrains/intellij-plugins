package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableList;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.CapturedBrowser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.SmartHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class KarmaDebugBrowserSelector {

  private static final Key<String> JS_DEBUG_ENGINE_KEY = new Key<String>("KARMA_JS_DEBUG_ENGINE_ID");

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
  public Pair<JSDebugEngine, WebBrowser> selectDebugEngine() {
    Set<WebBrowser> result = new SmartHashSet<WebBrowser>();
    List<WebBrowser> browsers = WebBrowserManager.getInstance().getActiveBrowsers();
    for (CapturedBrowser capturedBrowser : myCapturedBrowsers) {
      for (WebBrowser browser : browsers) {
        if (StringUtil.containsIgnoreCase(browser.getName(), capturedBrowser.getName()) ||
            StringUtil.containsIgnoreCase(browser.getFamily().getName(), capturedBrowser.getName())) {
          result.add(browser);
        }
      }
    }

    if (result.size() == 1) {
      WebBrowser browser = ContainerUtil.getFirstItem(result);
      assert browser != null;
      JSDebugEngine debugEngine = JSDebugEngine.findByBrowser(browser);
      return debugEngine == null ? null : Pair.create(debugEngine, browser);
    }
    JSDebugEngine debugEngineToReuse = getDebugEngineToReuse();
    if (debugEngineToReuse != null) {
      return Pair.create(debugEngineToReuse, WebBrowserManager.getInstance().getBrowser(debugEngineToReuse.getBrowserFamily()));
    }
    showSelectionUI(result);
    return null;
  }

  @Nullable
  private static JSDebugEngine getDebugEngine(@NotNull UserDataHolder userDataHolder) {
    String debugEngineId = JS_DEBUG_ENGINE_KEY.get(userDataHolder);
    if (debugEngineId != null) {
      return JSDebugEngine.findById(debugEngineId);
    }
    return null;
  }

  private static void setDebugEngine(@NotNull UserDataHolder userDataHolder, @Nullable JSDebugEngine debugEngine) {
    String debugEngineId = debugEngine != null ? debugEngine.getId() : null;
    JS_DEBUG_ENGINE_KEY.set(userDataHolder, debugEngineId);
  }

  @Nullable
  private JSDebugEngine getDebugEngineToReuse() {
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(myEnv.getRunProfile(), KarmaRunConfiguration.class);
    if (runConfiguration != null) {
      return getDebugEngine(runConfiguration);
    }
    return null;
  }

  private void setDebugEngineToReuse(@NotNull JSDebugEngine debugEngine) {
    KarmaRunConfiguration runConfiguration = ObjectUtils.tryCast(myEnv.getRunProfile(), KarmaRunConfiguration.class);
    if (runConfiguration != null) {
      setDebugEngine(runConfiguration, debugEngine);
    }
  }

  private void showSelectionUI(@NotNull Collection<WebBrowser> browsers) {
    final String message;
    if (browsers.isEmpty()) {
      message = "Karma tests can not be debugged";
    }
    else {
      message = formatUrl(browsers);
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
          JSDebugEngine debugEngine = browser == null ? null :JSDebugEngine.findByBrowser(browser);
          if (debugEngine != null) {
            setDebugEngineToReuse(debugEngine);
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
  private String formatUrl(@NotNull Collection<WebBrowser> debugEngines) {
    StringBuilder builder = new StringBuilder("<html><body>");
    if (myCapturedBrowsers.size() == 1) {
      builder.append("Can not debug tests in <code>\"").append(myCapturedBrowsers.get(0).getName()).append("\"</code>.");
    }
    else if (!myCapturedBrowsers.isEmpty()) {
      builder.append("Can not debug tests in any of:");
      builder.append("<pre>");
      for (CapturedBrowser browser : myCapturedBrowsers) {
        builder.append(" - ").append(browser.getName()).append("\n");
      }
      builder.append("</pre>");
    }

    builder.append("<div style='padding-top:4px; padding-bottom:4px'>");
    builder.append("Karma tests can be debugged in Google Chrome or Mozilla Firefox only.");
    builder.append("</div>");

    builder.append("<table align='center' cellspacing='0' cellpadding='0' style='border: none;padding-bottom:2px'>");
    builder.append("<tr>");
    for (WebBrowser browser : debugEngines) {
      builder.append("<td>");
      builder.append("<div style='padding-right:7px;padding-left:7px'>");
      builder.append("<a href='").append(browser.getId()).append("'>")
        .append("Debug in ").append(browser.getName())
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
