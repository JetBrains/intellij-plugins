package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableList;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.CapturedBrowser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
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
  public JSDebugEngine selectDebugEngine() {
    JSDebugEngine[] engines = JSDebugEngine.getEngines();
    List<JSDebugEngine> filteredEngines = ContainerUtil.filter(engines, new Condition<JSDebugEngine>() {
      @Override
      public boolean value(JSDebugEngine engine) {
        return !engine.getWebBrowser().getName().equals("Dartium");
      }
    });
    List<JSDebugEngine> candidates = ContainerUtil.newSmartList();
    for (JSDebugEngine engine : filteredEngines) {
      WebBrowser webBrowser = engine.getWebBrowser();
      for (CapturedBrowser capturedBrowser : myCapturedBrowsers) {
        if (capturedBrowser.getName().contains(webBrowser.getName())) {
          candidates.add(engine);
          break;
        }
      }
    }
    if (candidates.size() == 1) {
      return candidates.get(0);
    }
    JSDebugEngine debugEngineToReuse = getDebugEngineToReuse();
    if (debugEngineToReuse != null) {
      return debugEngineToReuse;
    }
    showSelectionUI(filteredEngines);
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

  private void showSelectionUI(@NotNull List<JSDebugEngine> debugEngines) {
    final String url;
    if (debugEngines.size() > 0) {
      url = formatUrl(debugEngines);
    }
    else {
      url = "Karma tests can not be debugged";
    }

    ToolWindowManager.getInstance(myProject).notifyByBalloon(
      myEnv.getExecutor().getToolWindowId(),
      MessageType.WARNING,
      url,
      null,
      new HyperlinkAdapter() {
        @Override
        protected void hyperlinkActivated(HyperlinkEvent e) {
          JSDebugEngine debugEngine = JSDebugEngine.findById(e.getDescription());
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
  private String formatUrl(@NotNull List<JSDebugEngine> debugEngines) {
    StringBuilder builder = new StringBuilder("<html><body>");
    if (myCapturedBrowsers.size() == 1) {
      builder.append("Can not debug tests in <code>\"").append(myCapturedBrowsers.get(0).getName()).append("\"</code>.");
    }
    else if (myCapturedBrowsers.size() > 0) {
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
    for (JSDebugEngine engine : debugEngines) {
      builder.append("<td>");
      builder.append("<div style='padding-right:7px;padding-left:7px'>");
      builder.append("<a href='").append(engine.getId()).append("'>")
        .append("Debug in ").append(engine.getWebBrowser().getName())
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
