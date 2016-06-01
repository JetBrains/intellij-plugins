package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.util.net.NetUtils;
import com.jetbrains.browserConnection.BrowserConnectionManager;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.jsonRpc.Client;

import java.util.List;

public class OpenDartObservatoryUrlAction extends DumbAwareAction {
  private final String myUrl;
  private final Computable<Boolean> myIsApplicable;

  public OpenDartObservatoryUrlAction(@NotNull final String url, @NotNull final Computable<Boolean> isApplicable) {
    super(DartBundle.message("open.observatory.action.text"), DartBundle.message("open.observatory.action.description"), DartIcons.Dart_16);
    myUrl = url;
    myIsApplicable = isApplicable;
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(myIsApplicable.compute());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    openUrlInChromeFamilyBrowser(myUrl);
  }

  /**
   * Opens new tab in any already open Chrome-family browser, if none found - start any new Chrome-family browser
   */
  public static void openUrlInChromeFamilyBrowser(@NotNull final String url) {
    try {
      final BrowserConnectionManager connectionManager = BrowserConnectionManager.getInstance();
      final Client chromeClient = connectionManager.findClient(client -> {
        final WebBrowser browser = connectionManager.getBrowser(client);
        return browser != null && browser.getFamily() == BrowserFamily.CHROME;
      });

      if (chromeClient != null) {
        BrowserConnectionManager.getInstance().openUrl(chromeClient, url);
      }
      else {
        openInAnyChromeFamilyBrowser(url);
      }
    }
    catch (Throwable t) {
      // ClassNotFound in Community Edition or if JavaScriptDebug plugin disabled
      openInAnyChromeFamilyBrowser(url);
    }
  }

  private static void openInAnyChromeFamilyBrowser(@NotNull final String url) {
    final List<WebBrowser> chromeBrowsers = WebBrowserManager.getInstance().getBrowsers(
      browser -> browser.getFamily() == BrowserFamily.CHROME, true);


    BrowserLauncher.getInstance().browse(url, chromeBrowsers.isEmpty() ? null : chromeBrowsers.get(0));
  }
}
