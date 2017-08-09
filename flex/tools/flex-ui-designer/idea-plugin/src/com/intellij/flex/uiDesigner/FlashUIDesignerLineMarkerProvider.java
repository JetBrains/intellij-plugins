package com.intellij.flex.uiDesigner;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.PlatformIcons;
import com.intellij.xml.util.XmlTagUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class FlashUIDesignerLineMarkerProvider implements LineMarkerProvider {
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    if (!(element instanceof XmlTag)) {
      return null;
    }

    final XmlTag tag = (XmlTag)element;
    final XmlFile psiFile = (XmlFile)tag.getContainingFile();
    if (psiFile.getRootTag() != tag || !DesignerApplicationManager.isApplicable(tag.getProject(), psiFile)) {
      return null;
    }

    XmlToken anchor = XmlTagUtil.getStartTagNameElement(tag);
    return anchor == null ? null :
           new LineMarkerInfo<>(anchor, anchor.getTextRange(), PlatformIcons.UI_FORM_ICON, Pass.LINE_MARKERS,
                                __ -> FlashUIDesignerBundle.message("gutter.open"),
                                (GutterIconNavigationHandler<PsiElement>)(e, __) -> DesignerApplicationManager.getInstance().openDocument(psiFile, false), GutterIconRenderer.Alignment.LEFT
    );
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
  }
}
