package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * Created by vnikolaenko
 */
public interface CfmlExpression extends PsiElement {
    @Nullable
    PsiType getPsiType();
}
