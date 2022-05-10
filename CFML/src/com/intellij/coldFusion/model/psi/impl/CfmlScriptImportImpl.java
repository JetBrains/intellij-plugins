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

import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlImport;
import com.intellij.coldFusion.model.psi.CfmlStringLiteralExpression;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public class CfmlScriptImportImpl extends CfmlCompositeElement implements CfmlImport {
  public CfmlScriptImportImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isImported(String componentName) {
    String importString = getImportString();
    return importString != null ? importString.endsWith(componentName) : false;
  }

  @Override
  @Nullable
  public String getImportString() {
    CfmlStringLiteralExpression childByType = findChildByClass(CfmlStringLiteralExpression.class);
    if (childByType != null) {
      return childByType.getValue();
    }
    return null;
  }

  @Override
  public String getPrefix() {
    return null;
  }

  /*
  @NotNull
  @Override
  public PsiReference[] getReferences() {
    PsiElement reference = findChildByType(CfscriptElementTypes.COMPONENT_REFERENCE);
    if (reference != null) {
      return new PsiReference[]{new CfmlComponentReference( reference.getNode(), this)};
    }
    return super.getReferences();
  }
  */
}
