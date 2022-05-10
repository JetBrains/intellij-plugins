/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

/**
 * @author vnikolaenko
 */
public class CfmlComponentConstructorReference {
}/*extends CfmlCompositeElement implements CfmlReference {
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
    return null;
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
    return null;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return false;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return new Object[0];
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
*/