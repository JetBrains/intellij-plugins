// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.containers.Stack;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.Angular2ModuleResolver;
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleStub;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class Angular2MetadataModule extends Angular2MetadataEntity<Angular2MetadataModuleStub> implements Angular2Module {

  private final Angular2ModuleResolver<Angular2MetadataModule> myModuleResolver = new Angular2ModuleResolver<>(
    () -> this, Angular2MetadataModule::collectSymbols);

  public Angular2MetadataModule(@NotNull Angular2MetadataModuleStub element) {
    super(element);
  }

  @Override
  @NotNull
  public Set<Angular2Declaration> getDeclarations() {
    return myModuleResolver.getDeclarations();
  }

  @Override
  @NotNull
  public Set<Angular2Module> getImports() {
    return myModuleResolver.getImports();
  }

  @Override
  @NotNull
  public Set<Angular2Entity> getExports() {
    return myModuleResolver.getExports();
  }

  @Override
  public boolean isScopeFullyResolved() {
    return myModuleResolver.isScopeFullyResolved();
  }

  @Override
  public boolean areExportsFullyResolved() {
    return myModuleResolver.areExportsFullyResolved();
  }

  private static <T extends Angular2Entity> Pair<Set<T>, Boolean> collectSymbols(Angular2MetadataModule source,
                                                                                  String propertyName,
                                                                                  Class<T> entityClass) {
    StubElement propertyStub = source.getStub().getModuleConfigPropertyValueStub(propertyName);
    if (propertyStub == null) {
      return Pair.pair(Collections.emptySet(), true);
    }
    boolean allResolved = true;
    Set<T> result = new HashSet<>();
    Stack<PsiElement> toResolve = new Stack<>(propertyStub.getPsi());
    while (!toResolve.empty()) {
      PsiElement element = toResolve.pop();
      if (element instanceof Angular2MetadataArray) {
        toResolve.addAll(asList(element.getChildren()));
      }
      else if (element instanceof Angular2MetadataReference) {
        toResolve.push(((Angular2MetadataReference)element).resolve());
      }
      else if (element != null
               && entityClass.isAssignableFrom(element.getClass())) {
        result.add(entityClass.cast(element));
      }
      else {
        allResolved = false;
      }
    }
    return Pair.pair(result, allResolved);
  }
}
