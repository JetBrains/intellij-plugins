package com.jetbrains.lang.dart.ide.runner.client;

import com.intellij.CommonBundle;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Url;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.debugger.wip.debugger.ChromeDebugProcess;
import com.jetbrains.debugger.wip.debugger.ChromeDebuggerEngine;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
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
  protected boolean isPreferredEngineForFile(@NotNull PsiFile psiFile) {
    return isHtmlFileWithDartScript(psiFile);
  }

  private static boolean isHtmlFileWithDartScript(@Nullable PsiFile psiFile) {
    if (psiFile == null || !HtmlUtil.isHtmlFile(psiFile)) {
      return false;
    }

    final String text = psiFile.getText();
    int i = -1;
    while ((i = text.indexOf(DartLanguage.DART_MIME_TYPE, i + 1)) != -1) {
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
