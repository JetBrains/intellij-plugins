// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.devtools.DartDevToolsService;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenDartObservatoryUrlAction extends DumbAwareAction {
  private @Nullable String myUrl;
  private final Computable<Boolean> myIsApplicable;

  /**
   * @param url {@code null} if URL is not known at the moment of the action instantiation; use {@link #setUrl(String)} afterwards
   */
  public OpenDartObservatoryUrlAction(final @Nullable String url, final @NotNull Computable<Boolean> isApplicable) {
    super(DartBundle.message("open.observatory.action.text"),
          DartBundle.message("open.observatory.action.description"),
          DartIcons.Dart_16);
    myUrl = url;
    myIsApplicable = isApplicable;
  }

  public void setUrl(final @NotNull String url) {
    myUrl = url;
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(final @NotNull AnActionEvent e) {
    e.getPresentation().setEnabled(myUrl != null && myIsApplicable.compute());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null || myUrl == null) return;

    String urlToOpen;
    String devToolsHostAndPort = DartDevToolsService.getInstance(project).getDevToolsHostAndPort();
    if (devToolsHostAndPort != null) {
      int colonIndex = myUrl.indexOf("://");
      String webSocketUri = "ws" + StringUtil.trimEnd(myUrl.substring(colonIndex), "/") + "/ws";
      urlToOpen = "http://" + devToolsHostAndPort + "/?uri=" + webSocketUri;
    }
    else {
      urlToOpen = myUrl;
    }

    openUrlInChromeFamilyBrowser(urlToOpen);
  }

  /**
   * Opens new tab in any already open Chrome-family browser, if none found - start any new Chrome-family browser
   */
  public static void openUrlInChromeFamilyBrowser(final @NotNull String url) {
    openInAnyChromeFamilyBrowser(url);
  }

  private static void openInAnyChromeFamilyBrowser(final @NotNull String url) {
    final List<WebBrowser> chromeBrowsers = WebBrowserManager.getInstance().getBrowsers(
      browser -> browser.getFamily() == BrowserFamily.CHROME, true);

    BrowserLauncher.getInstance().browse(url, chromeBrowsers.isEmpty() ? null : chromeBrowsers.get(0));
  }
}
