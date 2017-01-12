package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ArrayUtil;
import name.kropp.intellij.makefile.MakefileTargetReference;
import name.kropp.intellij.makefile.psi.MakefilePrerequisite;
import org.jetbrains.annotations.NotNull;

public abstract class MakefilePrerequisiteMixin extends ASTWrapperPsiElement implements MakefilePrerequisite {
    MakefilePrerequisiteMixin(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        PsiReference[] references = new FileReferenceSet(this).getAllReferences();
        return ArrayUtil.prepend(new MakefileTargetReference(this), references, PsiReference.ARRAY_FACTORY);
    }
}