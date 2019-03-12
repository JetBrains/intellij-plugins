package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NotNull;

public class MakefileFilenameMixin extends ASTWrapperPsiElement {
    MakefileFilenameMixin(@NotNull ASTNode astNode) {
        super(astNode);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return new FileReferenceSet(getNode().getPsi()).getAllReferences();
    }
}
