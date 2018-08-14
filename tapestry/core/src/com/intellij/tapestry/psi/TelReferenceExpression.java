package com.intellij.tapestry.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TelReferenceExpression extends TelCompositeElement implements TelReferenceQualifier {

  private final TelQualifiedReference myReference = new TelQualifiedReference(this) {
    @Override
    @NotNull
    public TextRange getRangeInElement() {
      final PsiElement element = getReferenceNameElement();
      if (element == null) return TextRange.from(0, getTextLength());
      return TextRange.from(element.getStartOffsetInParent(), element.getTextLength());
    }

    @Override
    @Nullable
    public String getReferenceName() {
      final PsiElement element = getReferenceNameElement();
      return element == null ? null : element.getText();
    }

    @Override
    @Nullable
    public TelReferenceQualifier getReferenceQualifier() {
      return findChildByClass(TelReferenceQualifier.class);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
      PsiElement resolve = resolve();
      // if we referenced property name before (without get) then rename should also strip get prefix
      if (resolve instanceof PsiMethod && PropertyUtilBase.getPropertyName((PsiMethod)resolve) != null) {
        String newPropertyName = PropertyUtilBase.getPropertyName(newElementName);
        if (newPropertyName != null) newElementName = newPropertyName;
      }
      final PsiElement newReferenceName = TelPsiUtil.parseReference(newElementName, getProject()).getReferenceNameElement();
      getNode().replaceChild(getReferenceNameElement().getNode(), newReferenceName.getNode());
      return TelReferenceExpression.this;
    }
  };

  protected TelReferenceExpression(@NotNull final ASTNode node) {
    super(node);
  }

  private PsiElement getReferenceNameElement() {
    return findChildByType(TelTokenTypes.TAP5_EL_IDENTIFIER);
  }

  @Override
  @NotNull
  public TelQualifiedReference getReference() {
    return myReference;
  }

  @Override
  @Nullable
  public PsiType getPsiType() {
    return myReference.getPsiType();
  }

}
