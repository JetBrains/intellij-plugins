package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.ProcessingContext;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import org.jetbrains.annotations.NotNull;

final class JavaScriptInJadeCompletionContributor extends CompletionContributor {
  JavaScriptInJadeCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement().inFile(PlatformPatterns.psiFile(JadeFileImpl.class)),
      new CompletionProvider<>() {
        public void addCompletions(@NotNull CompletionParameters parameters,
                                   @NotNull ProcessingContext context,
                                   @NotNull CompletionResultSet resultSet) {
          PsiFile file = parameters.getOriginalFile();

          file.acceptChildren(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement el) {
              if (!(el instanceof JSNamedElement var) || (el instanceof JSDefinitionExpression)) {
                super.visitElement(el);
                return;
              }

              resultSet.addElement(JSLookupUtilImpl.createLookupElement(var));
            }
          });
        }
      }
    );
  }
}
