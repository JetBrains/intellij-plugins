// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

public class Angular2MetadataClassBase<Stub extends Angular2MetadataClassStubBase> extends Angular2MetadataElement<Stub> {
  public Angular2MetadataClassBase(@NotNull Stub element) {
    super(element);
  }

  @Nullable
  public TypeScriptClass getTypeScriptClass() {
    return getClassAndDependencies().first;
  }

  @NotNull
  @Override
  public String getName() {
    return getCachedClassBasedValue(cls -> cls != null
                                           ? cls.getName()
                                           : StringUtil.notNullize(getStub().getMemberName(), "<unnamed>"));
  }

  public Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase> getExtendedClass() {
    Angular2MetadataReferenceStub refStub = getStub().getExtendsReference();
    if (refStub != null) {
      //noinspection unchecked
      return ObjectUtils.tryCast(refStub.getPsi().resolve(), Angular2MetadataClassBase.class);
    }
    return null;
  }

  protected Pair<TypeScriptClass, Collection<Object>> getClassAndDependencies() {
    return CachedValuesManager.getCachedValue(this, () -> {
      String className = getStub().getClassName();
      Angular2MetadataNodeModule nodeModule = getNodeModule();
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

  protected <T> T getCachedClassBasedValue(Function<TypeScriptClass, T> provider) {
    return CachedValuesManager.getCachedValue(
      this,
      CachedValuesManager.getManager(getProject()).getKeyForClass(provider.getClass()),
      () -> {
        Pair<TypeScriptClass, Collection<Object>> dependencies = getClassAndDependencies();
        return CachedValueProvider.Result
          .create(provider.apply(dependencies.first), dependencies.second);
      });
  }
}
