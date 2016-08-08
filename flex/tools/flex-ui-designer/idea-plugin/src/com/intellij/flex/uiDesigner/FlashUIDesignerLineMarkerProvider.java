package com.intellij.flex.uiDesigner;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
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

    //holder.createInfoAnnotation(textRange, null).setGutterIconRenderer(new MyRenderer());
    return new LineMarkerInfo<>(element, element.getTextRange(), PlatformIcons.UI_FORM_ICON, Pass.LINE_MARKERS,
                                element1 -> FlashUIDesignerBundle.message("gutter.open"),
                                new GutterIconNavigationHandler<PsiElement>() {
                                  @Override
                                  public void navigate(MouseEvent e, PsiElement elt) {
                                    DesignerApplicationManager.getInstance().openDocument(psiFile, false);
                                  }
                                }, GutterIconRenderer.Alignment.LEFT
    );
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
  }
}
