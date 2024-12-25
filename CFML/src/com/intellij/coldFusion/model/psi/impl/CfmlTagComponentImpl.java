// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.*;
import com.intellij.coldFusion.model.psi.stubs.CfmlStubElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlTagComponentImpl extends CfmlTagImpl implements CfmlComponent, StubBasedPsiElement<NamedStub> {
public CfmlTagComponentImpl(ASTNode astNode) {
    super(astNode);
  }

  public CfmlTagComponentImpl(@NotNull NamedStub<CfmlComponent> stub) {
    super(stub, CfmlStubElementTypes.COMPONENT_TAG);
  }

  @Override
  public @NotNull String getName() {
    final String name = CfmlPsiUtil.getPureAttributeValue(this, "name");
    final String nameFromFile = CfmlUtil.getFileName(this);
    return !StringUtil.isEmpty(nameFromFile) ? nameFromFile : (name != null ? name : "");
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null;
  }

  @Override
  public CfmlFunction @NotNull [] getFunctions() {
    CfmlFunction[] functions = findChildrenByClass(CfmlFunction.class);
    final CfmlTagScriptImpl[] tagScripts = PsiTreeUtil.getChildrenOfType(this, CfmlTagScriptImpl.class);
    if (tagScripts != null) {
      for(CfmlTagScriptImpl tagScript:tagScripts) {
        final CfmlFunction[] functionsFromScript = PsiTreeUtil.getChildrenOfType(tagScript, CfmlFunction.class);
        if (functionsFromScript != null) functions = ArrayUtil.mergeArrays(functions, functionsFromScript);
      }
    }
    return functions;
  }

  @Override
  public CfmlFunction @NotNull [] getFunctionsWithSupers(boolean isSuperPriority) {
    return CfmlPsiUtil.getFunctionsWithSupers(this, isSuperPriority);
  }

  @Override
  public CfmlProperty @NotNull [] getProperties() {
    return findChildrenByClass(CfmlProperty.class);
  }

  @Override
  public CfmlProperty @NotNull [] getPropertiesWithSupers(boolean isSuperPriority) {
    return CfmlPsiUtil.getPropertiesWithSupers(this, isSuperPriority);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    if ((lastParent == null || lastParent.getParent() != this) &&
        !CfmlPsiUtil.processDeclarations(processor, state, null, this)) {
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
  public @NotNull String getTagName() {
    PsiElement childByType = findChildByType(CfmlTokenTypes.CF_TAG_NAME);
    if (childByType != null) {
      if ("cfinterface".equalsIgnoreCase(childByType.getText())) {
        return "cfinterface";
      }
    }
    return "cfcomponent";
  }

  @Override
  public boolean isInterface() {
    String tagName = getTagName();
    if ("cfinterface".equalsIgnoreCase(tagName)) {
      return true;
    }
    return false;
  }

  @Override
  public @Nullable String getSuperName() {
    final PsiElement rEx = getAttributeValueElement("extends");
    return rEx == null ? null : rEx.getText();
  }

  @Override
  public String[] getInterfaceNames() {
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  @Override
  public CfmlComponent[] getImplementedInterfaces() {
    return CfmlComponent.EMPTY_ARRAY;
  }

  @Override
  public @Nullable CfmlComponent getSuper() {
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
  public PsiReference @NotNull [] getReferences() {
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
