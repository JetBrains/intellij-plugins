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

import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.psi.CfmlParameter;
import com.intellij.coldFusion.model.psi.CfmlPsiUtil;
import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.CheckUtil;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlTagFunctionParameterImpl extends CfmlTagImpl implements CfmlParameter, CfmlVariable {
  public final static String TAG_NAME = "cfargument";

  public CfmlTagFunctionParameterImpl(ASTNode astNode) {
    super(astNode);
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    CfmlAttributeNameImpl childByClass = findChildByClass(CfmlAttributeNameImpl.class);
    if (childByClass != null) {
      childByClass.setName(name);
    }
    return this;
  }

  @NotNull
  @Override
  public String getName() {
    final CfmlAttributeNameImpl attribute = findChildByClass(CfmlAttributeNameImpl.class);
    if (attribute != null) {
      return attribute.getName();
    }
    return "";
  }

  public boolean isRequired() {
    String requiredAttr = CfmlPsiUtil.getPureAttributeValue(this, "required");
    if (requiredAttr == null) {
      return false;
    }
    requiredAttr = requiredAttr.toLowerCase();
    return "yes".equals(requiredAttr) || "true".equals(requiredAttr);
  }

  public String getType() {
    return CfmlPsiUtil.getPureAttributeValue(this, "type");
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.ARGUMENTS_SCOPE;
  }

  @Override
  @NotNull
  public String getTagName() {
    return TAG_NAME;
  }

  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    final PsiElement parameterName = findChildByClass(CfmlAttributeNameImpl.class);
    if (parameterName != null) {
      PsiElement navigationElement = parameterName.getNavigationElement();
      if (navigationElement != null) {
        return navigationElement;
      }
    }
    return super.getNavigationElement();
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    return processor.execute(this, state);
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @NotNull
  @Override
  public String getlookUpString() {
    return getName();
  }

  @Override
  public int getTextOffset() {
    if (getNavigationElement() == this) {
      return super.getTextOffset();
    }
    return getNavigationElement().getTextOffset();
  }
}
