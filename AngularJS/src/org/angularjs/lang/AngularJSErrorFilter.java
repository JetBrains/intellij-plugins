// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.codeInsight.tags.AngularJSTagDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

final class AngularJSErrorFilter extends HighlightErrorFilter {
  @Override
  public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement error) {
    final Project project = error.getProject();
    final Language language = error.getLanguage();
    if ("CSS".equals(language.getID()) && PsiTreeUtil.getParentOfType(error, XmlAttribute.class) != null &&
        AngularIndexUtil.hasAngularJS(project)) {
      final PsiFile file = error.getContainingFile();

      PsiErrorElement nextError = error;
      while (nextError != null) {
        // Need to workaround valid Angular in CSS injection (CSS parser emits 3 errors in this code):
        // <div style="background-color: {{entity.color}}"></div>
        if (nextError.getErrorDescription().equals("Unexpected token") &&
            isErrorInTermListBeforeAngularCssValue(nextError, project, file)) {
          return false;
        }
        if (nextError.getErrorDescription().equals("';' expected") && isAngularCssValue(nextError, project, file)) return false;
        if (hasAngularInjectionAt(project, file, nextError.getTextOffset())) return false;
        nextError = PsiTreeUtil.getNextSiblingOfType(nextError, PsiErrorElement.class);
      }
    }
    if (language == AngularJSLanguage.INSTANCE) {
      PsiElement host = InjectedLanguageManager.getInstance(error.getProject()).getInjectionHost(error);
      XmlAttribute attribute = PsiTreeUtil.getParentOfType(host, XmlAttribute.class, false, XmlTag.class);
      if (attribute != null && DirectiveUtil.getAttributeName(attribute.getName()).equals("ng-options")) {
        return false;
      }
    }
    if (HTMLLanguage.INSTANCE.is(language) && error.getErrorDescription().endsWith("not closed")) {
      final PsiElement parent = error.getParent();
      final XmlElementDescriptor descriptor = parent instanceof XmlTag ? ((XmlTag)parent).getDescriptor() : null;
      return !(descriptor instanceof AngularJSTagDescriptor);
    }
    return true;
  }

  private static boolean isErrorInTermListBeforeAngularCssValue(@NotNull PsiErrorElement error,
                                                                @NotNull Project project,
                                                                @NotNull PsiFile file) {
    if (error.getParent() == null || error.getParent().getParent() == null || error.getParent().getParent().getNextSibling() == null) {
      return false;
    }
    return isAngularCssValue(error.getParent().getParent().getNextSibling(), project, file);
  }

  private static boolean isAngularCssValue(@NotNull PsiElement error, @NotNull Project project, @NotNull PsiFile file) {
    PsiElement element = error.getNextSibling();
    while (element != null && element.getNode().getElementType().equals(CssElementTypes.CSS_WHITE_SPACE)) {
      element = element.getNextSibling();
    }
    if (element != null && element.getNode().getElementType().equals(CssElementTypes.CSS_DECLARATION_BLOCK)) {
      PsiElement firstChild = element.getFirstChild();
      if (firstChild == null) return false;
      PsiElement secondChild = firstChild.getNextSibling();
      if (firstChild.getNode().getElementType().equals(CssElementTypes.CSS_LBRACE) &&
          secondChild != null &&
          secondChild.getNode().getElementType().equals(CssElementTypes.CSS_DECLARATION_BLOCK) &&
          secondChild.getFirstChild() != null && secondChild.getFirstChild().getNextSibling() != null) {
        return hasAngularInjectionAt(project, file, secondChild.getFirstChild().getNextSibling().getTextOffset());
      }
    }
    return false;
  }

  private static boolean hasAngularInjectionAt(final Project project, final PsiFile file, final int offset) {
    final PsiElement injection =
      InjectedLanguageManager.getInstance(project).findInjectedElementAt(file, offset);
    return injection != null && injection.getContainingFile().getLanguage() == AngularJSLanguage.INSTANCE;
  }
}
