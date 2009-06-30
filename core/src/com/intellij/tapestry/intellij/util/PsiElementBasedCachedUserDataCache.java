package com.intellij.tapestry.intellij.util;

import com.intellij.psi.PsiElement;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;

/**
 * @author Alexey Chmutov
 *         Date: Jun 29, 2009
 *         Time: 3:54:33 PM
 */
public abstract class PsiElementBasedCachedUserDataCache<T, Owner extends PsiElement> extends CachedUserDataCache<T, Owner> {
  public PsiElementBasedCachedUserDataCache(@NonNls String keyName) {
    super(keyName);
  }

  public Project getProject(Owner projectOwner) {
    return projectOwner.getProject();
  }
}
