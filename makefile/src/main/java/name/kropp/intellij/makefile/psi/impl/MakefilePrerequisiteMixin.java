package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import name.kropp.intellij.makefile.MakefileTargetReference;
import name.kropp.intellij.makefile.psi.MakefilePrerequisite;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MakefilePrerequisiteMixin extends ASTWrapperPsiElement implements MakefilePrerequisite {
    MakefilePrerequisiteMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return new MakefileTargetReference(this);
    }
}