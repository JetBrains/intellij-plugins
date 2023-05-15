package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Computable;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenDartObservatoryUrlAction extends DumbAwareAction {
  @Nullable private String myUrl;
  private final Computable<Boolean> myIsApplicable;

  /**
   * @param url {@code null} if URL is not known at the moment of the action instantiation; use {@link #setUrl(String)} afterwards
   */
  public OpenDartObservatoryUrlAction(@Nullable final String url, @NotNull final Computable<Boolean> isApplicable) {
    super(DartBundle.message("open.observatory.action.text"),
          DartBundle.message("open.observatory.action.description"),
          DartIcons.Observatory);
    myUrl = url;
    myIsApplicable = isApplicable;
  }

  public void setUrl(@NotNull final String url) {
    myUrl = url;
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(myUrl != null && myIsApplicable.compute());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    if (myUrl != null) {
      openUrlInChromeFamilyBrowser(myUrl);
    }
  }

  /**
   * Opens new tab in any already open Chrome-family browser, if none found - start any new Chrome-family browser
   */
  public static void openUrlInChromeFamilyBrowser(@NotNull final String url) {
    openInAnyChromeFamilyBrowser(url);
  }

  private static void openInAnyChromeFamilyBrowser(@NotNull final String url) {
    final List<WebBrowser> chromeBrowsers = WebBrowserManager.getInstance().getBrowsers(
      browser -> browser.getFamily() == BrowserFamily.CHROME, true);

    BrowserLauncher.getInstance().browse(url, chromeBrowsers.isEmpty() ? null : chromeBrowsers.get(0));
  }
}
