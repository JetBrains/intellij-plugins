package com.jetbrains.plugins.jade.psi.references;

import com.intellij.lang.javascript.psi.JSExecutionScope;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class JavaScriptInJadeReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(), new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        JSExecutionScope scopeElement;
        PsiElement cur = element;
        while (!(cur instanceof JSExecutionScope) && cur != null) {
          cur = cur.getParent();
        }

        if (cur == null) {
          return PsiReference.EMPTY_ARRAY;
        }

        ArrayList<PsiReference> references = new ArrayList<>();

        scopeElement = (JSExecutionScope) cur;
        scopeElement.acceptChildren(new PsiRecursiveElementVisitor() {
          @Override
          public void visitElement(@NotNull PsiElement el) {
            if (!(el instanceof JSNamedElement ref)) {
              super.visitElement(el);
              return;
            }

            String name = ref.getName();
            if (element.getText().equals(name)) {
              references.add(new JavaScriptInJadeReference(ref, ref.getTextRange()));
            }
          }
        });

        return references.toArray(PsiReference.EMPTY_ARRAY);
      }
    });
  }
}
