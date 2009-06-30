package com.intellij.tapestry.intellij.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import org.jetbrains.annotations.NonNls;

/**
 * @author Alexey Chmutov
 *         Date: Jun 29, 2009
 *         Time: 3:54:33 PM
 */
public abstract class CachedUserDataCache<T, Owner extends UserDataHolder> extends UserDataCache<CachedValue<T>, Owner, Object> {
  public CachedUserDataCache(@NonNls String keyName) {
    super(keyName);
  }

  protected final CachedValue<T> compute(final Owner owner, Object p) {
    return PsiManager.getInstance(getProject(owner)).getCachedValuesManager().createCachedValue(new CachedValueProvider<T>() {
      public Result<T> compute() {
        return Result.create(computeValue(owner), getDependencies(owner));
      }
    }, false);
  }

  public abstract T computeValue(Owner owner);

  protected Object[] getDependencies(Owner owner) {
    return new Object[]{owner};
  }

  protected abstract Project getProject(Owner projectOwner);

  public final T get(Owner owner) {
    return get(owner, null).getValue();
  }
}