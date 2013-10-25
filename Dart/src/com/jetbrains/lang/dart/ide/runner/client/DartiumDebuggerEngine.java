package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.chromeConnector.debugger.ChromeDebuggerEngine;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import org.jetbrains.annotations.NotNull;

public class DartiumDebuggerEngine extends ChromeDebuggerEngine {

  public DartiumDebuggerEngine() {
    super("dartium");
  }

  @NotNull
  public WebBrowser getWebBrowser() {
    return DartSettingsUtil.DARTIUM;
  }

  public void checkAvailability(final Project project) throws RuntimeConfigurationError {
    DartSettingsUtil.getDartiumPathOrThrowErrorWithQuickFix(project);
  }
}
