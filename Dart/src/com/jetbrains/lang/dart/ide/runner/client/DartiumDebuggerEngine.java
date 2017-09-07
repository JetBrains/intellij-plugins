package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.CommonBundle;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.DebuggableFileFinder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Url;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.debugger.wip.ChromeDebugProcess;
import com.jetbrains.debugger.wip.ChromeDebuggerEngine;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartiumDebuggerEngine extends ChromeDebuggerEngine {
  private static final Logger LOG = Logger.getInstance(DartiumDebuggerEngine.class);

  @NotNull
  @Override
  public ChromeDebugProcess createDebugProcess(@NotNull final XDebugSession session,
                                               @NotNull final WebBrowser browser,
                                               @NotNull final DebuggableFileFinder fileFinder,
                                               @Nullable final Url initialUrl,
                                               @Nullable final ExecutionResult executionResult,
                                               final boolean usePreliminaryPage) {
    ChromeDebugProcess debugProcess =
      super.createDebugProcess(session, browser, fileFinder, initialUrl, executionResult, usePreliminaryPage);
    debugProcess.setProcessBreakpointConditionsAtIdeSide(true);
    debugProcess.setBreakpointLanguageHint((breakpoint, location) -> {
      String result = StringUtil.endsWithIgnoreCase(breakpoint == null ? location.getUrl().getPath() : breakpoint.getFileUrl(), ".dart")
                      ? "dart"
                      : null;
      if (LOG.isDebugEnabled()) {
        LOG.debug(breakpoint + ", " + location.getUrl() + " " + result);
      }
      return result;
    });
    return debugProcess;
  }

  @Override
  @Nullable
  public WebBrowser getBrowser() {
    return DartiumUtil.getDartiumBrowser();
  }

  @Override
  public void checkAvailability(@NotNull final Project project) throws RuntimeConfigurationError {
    if (DartiumUtil.getDartiumBrowser() == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dartium.not.configured", CommonBundle.settingsActionPath()),
                                          () -> DartConfigurable.openDartSettings(project));
    }
  }

  @Override
  public boolean isBrowserSupported(@NotNull WebBrowser browser) {
    return browser.equals(DartiumUtil.getDartiumBrowser());
  }
}
