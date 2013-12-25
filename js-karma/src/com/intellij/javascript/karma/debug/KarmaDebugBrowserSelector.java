package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableList;
import com.intellij.execution.Executor;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.karma.server.CapturedBrowser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class KarmaDebugBrowserSelector {

  private final Project myProject;
  private final ImmutableList<CapturedBrowser> myCapturedBrowsers;
  private final Executor myExecutor;

  protected KarmaDebugBrowserSelector(@NotNull Project project,
                                      @NotNull Collection<CapturedBrowser> browsers,
                                      @NotNull Executor executor) {
    myProject = project;
    myCapturedBrowsers = ImmutableList.copyOf(browsers);
    myExecutor = executor;
  }

  @Nullable
  public JSDebugEngine selectDebugEngine() {
    JSDebugEngine[] engines = JSDebugEngine.getEngines();
    for (JSDebugEngine engine : engines) {
      for (CapturedBrowser browser : myCapturedBrowsers) {
        if (browser.getName().contains(engine.getWebBrowser().getName())) {
          return engine;
        }
      }
    }
    select();
    return null;
  }

  private void select() {
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

    builder.append("<div style='padding-top:4px; padding-bottom:6px'>");
    builder.append("Karma tests can be debugged in Google Chrome or Mozilla Firefox only.");
    builder.append("</div>");

    builder.append("<table align='right' cellspacing='0' cellpadding='0' style='border: none;padding-bottom:2px'>");
    builder.append("<tr>");
    builder.append("<td>");
    builder.append("<a href='chrome'>Debug in Chrome</a>");
    builder.append("</td>");
    builder.append("<td>");
    builder.append("<div style='padding-right:10px;padding-left:10px'><a href='chrome'>Debug in Firefox</a></div>");
    builder.append("</td>");
    builder.append("</tr>");
    builder.append("</table>");

    ToolWindowManager.getInstance(myProject).notifyByBalloon(myExecutor.getToolWindowId(), MessageType.WARNING, builder.toString(), null, null);
  }

}
