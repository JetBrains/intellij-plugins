package org.intellij.plugins.markdown.lang.stubs;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCompositePsiElement;

public abstract class MarkdownCompositeStubBasedPsiElementBase<Stub extends MarkdownStubElement>
  extends StubBasedPsiElementBase<Stub> implements MarkdownCompositePsiElement, StubBasedPsiElement<Stub> {

  public MarkdownCompositeStubBasedPsiElementBase(final Stub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public MarkdownCompositeStubBasedPsiElementBase(final ASTNode node) {
    super(node);
  }

  public String toString() {
    return getElementType().toString();
  }
}