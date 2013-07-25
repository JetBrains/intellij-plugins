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

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 * @date 07.02.11
 */
public class CfmlThisComponentReference extends CfmlCompositeElement implements CfmlReference {
  public CfmlThisComponentReference(@NotNull ASTNode node) {
    super(node);
  }

  public final PsiReference getReference() {
    return this;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    PsiElement resolveResult = resolve();
    if (resolveResult == null) {
      return ResolveResult.EMPTY_ARRAY;
    }
    return new ResolveResult[]{new PsiElementResolveResult(resolveResult, false)};
  }

  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public TextRange getRangeInElement() {
    return new TextRange(0, getTextLength());
  }

  @Override
  public PsiElement resolve() {
    return getComponentDefinition();
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't rename a keyword");
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't bind a keyword");
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return element instanceof CfmlComponent &&
           (element.getContainingFile() == getContainingFile());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Nullable
  private CfmlComponent getComponentDefinition() {
    CfmlFile containingFile = getContainingFile();
    if (containingFile != null) {
      return containingFile.getComponentDefinition();
    }
    return null;
  }

  @Override
  public PsiType getPsiType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
