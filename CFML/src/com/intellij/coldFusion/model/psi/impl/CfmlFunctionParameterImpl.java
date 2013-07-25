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
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlParameter;
import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 *         Time: 13:57:29
 */
public class CfmlFunctionParameterImpl extends CfmlCompositeElement implements CfmlParameter, CfmlVariable {
  public static final CfmlFunctionParameterImpl[] EMPTY_ARRAY = new CfmlFunctionParameterImpl[0];

  public CfmlFunctionParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  public PsiType getPsiType() {
    return null;
  }

  @NotNull
  @Override
  public String getName() {
    final PsiElement parameterName = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    if (parameterName != null) {
      return parameterName.getText();
    }
    return "";
  }

  public boolean isRequired() {
    final PsiElement element = findChildByType(CfscriptTokenTypes.REQUIRED_KEYWORD);
    return element != null;
  }

  public String getType() {
    final PsiElement typeElement = findChildByType(CfmlElementTypes.TYPE);
    if (typeElement != null) {
      return typeElement.getText();
    }
    return null;
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.ARGUMENTS_SCOPE;
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  public PsiElement getNameIdentifier() {
    return getNavigationElement();
  }

  @NotNull
  @Override
  public PsiElement getNavigationElement() {
    final PsiElement parameterName = findChildByType(CfscriptTokenTypes.IDENTIFIER);
    return parameterName != null ? parameterName : super.getNavigationElement();
  }

  @NotNull
  public String getlookUpString() {
    return getName();
  }
}
