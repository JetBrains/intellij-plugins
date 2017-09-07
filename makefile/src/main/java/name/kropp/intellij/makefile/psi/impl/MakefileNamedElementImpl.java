package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import name.kropp.intellij.makefile.psi.MakefileNamedElement;
import name.kropp.intellij.makefile.stub.MakefileTargetStubElement;
import org.jetbrains.annotations.NotNull;

public abstract class MakefileNamedElementImpl extends StubBasedPsiElementBase<MakefileTargetStubElement> implements MakefileNamedElement {
    public MakefileNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    public MakefileNamedElementImpl(@NotNull MakefileTargetStubElement stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }
}