package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.coldFusion.CfmlBundle;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public abstract class CfmlInspectionBase extends LocalInspectionTool {
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new PsiElementVisitor() {
            public void visitElement(final PsiElement element) {
                registerProblems(element, holder);
            }
        };
    }

    protected abstract void registerProblems(final PsiElement element, final ProblemsHolder holder); 

    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return CfmlBundle.message("cfml.inspections.group");
    }

    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    public boolean isEnabledByDefault() {
        return true;
    }
}
