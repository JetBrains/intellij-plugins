package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import name.kropp.intellij.makefile.MakefileReference;
import name.kropp.intellij.makefile.psi.MakefileDependency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MakefileDependencyMixin extends ASTWrapperPsiElement implements MakefileDependency {
    MakefileDependencyMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        return new MakefileReference(this);
    }
}