package org.angularjs.codeInsight;


import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptLanguageServiceCompletionContributor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import org.angularjs.service.Angular2LanguageService;
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
    if (parent instanceof JSReferenceExpression && ((JSReferenceExpression)parent).getQualifier() == null) {
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

  @Nullable
  @Override
  protected Icon getIcon(String kind, @Nullable String kindModifiers) {
    //todo more icons!
    if ("element".equals(kind)) {
      return null;
    }
    return super.getIcon(kind, kindModifiers);
  }
}
