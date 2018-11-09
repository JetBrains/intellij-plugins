package com.intellij.tapestry.intellij.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;

/**
 * @author Alexey Chmutov
 */
public abstract class PsiElementBasedCachedUserDataCache<T, Owner extends PsiElement> extends CachedUserDataCache<T, Owner> {
  public PsiElementBasedCachedUserDataCache(@NonNls String keyName) {
    super(keyName);
  }

  @Override
  public Project getProject(Owner projectOwner) {
    return projectOwner.getProject();
  }
}
