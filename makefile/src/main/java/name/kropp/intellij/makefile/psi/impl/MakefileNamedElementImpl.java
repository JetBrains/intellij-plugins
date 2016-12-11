package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import name.kropp.intellij.makefile.psi.MakefileNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class MakefileNamedElementImpl extends ASTWrapperPsiElement implements MakefileNamedElement {
    public MakefileNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }
}