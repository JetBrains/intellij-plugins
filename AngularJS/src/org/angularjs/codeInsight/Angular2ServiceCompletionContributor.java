package org.angularjs.codeInsight;


import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand;
import com.intellij.lang.typescript.compiler.languageService.ide.TypeScriptLanguageServiceCompletionContributor;
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptCompletionsRequestArgs;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.angularjs.service.Angular2LanguageService;
import org.angularjs.service.protocol.command.Angular2CompletionsCommand;
import org.jetbrains.annotations.NotNull;

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

  @NotNull
  @Override
  protected JSLanguageServiceSimpleCommand createCommand(TypeScriptCompletionsRequestArgs args) {
    return new Angular2CompletionsCommand(args);
  }
}
