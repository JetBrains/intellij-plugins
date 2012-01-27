package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.actions.RunDesignViewAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlashUIDesignerMxmlAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof XmlTag)) {
      return;
    }

    XmlFile psiFile = (XmlFile)element.getContainingFile();
    if (psiFile.getRootTag() != element || !RunDesignViewAction.isSupported(element.getProject(), psiFile)) {
      return;
    }

    holder.createInfoAnnotation(element, null).setGutterIconRenderer(new MyRenderer());
  }

  private static class MyRenderer extends GutterIconRenderer {
    @NotNull
    @Override
    public Icon getIcon() {
      return PlatformIcons.UI_FORM_ICON;
    }

    @Override
    public AnAction getClickAction() {
      return new RunDesignViewAction();
    }

    @Override
    public boolean equals(Object o) {
      return this == o || !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public boolean isNavigateAction() {
      return true;
    }

    @Override
    public String getTooltipText() {
      return FlashUIDesignerBundle.message("gutter.open");
    }
  }
}
