package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.CommonBundle;
import com.intellij.chromeConnector.debugger.ChromeDebugProcess;
import com.intellij.chromeConnector.debugger.ChromeDebuggerEngine;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Url;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartiumDebuggerEngine extends ChromeDebuggerEngine {
  public DartiumDebuggerEngine() {
    super("dartium");
  }

  public ChromeDebugProcess createDebugProcess(@NotNull final XDebugSession session,
                                               @NotNull final WebBrowser browser,
                                               @NotNull final DebuggableFileFinder fileFinder,
                                               @Nullable final Url initialUrl,
                                               @Nullable final ExecutionResult executionResult,
                                               final boolean usePreliminaryPage) {
    final ChromeDebugProcess debugProcess =
      super.createDebugProcess(session, browser, fileFinder, initialUrl, executionResult, usePreliminaryPage);
    debugProcess.setProcessBreakpointConditionsAtIDESide(true);
    return debugProcess;
  }

  @Override
  @NotNull
  public WebBrowser getBrowser() {
    final WebBrowser dartium = DartiumUtil.getDartiumBrowser();
    return dartium != null ? dartium : super.getBrowser(); // may be return some fake browser if Dartium not found?
  }

  @Override
  public void checkAvailability(@NotNull final Project project) throws RuntimeConfigurationError {
    if (DartiumUtil.getDartiumBrowser() == null) {
      throw new RuntimeConfigurationError(DartBundle.message("dartium.not.configured", CommonBundle.settingsActionPath()), new Runnable() {
        @Override
        public void run() {
          ShowSettingsUtilImpl.showSettingsDialog(project, DartConfigurable.DART_SETTINGS_PAGE_ID, "");
        }
      });
    }
  }

  @Override
  protected boolean isPreferredEngineForFile(@NotNull PsiFile psiFile) {
    return isHtmlFileWithDartScript(psiFile);
  }

  private static boolean isHtmlFileWithDartScript(@Nullable PsiFile psiFile) {
    if (psiFile == null || !HtmlUtil.isHtmlFile(psiFile)) {
      return false;
    }

    final String text = psiFile.getText();
    int i = -1;
    while ((i = text.indexOf("application/dart", i + 1)) != -1) {
      final PsiElement element = psiFile.findElementAt(i);
      if (element != null && element.getParent() instanceof XmlAttributeValue) {
        if (HtmlUtil.isScriptTag(PsiTreeUtil.getParentOfType(element, XmlTag.class))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isBrowserSupported(@NotNull WebBrowser browser) {
    return browser.equals(DartiumUtil.getDartiumBrowser());
  }
}
