/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.tapestry.psi;

import com.intellij.icons.AllIcons;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.meta.PsiMetaOwner;
import com.intellij.psi.meta.PsiPresentableMetaData;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
*/
public class PropertyAccessorElement extends FakePsiElement implements PsiMetaOwner, PsiPresentableMetaData {
  private final PsiField myField;
  private final String myName;
  private final boolean myGetterNotSetter;

  public PropertyAccessorElement(final PsiField field, final String name, final boolean getterNotSetter) {
    myField = field;
    myName = name;
    myGetterNotSetter = getterNotSetter;
  }

  @NotNull
  public PsiType getMethodReturnType() {
    return myGetterNotSetter ? myField.getType() : PsiType.VOID;
  }

  @NotNull
  public PsiField getField() {
    return myField;
  }

  @Override
  @NotNull
  public PsiElement getNavigationElement() {
    return myField;
  }

  @Override
  public PsiManager getManager() {
    return myField.getManager();
  }

  @Override
  public PsiElement getDeclaration() {
    return this;
  }

  @Override
  @NonNls
  public String getName(PsiElement context) {
    return getName();
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  @Override
  public void init(PsiElement element) {

  }

  @Override
  @Nullable
  public Icon getIcon(boolean flags) {
    return AllIcons.Nodes.Property;
  }

  @Override
  public PsiElement getParent() {
    return myField;
  }

  @Override
  @Nullable
  public PsiMetaData getMetaData() {
    return this;
  }

  @Override
  public String getTypeName() {
    return JavaBundle.message("bean.property");
  }

  @Override
  @Nullable
  public Icon getIcon() {
    return getIcon(0);
  }

  @Override
  public TextRange getTextRange() {
    return TextRange.from(0, 0);
  }
}
