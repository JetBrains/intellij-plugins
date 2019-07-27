// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.util.ArrayUtilRt;
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

  @Override
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

  @Override
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

  @Override
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

  @Override
  public boolean isInterface() {
    if (getNode().getFirstChildNode().getElementType() == CfscriptTokenTypes.INTERFACE_KEYWORD) {
      return true;
    }
    return false;
  }

  @Override
  @NotNull
  public String getSuperName() {
    if (isInterface()) return "";
    if (getStub() != null) return ((CfmlComponentStub)getStub()).getSuperclass();
    return CfmlPsiUtil.getSuperComponentName(this);
  }

  // TODO
  @Override
  public String[] getInterfaceNames() {
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  // TODO
  @Override
  public CfmlComponent[] getImplementedInterfaces() {
    return CfmlComponent.EMPTY_ARRAY;
  }

  @Override
  public CfmlComponent getSuper() {
    return CfmlPsiUtil.getSuperComponent(this);
  }

  @Override
  public CfmlComponentReference getSuperReference() {
    return CfmlPsiUtil.getSuperComponentReference(this);
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
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
