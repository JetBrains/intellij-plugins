// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;


import com.intellij.icons.AllIcons;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptLanguageServiceCompletionContributor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import icons.AngularJSIcons;
import org.angular2.service.Angular2LanguageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil.useServiceCompletion;

public class Angular2ServiceCompletionContributor extends TypeScriptLanguageServiceCompletionContributor {

  @Override
  protected boolean isAvailableForFile(PsiFile file) {
    if (!Angular2LanguageService.isEnabledAngularService(file.getProject())) {
      return false;
    }

    VirtualFile virtualFile = file.getVirtualFile();
    return useServiceCompletion(file.getProject(), virtualFile);
  }

  @Override
  protected boolean isApplicablePlaceForCompletion(PsiElement position) {
    PsiElement parent = position.getParent();
    if (parent instanceof JSExpression) {
      return true;
    }
    if (parent instanceof XmlTag) {
      IElementType type = position.getNode().getElementType();
      if (type == XmlTokenType.XML_NAME || type == XmlTokenType.XML_TAG_NAME) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected VirtualFile getVirtualFile(@NotNull PsiFile file) {
    VirtualFile virtualFile = PsiUtilCore.getVirtualFile(file);
    if (virtualFile instanceof VirtualFileWindow) {
      virtualFile = ((VirtualFileWindow)virtualFile).getDelegate();
    }
    return virtualFile;
  }

  @Override
  @Nullable
  protected Icon getIcon(String kind, @Nullable String kindModifiers) {
    if ("element".equals(kind)) {
      return AllIcons.Nodes.Tag;
    }
    if ("component".equals(kind)) {
      return AngularJSIcons.Angular2;
    }

    return getKindIcon(kind);
  }
}
