package com.intellij.plugins.drools.lang.support;

import com.intellij.plugins.drools.lang.psi.DroolsIdentifier;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.refactoring.rename.CommonEditorReferenceBeanPropertyRenameHandler;
import org.jetbrains.annotations.Nullable;

public class DroolsPropertyRenameHandler extends CommonEditorReferenceBeanPropertyRenameHandler {

  public DroolsPropertyRenameHandler() {
    super(DroolsIdentifier.class);
  }

  @Override
  @Nullable
  public BeanProperty getBeanProperty(PsiElement psiElement) {
    if (psiElement instanceof BeanPropertyElement){
      return super.getBeanProperty(((BeanPropertyElement)psiElement).getMethod());
    }
    return null;
  }
}
