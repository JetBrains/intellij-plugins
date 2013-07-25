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
import com.intellij.coldFusion.model.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 * Date: 27.10.2008
 */
public class CfmlTagImpl extends CfmlCompositeElement implements CfmlTag {
  public CfmlTagImpl(ASTNode astNode) {
    super(astNode);
  }

  public CfmlTagImpl(@NotNull NamedStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    return CfmlPsiUtil.processDeclarations(processor, state, lastParent, this);
  }

  @NotNull
  public String getTagName() {
    PsiElement pe = findChildByType(CfmlTokenTypes.CF_TAG_NAME);
    return pe == null ? "" : pe.getText().toLowerCase();
  }

  @Override
  public String getName() {
    return getTagName();
  }

  @Nullable
  public PsiNamedElement getDeclarativeElement() {
    if ("cfset".equals(getName())) {
      final CfmlAssignmentExpression assignment = findChildByClass(CfmlAssignmentExpression.class);
      return assignment != null ? assignment.getAssignedVariable() : null;
    }
    return findChildByClass(CfmlNamedAttributeImpl.class);
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    final Pair<String, String> prefixAndName = CfmlUtil.getPrefixAndName(getName());
    final String componentName = prefixAndName.getSecond();
    final CfmlImport cfmlImport = CfmlUtil.getImportByPrefix(this, prefixAndName.getFirst());
    PsiElement tagName = findChildByType(CfmlTokenTypes.CF_TAG_NAME);
    if (tagName != null && cfmlImport != null && !StringUtil.isEmpty(componentName)) {
      return new PsiReference[]{new CfmlComponentReference(tagName.getNode())};
    }
    return super.getReferences();
  }

  @Nullable
  public PsiElement getAttributeValueElement(@NotNull String attributeName) {
    return CfmlPsiUtil.getAttributeValueElement(this, attributeName);
  }
}
