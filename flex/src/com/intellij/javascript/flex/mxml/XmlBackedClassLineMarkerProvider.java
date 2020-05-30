package com.intellij.javascript.flex.mxml;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.Query;
import com.intellij.xml.util.XmlTagUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class XmlBackedClassLineMarkerProvider implements LineMarkerProvider {

  @Override
  public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements, @NotNull Collection<? super LineMarkerInfo<?>> result) {
    for (PsiElement element : elements) {
      ProgressManager.checkCanceled();
      PsiElement parent = element.getParent();

      if (parent instanceof XmlTag &&
          XmlTagUtil.getStartTagNameElement((XmlTag)parent) == element &&
          parent.getParent() instanceof XmlDocument &&
          parent.getContainingFile() != null &&
          JavaScriptSupportLoader.isFlexMxmFile(parent.getContainingFile())) {
        final XmlBackedJSClass clazz = XmlBackedJSClassFactory.getInstance().getXmlBackedClass((XmlTag)parent);
        Query<JSClass> classQuery = JSClassSearch.searchClassInheritors(clazz, true);
        XmlToken nameElement = XmlTagUtil.getStartTagNameElement((XmlTag)parent);
        if (classQuery.findFirst() != null && nameElement != null) {
          result.add(new LineMarkerInfo<>(element, nameElement.getTextRange(), AllIcons.Gutter.OverridenMethod,
                                          JavaScriptLineMarkerProvider.ourClassInheritorsTooltipProvider,
                                          JavaScriptLineMarkerProvider.ourClassInheritorsNavHandler, GutterIconRenderer.Alignment.RIGHT));
        }
      }
    }
  }

}
