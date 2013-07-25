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

import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: vnikolaenko
 * Date: 29.04.2009
 */
// an element which declared in <cftag name = "CfmlNamedElement" ... >
public class CfmlNamedAttributeImpl extends CfmlAttributeNameImpl implements CfmlVariable {
  public CfmlNamedAttributeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public static Icon getIcon() {
    return PlatformIcons.VARIABLE_ICON;
  }

  public PsiType getPsiType() {
    return null;
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  @NotNull
  public String getlookUpString() {
    return getName();
  }
}
