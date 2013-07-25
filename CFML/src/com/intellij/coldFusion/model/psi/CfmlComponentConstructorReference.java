package com.intellij.coldFusion.model.psi;

/**
 * @author vnikolaenko
 * @date 21.02.11
 */
public class CfmlComponentConstructorReference {}/*extends CfmlCompositeElement implements CfmlReference {
  CfmlComponentConstructorCall myParent;

  public CfmlComponentConstructorReference(@NotNull ASTNode node, CfmlComponentConstructorCall parent) {
    super(node);
    myParent = parent;
  }

  @Override
  public PsiType getPsiType() {
    String componentReferenceText = getCanonicalText();
    if (!StringUtil.isEmpty(componentReferenceText)) {
      return new CfmlComponentType(componentReferenceText, getProject());
    }
    return null;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    PsiElement resolveResult = resolve();
    if (resolveResult != null) {
      return new ResolveResult[]{new CfmlResolveResult(resolveResult)};
    }
    return ResolveResult.EMPTY_ARRAY;
  }

  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public TextRange getRangeInElement() {
    int offset = 0;
    if (myParent != null) {
      final int parentOffset = myParent.getTextRange().getStartOffset();
      offset = getTextRange().getStartOffset() - parentOffset;
    }
    return new TextRange(0, getTextLength()).shiftRight(offset);
  }

  @Override
  public PsiElement resolve() {
    CfmlComponentReference componentReference = myParent.getComponentReference();
    if (childByType != null) {
      ASTNode node = childByType.getNode();
      if (node != null) {
        return (new CfmlComponentReference(node, this)).resolve();
      }

    }
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    String componentReferenceText = myParent.getComponentReference().getText();
    if (componentReferenceText != null) {
      return getContainingFile().getComponentQualifiedName(componentReferenceText);
    }
    return "";
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isSoft() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
*/