// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class Angular2MetadataClassBase<Stub extends Angular2MetadataClassStubBase> extends Angular2MetadataElement<Stub> {
  public Angular2MetadataClassBase(@NotNull Stub element) {
    super(element);
  }

  @Nullable
  public TypeScriptClass getTypeScriptClass() {
    return getClassAndDependencies().first;
  }

  protected Pair<TypeScriptClass, Collection<Object>> getClassAndDependencies() {
    return getCachedValue(() -> {
      String className = getStub().getClassName();
      Angular2MetadataNodeModule nodeModule = ObjectUtils.tryCast(getParent(), Angular2MetadataNodeModule.class);
      Pair<PsiFile, TypeScriptClass> fileAndClass = className != null && nodeModule != null
                                                    ? nodeModule.locateFileAndClass(className)
                                                    : Pair.create(null, null);
      Collection<Object> dependencies = new HashSet<>();
      dependencies.add(getContainingFile());
      if (fileAndClass.second != null) {
        JSClassUtils.processClassesInHierarchy(fileAndClass.second, true, (aClass, typeSubstitutor, fromImplements) -> {
          dependencies.add(aClass.getContainingFile());
          return true;
        });
      }
      else if (fileAndClass.first != null) {
        dependencies.add(fileAndClass.first);
      }
      return CachedValueProvider.Result.create(Pair.create(fileAndClass.second, dependencies), dependencies);
    });
  }

  protected <T> T getCachedValue(@NotNull CachedValueProvider<T> provider) {
    return CachedValuesManager.getCachedValue(this, provider);
  }
}
