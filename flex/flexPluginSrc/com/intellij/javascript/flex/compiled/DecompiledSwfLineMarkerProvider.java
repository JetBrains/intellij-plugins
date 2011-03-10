package com.intellij.javascript.flex.compiled;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 07.03.2009
 * Time: 0:25:24
 * To change this template use File | Settings | File Templates.
 */
public class DecompiledSwfLineMarkerProvider extends JavaScriptLineMarkerProvider {
  public DecompiledSwfLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager) {
    super(daemonSettings, colorsManager);
  }

  @Override
  public LineMarkerInfo getLineMarkerInfo(PsiElement element) {
    return null;
  }
}
