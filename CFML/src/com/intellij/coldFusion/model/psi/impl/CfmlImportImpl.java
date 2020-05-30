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

import com.intellij.coldFusion.model.psi.CfmlComponentReference;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 * @date 16.02.11
 */
public class CfmlImportImpl extends CfmlTagImpl implements CfmlImport {
  public CfmlImportImpl(ASTNode astNode) {
    super(astNode);
  }

  @Override
  public boolean isImported(String componentName) {
    String importString = getImportString();
    return importString != null ? importString.endsWith(componentName) : false;
  }

  @Override
  public String getImportString() {
    PsiElement taglib = getAttributeValueElement("taglib");
    return taglib != null ? taglib.getText() : null;
  }

  @Override
  public String getPrefix() {
    PsiElement taglib = getAttributeValueElement("prefix");
    return taglib != null ? taglib.getText() : null;
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    PsiElement taglib = getAttributeValueElement("taglib");
    if (taglib != null) {
      return new PsiReference[]{new CfmlComponentReference(taglib.getNode(), this)};
    }
    return super.getReferences();
  }
}
