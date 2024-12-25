// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.support;

import com.intellij.plugins.drools.lang.psi.DroolsIdentifier;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.refactoring.rename.CommonEditorReferenceBeanPropertyRenameHandler;
import org.jetbrains.annotations.Nullable;

public final class DroolsPropertyRenameHandler extends CommonEditorReferenceBeanPropertyRenameHandler {

  public DroolsPropertyRenameHandler() {
    super(DroolsIdentifier.class);
  }

  @Override
  public @Nullable BeanProperty getBeanProperty(PsiElement psiElement) {
    if (psiElement instanceof BeanPropertyElement){
      return super.getBeanProperty(((BeanPropertyElement)psiElement).getMethod());
    }
    return null;
  }
}
