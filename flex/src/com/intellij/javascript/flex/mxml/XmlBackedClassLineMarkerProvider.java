package com.intellij.javascript.flex.mxml;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class XmlBackedClassLineMarkerProvider implements LineMarkerProvider {

  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    return null;
  }

  public void collectSlowLineMarkers(@NotNull java.util.List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    for (PsiElement element : elements) {
      ProgressManager.checkCanceled();

      if (element instanceof XmlTag &&
          element.getParent() instanceof XmlDocument &&
          element.getContainingFile() != null &&
          JavaScriptSupportLoader.isFlexMxmFile(element.getContainingFile())) {
        final XmlBackedJSClassImpl clazz = XmlBackedJSClassImpl.getXmlBackedClass((XmlTag)element);
        Query<JSClass> classQuery = JSClassSearch.searchClassInheritors(clazz, true);
        if (classQuery.findFirst() != null) {
          result.add(new LineMarkerInfo<JSClass>(clazz, element.getTextOffset(), AllIcons.Gutter.OverridenMethod,
                                                 Pass.UPDATE_OVERRIDEN_MARKERS,
                                                 JavaScriptLineMarkerProvider.ourClassInheritorsTooltipProvider,
                                                 JavaScriptLineMarkerProvider.ourClassInheritorsNavHandler));
        }
      }
    }
  }

}
