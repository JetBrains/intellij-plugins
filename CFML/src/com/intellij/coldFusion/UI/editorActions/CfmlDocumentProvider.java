// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeNameImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 */
public final class CfmlDocumentProvider implements DocumentationProvider {

  @Override
  public @Nls String generateDoc(PsiElement element, PsiElement originalElement) {
    if (element instanceof CfmlAttributeImpl && element.getParent() instanceof CfmlTag) {
      String tagName = StringUtil.toLowerCase(((CfmlTag)element.getParent()).getTagName());
      String attributeName = (element instanceof CfmlAttributeNameImpl) ?
                             "name" :
                             StringUtil.notNullize(((CfmlAttributeImpl)element).getName());
      return CfmlUtil.getAttributeDescription(tagName, attributeName, element.getProject());
    }
    else if (element instanceof CfmlTag) {
      String name = StringUtil.toLowerCase(((CfmlTag)element).getTagName());
      if (CfmlUtil.isStandardTag(name, element.getProject())) {
        return CfmlUtil.getTagDescription(name, element.getProject());
      }
    }
    return null;
  }

  @Override
  public @Nullable PsiElement getCustomDocumentationElement(@NotNull Editor editor,
                                                            @NotNull PsiFile file,
                                                            @Nullable PsiElement contextElement,
                                                            int targetOffset) {
    if (contextElement == null) return null;
    if (contextElement.getParent() instanceof CfmlTagImpl) return contextElement.getParent();
    if (contextElement.getParent() instanceof CfmlAttributeImpl) {
      return contextElement.getParent();
    }
    else {
      return null;
    }
  }
}
