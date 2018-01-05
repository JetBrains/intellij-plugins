package com.dmarcotte.handlebars.file;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class HbPsiReferenceProvider extends PsiReferenceProvider {

    protected Project project;

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        PsiReference ref = new HbReference(psiElement);
        return new PsiReference[] {ref};
    }
}
