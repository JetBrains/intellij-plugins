package com.intellij.tapestry.intellij.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public abstract class CachedUserDataCache<T, Owner extends UserDataHolder> extends UserDataCache<CachedValue<T>, Owner, Object> {
  public CachedUserDataCache(@NonNls String keyName) {
    super(keyName);
  }

  @Override
  protected final CachedValue<T> compute(final Owner owner, Object p) {
    return CachedValuesManager.getManager(getProject(owner)).createCachedValue(
      () -> CachedValueProvider.Result.create(computeValue(owner), getDependencies(owner)), false);
  }

  @Nullable
  protected abstract T computeValue(Owner owner);

  protected Object[] getDependencies(Owner owner) {
    return new Object[]{owner};
  }

  protected abstract Project getProject(Owner projectOwner);

  public final T get(Owner owner) {
    return get(owner, null).getValue();
  }
}