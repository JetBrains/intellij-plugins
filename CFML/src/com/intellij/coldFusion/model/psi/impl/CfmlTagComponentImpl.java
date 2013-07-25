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
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlTagComponentImpl extends CfmlTagImpl implements CfmlComponent, StubBasedPsiElement<NamedStub> {

  private final static String TAG_NAME = "cfcomponent";

  public CfmlTagComponentImpl(ASTNode astNode) {
    super(astNode);
  }

  public CfmlTagComponentImpl(@NotNull NamedStub<CfmlComponent> stub) {
    super(stub, CfmlElementTypes.COMPONENT_TAG);
  }

  @Override
  public String getName() {
    final String name = CfmlPsiUtil.getPureAttributeValue(this, "name");
    final String nameFromFile = CfmlUtil.getFileName(this);
    return !StringUtil.isEmpty(nameFromFile) ? nameFromFile : name != null ? name : "";
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public CfmlFunction[] getFunctions() {
    return findChildrenByClass(CfmlFunction.class);
  }

  @NotNull
  @Override
  public CfmlFunction[] getFunctionsWithSupers(boolean isSuperPriority) {
    return CfmlPsiUtil.getFunctionsWithSupers(this, isSuperPriority);
  }

  @NotNull
  @Override
  public CfmlProperty[] getProperties() {
    return findChildrenByClass(CfmlProperty.class);
  }

  @NotNull
  @Override
  public CfmlProperty[] getPropertiesWithSupers(boolean isSuperPriority) {
    return CfmlPsiUtil.getPropertiesWithSupers(this, isSuperPriority);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if (!CfmlPsiUtil.processDeclarations(processor, state, null, this)) {
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

  @NotNull
  public String getTagName() {
    PsiElement childByType = findChildByType(CfmlTokenTypes.CF_TAG_NAME);
    if (childByType != null) {
      if ("cfinterface".equalsIgnoreCase(childByType.getText())) {
        return "cfinterface";
      }
    }
    return "cfcomponent";
  }

  public boolean isInterface() {
    String tagName = getTagName();
    if ("cfinterface".equalsIgnoreCase(tagName)) {
      return true;
    }
    return false;
  }

  @Nullable
  public String getSuperName() {
    final PsiElement rEx = getAttributeValueElement("extends");
    return rEx == null ? null : rEx.getText();
  }

  public String[] getInterfaceNames() {
    return ArrayUtil.EMPTY_STRING_ARRAY;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public CfmlComponent[] getImplementedInterfaces() {
    return new CfmlComponent[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Nullable
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

  @Nullable
  private CfmlComponentReference getReferenceToSuperComponent() {
    return null;
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
    PsiElement accessors = getAttributeValueElement("accessors");
    if (accessors == null) {
      return false;
    }
    return "yes".equalsIgnoreCase(accessors.getText()) || "true".equalsIgnoreCase(accessors.getText());
  }

  @Override
  public boolean isPersistent() {
    PsiElement persistentEl = getAttributeValueElement("persistent");
    if (persistentEl == null) {
      return false;
    }
    return "yes".equalsIgnoreCase(persistentEl.getText()) || "true".equalsIgnoreCase(persistentEl.getText());
  }
}
