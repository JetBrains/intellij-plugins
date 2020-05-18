// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeofType;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.util.containers.JBIterable;
import org.angular2.entities.*;
import org.angular2.entities.Angular2ModuleResolver.ResolvedEntitiesList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.lang.javascript.psi.types.TypeScriptTypeOfJSTypeImpl.getTypeOfResultElements;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2IvyModule extends Angular2IvyEntity<Angular2IvySymbolDef.Module> implements Angular2Module {

  private final Angular2ModuleResolver<TypeScriptField> myModuleResolver = new Angular2ModuleResolver<>(
    () -> getField(), Angular2IvyModule::collectSymbols);


  public Angular2IvyModule(@NotNull Angular2IvySymbolDef.Module entityDef) {
    super(entityDef);
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
  public boolean isPublic() {
    return !getName().startsWith("ɵ");
  }

  @Override
  public boolean areExportsFullyResolved() {
    return myModuleResolver.areExportsFullyResolved();
  }

  @Override
  public boolean areDeclarationsFullyResolved() {
    return myModuleResolver.areDeclarationsFullyResolved();
  }

  private static @NotNull <T extends Angular2Entity> Result<ResolvedEntitiesList<T>> collectSymbols(@NotNull TypeScriptField fieldDef,
                                                                                                    @NotNull String propertyName,
                                                                                                    @NotNull Class<T> symbolClazz) {
    Angular2IvySymbolDef.Module moduleDef = tryCast(Angular2IvySymbolDef.get(fieldDef, false), Angular2IvySymbolDef.Module.class);
    List<TypeScriptTypeofType> types = moduleDef == null ? Collections.emptyList() : moduleDef.getTypesList(propertyName);
    if (types.isEmpty()) {
      return ResolvedEntitiesList.createResult(Collections.emptySet(), true, fieldDef);
    }
    Set<T> entities = new HashSet<>();
    boolean fullyResolved = true;
    for (TypeScriptTypeofType typeOfType : types) {
      String reference = typeOfType.getReferenceText();
      if (reference == null) {
        fullyResolved = false;
        continue;
      }
      T entity = JBIterable.from(getTypeOfResultElements(typeOfType, reference))
        .filterMap(el -> Angular2EntitiesProvider.getEntity(el))
        .filter(symbolClazz)
        .first();
      if (entity == null) {
        fullyResolved = false;
      }
      else {
        entities.add(entity);
      }
    }
    return ResolvedEntitiesList.createResult(entities, fullyResolved, JSTypeUtils.getTypeInvalidationDependency());
  }
}
