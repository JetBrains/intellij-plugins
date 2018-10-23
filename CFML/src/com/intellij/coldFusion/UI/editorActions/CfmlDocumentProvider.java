// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeNameImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlDocumentProvider extends DocumentationProviderEx {
  @Override
  public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
    return null;
  }

  @Override
  public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
    return null;
  }

  @Override
  public String generateDoc(PsiElement element, PsiElement originalElement) {
    if (element instanceof CfmlAttributeImpl && element.getParent() instanceof CfmlTag) {
      String tagName = ((CfmlTag)element.getParent()).getTagName().toLowerCase(Locale.ENGLISH);
      String attributeName = (element instanceof CfmlAttributeNameImpl) ?
                             "name" :
                             StringUtil.notNullize(((CfmlAttributeImpl)element).getName());
      return CfmlUtil.getAttributeDescription(tagName, attributeName, element.getProject());
    }
    else if (element instanceof CfmlTag) {
      String name = ((CfmlTag)element).getTagName().toLowerCase(Locale.ENGLISH);
      if (CfmlUtil.isStandardTag(name, element.getProject())) {
        return CfmlUtil.getTagDescription(name, element.getProject());
      }
    }
    return null;
  }

  @Override
  public PsiElement getDocumentationElementForLookupItem(@NotNull PsiManager psiManager, @NotNull Object object, @NotNull PsiElement element) {
    return null;
  }

  @Override
  public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
    return null;
  }

  @Nullable
  @Override
  public PsiElement getCustomDocumentationElement(@NotNull Editor editor,
                                                  @NotNull PsiFile file,
                                                  @Nullable PsiElement contextElement) {
    if (contextElement == null) return null;
    if (contextElement.getParent() instanceof CfmlTagImpl) return contextElement.getParent();
    if (contextElement.getParent() instanceof CfmlAttributeImpl) {
      return contextElement.getParent();
    }
    else {
      return super.getCustomDocumentationElement(editor, file, contextElement);
    }
  }
}
