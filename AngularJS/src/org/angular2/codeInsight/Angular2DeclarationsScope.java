// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Module;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.containers.ContainerUtil.find;
import static java.util.Arrays.asList;

/**
 * Objects of this class should not be cached or stored. It is intended for single use.
 */
public class Angular2DeclarationsScope {

  private final NotNullLazyValue<Pair<Set<Angular2Declaration>, Boolean>> myScope;
  private final Map<Project, MultiMap<Angular2Declaration, Angular2Module>> myExport2NgModuleMap = new HashMap<>();

  public Angular2DeclarationsScope(@NotNull PsiElement element) {
    myScope = NotNullLazyValue.createValue(() -> {
      PsiFile file = element.getContainingFile();
      if (file == null) {
        return pair(null, false);
      }
      return CachedValuesManager.getCachedValue(file, () -> {
        Angular2Module module = doIfNotNull(Angular2EntitiesProvider.getComponent(
          Angular2IndexingHandler.findComponentClass(file)), c -> c.getModule());
        return CachedValueProvider.Result
          .create(module != null ? pair(module.getDeclarationsInScope(), module.isScopeFullyResolved()) : pair(null, false),
                  PsiModificationTracker.MODIFICATION_COUNT);
      });
    });
  }

  @Nullable
  public <T extends Angular2Declaration> Pair<T, DeclarationProximity> getClosestDeclaration(@NotNull Collection<T> declarations) {
    return StreamEx.of(declarations)
      .map(d -> pair(d, getDeclarationProximity(d)))
      .min(Comparator.comparing(p -> p.second))
      .orElse(null);
  }

  public boolean isFullyResolved() {
    return myScope.getValue().second;
  }

  public boolean contains(@NotNull Angular2Declaration declaration) {
    Set<Angular2Declaration> scope = myScope.getValue().first;
    return scope == null || scope.contains(declaration);
  }

  @NotNull
  public DeclarationProximity getDeclarationProximity(@NotNull Angular2Declaration declaration) {
    if (contains(declaration)) {
      return DeclarationProximity.IN_SCOPE;
    }
    if (find(myExport2NgModuleMap
               .computeIfAbsent(declaration.getSourceElement().getProject(),
                                p -> Angular2EntitiesProvider.getExportedDeclarationToModuleMap(p))
               .get(declaration),
             Angular2Module::isPublic) != null) {
      return DeclarationProximity.PUBLIC_MODULE_EXPORT;
    }
    return DeclarationProximity.DOES_NOT_EXIST;
  }

  @NotNull
  public DeclarationProximity getDeclarationsProximity(@NotNull Angular2Declaration... declarations) {
    return getDeclarationsProximity(asList(declarations));
  }

  @NotNull
  public DeclarationProximity getDeclarationsProximity(@NotNull Iterable<? extends Angular2Declaration> declarations) {
    if (myScope == null) {
      return DeclarationProximity.IN_SCOPE;
    }
    DeclarationProximity result = DeclarationProximity.DOES_NOT_EXIST;
    for (Angular2Declaration declaration : declarations) {
      switch (getDeclarationProximity(declaration)) {
        case IN_SCOPE:
          return DeclarationProximity.IN_SCOPE;
        case PUBLIC_MODULE_EXPORT:
          result = DeclarationProximity.PUBLIC_MODULE_EXPORT;
          break;
        case DOES_NOT_EXIST:
          break;
      }
    }
    return result;
  }

  public enum DeclarationProximity {
    IN_SCOPE,
    PUBLIC_MODULE_EXPORT,
    DOES_NOT_EXIST
  }
}
