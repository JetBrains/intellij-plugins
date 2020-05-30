// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

public abstract class Angular2SourceEntityBase extends UserDataHolderBase implements Angular2Entity {

  private static Object NULL_MARK = new Object();

  protected final TypeScriptClass myClass;

  protected Angular2SourceEntityBase(@NotNull TypeScriptClass aClass) {
    myClass = aClass;
  }

  @Override
  public @NotNull TypeScriptClass getTypeScriptClass() {
    return myClass;
  }

  @Override
  public @NotNull String getName() {
    return StringUtil.notNullize(myClass.getName(), Angular2Bundle.message("angular.description.unnamed"));
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  protected <T> T getCachedValue(@NotNull CachedValueProvider<T> provider) {
    return CachedValuesManager.getManager(myClass.getProject()).getCachedValue(this, provider);
  }

  /**
   * Since Ivy entities are cached on TypeScriptClass dependencies, we can avoid caching for values depending solely on class contents.
   */
  protected <T> @NotNull T getLazyValue(Key<T> key, @NotNull Supplier<@NotNull ? extends T> provider) {
    T result = getUserData(key);
    return result != null ? result : putUserDataIfAbsent(key, provider.get());
  }

  /**
   * Since Ivy entities are cached on TypeScriptClass dependencies, we can avoid caching for values depending solely on class contents.
   */
  protected <T> @Nullable T getNullableLazyValue(Key<T> key, @NotNull Supplier<@Nullable ? extends T> provider) {
    T result = getUserData(key);
    if (result == NULL_MARK) {
      return null;
    }
    if (result == null) {
      result = provider.get();
      if (result == null) {
        //noinspection unchecked
        putUserDataIfAbsent(key, (T)NULL_MARK);
      }
      else {
        return putUserDataIfAbsent(key, result);
      }
    }
    return result;
  }

  protected @NotNull Collection<Object> getClassModificationDependencies() {
    return getCachedValue(() -> {
      Collection<Object> dependencies = new HashSet<>();
      JSClassUtils.processClassesInHierarchy(myClass, true, (aClass, typeSubstitutor, fromImplements) -> {
        dependencies.add(aClass);
        return true;
      });
      return CachedValueProvider.Result.create(dependencies, dependencies);
    });
  }
}
