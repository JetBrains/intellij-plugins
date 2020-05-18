// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.Angular2ModuleResolver;
import org.angular2.entities.Angular2ModuleResolver.ResolvedEntitiesList;
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleStub;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Angular2MetadataModule extends Angular2MetadataEntity<Angular2MetadataModuleStub> implements Angular2Module {

  private final Angular2ModuleResolver<Angular2MetadataModule> myModuleResolver = new Angular2ModuleResolver<>(
    () -> this, Angular2MetadataModule::collectSymbols);

  public Angular2MetadataModule(@NotNull Angular2MetadataModuleStub element) {
    super(element);
  }

  @Override
  public @NotNull Set<Angular2Declaration> getDeclarations() {
    return myModuleResolver.getDeclarations();
  }

  @Override
  public @NotNull Set<Angular2Module> getImports() {
    return myModuleResolver.getImports();
  }

  @Override
  public @NotNull Set<Angular2Entity> getExports() {
    return myModuleResolver.getExports();
  }

  @Override
  public @NotNull Set<Angular2Declaration> getAllExportedDeclarations() {
    return myModuleResolver.getAllExportedDeclarations();
  }

  @Override
  public boolean isScopeFullyResolved() {
    return myModuleResolver.isScopeFullyResolved();
  }

  @Override
  public boolean areExportsFullyResolved() {
    return myModuleResolver.areExportsFullyResolved();
  }

  @Override
  public boolean areDeclarationsFullyResolved() {
    return myModuleResolver.areDeclarationsFullyResolved();
  }

  @Override
  public boolean isPublic() {
    //noinspection HardCodedStringLiteral
    return getStub().getMemberName() == null
           || !getStub().getMemberName().startsWith("ɵ");
  }

  private static <T extends Angular2Entity> Result<ResolvedEntitiesList<T>> collectSymbols(@NotNull Angular2MetadataModule source,
                                                                                           @NotNull String propertyName,
                                                                                           @NotNull Class<T> entityClass) {
    StubElement propertyStub = source.getStub().getDecoratorFieldValueStub(propertyName);
    if (propertyStub == null) {
      return ResolvedEntitiesList.createResult(Collections.emptySet(), true, source);
    }
    Ref<Boolean> allResolved = new Ref<>(true);
    Set<T> result = new HashSet<>();
    Set<PsiElement> cacheDependencies = new HashSet<>();
    collectReferencedElements(propertyStub.getPsi(), element -> {
      if (element != null
          && entityClass.isAssignableFrom(element.getClass())) {
        result.add(entityClass.cast(element));
      }
      else {
        allResolved.set(false);
      }
    }, cacheDependencies);
    return ResolvedEntitiesList.createResult(result, allResolved.get(), cacheDependencies);
  }
}
