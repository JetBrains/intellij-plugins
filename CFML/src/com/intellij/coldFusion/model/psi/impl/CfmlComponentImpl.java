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
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.stubs.CfmlComponentStub;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlComponentImpl extends CfmlCompositeElement implements CfmlComponent, StubBasedPsiElement<NamedStub> {
  public CfmlComponentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public CfmlComponentImpl(@NotNull NamedStub<CfmlComponent> stub) {
    super(stub, CfmlStubElementTypes.COMPONENT_DEFINITION);
  }

  public String getName() {
    final String nameFromFile = CfmlUtil.getFileName(this);
    if (!StringUtil.isEmpty(nameFromFile)) {
      return nameFromFile;
    }
    final PsiElement nameElement = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    if (nameElement != null) {
      return nameElement.getText();
    }
    return "";
  }

  @NotNull
  public CfmlFunction[] getFunctions() {
    CfmlFunction[] childrenOfType = PsiTreeUtil.getChildrenOfType(this, CfmlFunction.class);
    return childrenOfType == null ? CfmlFunction.EMPTY_ARRAY : childrenOfType;
  }

  @NotNull
  @Override
  public CfmlFunction[] getFunctionsWithSupers(boolean isSuperPriority) {
    return CfmlPsiUtil.getFunctionsWithSupers(this, isSuperPriority);
  }

  @NotNull
  @Override
  public CfmlProperty[] getProperties() {
    CfmlProperty[] childrenOfType = PsiTreeUtil.getChildrenOfType(this, CfmlProperty.class);
    return childrenOfType == null ? CfmlProperty.EMPTY_ARRAY : childrenOfType;
  }

  @NotNull
  @Override
  public CfmlProperty[] getPropertiesWithSupers(boolean isSuperPriority) {
    return CfmlPsiUtil.getPropertiesWithSupers(this, isSuperPriority);
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (!CfmlPsiUtil.processDeclarations(processor, state, lastParent, this)) {
      return false;
    }

    for (CfmlFunction function : getFunctionsWithSupers(place.getFirstChild() instanceof CfmlSuperComponentReference)) {
      if (!processor.execute(function, state)) {
        return false;
      }
    }

    CfmlProperty[] propertiesWithSupers = getPropertiesWithSupers(place.getFirstChild() instanceof CfmlSuperComponentReference);
    for (CfmlProperty property : propertiesWithSupers) {
      if (!processor.execute(property, state)) {
        return false;
      }
    }
    return CfmlPsiUtil.processGlobalVariablesForComponent(this, processor, state, lastParent);
  }

  public boolean isInterface() {
    if (getNode().getFirstChildNode().getElementType() == CfscriptTokenTypes.INTERFACE_KEYWORD) {
      return true;
    }
    return false;
  }

  @NotNull
  public String getSuperName() {
    if (isInterface()) return "";
    if (getStub() != null) return ((CfmlComponentStub)getStub()).getSuperclass();
    return CfmlPsiUtil.getSuperComponentName(this);
  }

  // TODO
  public String[] getInterfaceNames() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  // TODO
  public CfmlComponent[] getImplementedInterfaces() {
    return new CfmlComponent[0];
  }

  public CfmlComponent getSuper() {
    return CfmlPsiUtil.getSuperComponent(this);
  }

  @Override
  public CfmlComponentReference getSuperReference() {
    return CfmlPsiUtil.getSuperComponentReference(this);
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @Override
  public String toString() {
    return super.toString()/*getNode().getElementType().toString()*/;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    return CfmlPsiUtil.getComponentReferencesFromAttributes(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfmlRecursiveElementVisitor) {
      ((CfmlRecursiveElementVisitor)visitor).visitCfmlComponent(this);
    }
    else {
      super.accept(visitor);
    }
  }

  @Override
  public boolean hasImplicitAccessors() {
    PsiElement accessors = CfmlPsiUtil.getAttributeValueElement(this, "accessors");
    if (accessors == null) {
      return false;
    }
    return "yes".equalsIgnoreCase(accessors.getText()) || "true".equalsIgnoreCase(accessors.getText());
  }

  @Override
  public boolean isPersistent() {
    PsiElement persistentEl = CfmlPsiUtil.getAttributeValueElement(this, "persistent");
    if (persistentEl == null) {
      return false;
    }
    return "yes".equalsIgnoreCase(persistentEl.getText()) || "true".equalsIgnoreCase(persistentEl.getText());
  }
}
