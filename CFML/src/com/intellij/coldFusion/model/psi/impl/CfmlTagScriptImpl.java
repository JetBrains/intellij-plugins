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

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlRecursiveElementVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlTagScriptImpl extends CfmlTagImpl {
  public static final String TAG_NAME = "cfscript";

  public CfmlTagScriptImpl(ASTNode astNode) {
    super(astNode);
  }

  @NotNull
  public String getTagName() {
    return TAG_NAME;
  }

  public boolean isInsideTag(int offset) {
    PsiElement scriptStartElement = findChildByType(CfmlTokenTypes.R_ANGLEBRACKET);
    PsiElement scriptEndElement = findChildByType(CfmlTokenTypes.LSLASH_ANGLEBRACKET);
    if (scriptStartElement != null &&
        scriptEndElement != null &&
        scriptStartElement.getTextRange().getEndOffset() <= offset &&
        scriptEndElement.getTextRange().getStartOffset() >= offset) {
      return true;
    }
    return false;
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CfmlRecursiveElementVisitor) {
      ((CfmlRecursiveElementVisitor)visitor).visitCfmlTag(this);
    }
    else {
      super.accept(visitor);
    }
  }
}

