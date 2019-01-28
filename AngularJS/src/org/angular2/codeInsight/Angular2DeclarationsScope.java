// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
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

public class Angular2DeclarationsScope {

  private final NullableLazyValue<Set<Angular2Declaration>> myScope;
  private final Map<Project, MultiMap<Angular2Declaration, Angular2Module>> myExport2NgModuleMap = new HashMap<>();

  public Angular2DeclarationsScope(@NotNull PsiElement element) {
    myScope = NullableLazyValue.createValue(() -> {
      Angular2Module module = doIfNotNull(Angular2EntitiesProvider.getComponent(
        Angular2IndexingHandler.findComponentClass(element)), c -> c.getModule());
      return module != null ? module.getDeclarationsInScope() : null;
    });
  }

  @Nullable
  public <T extends Angular2Declaration> Pair<T, DeclarationProximity> getClosestDeclaration(@NotNull Collection<T> declarations) {
    return StreamEx.of(declarations)
      .map(d -> pair(d, getDeclarationProximity(d)))
      .min(Comparator.comparing(p -> p.second))
      .orElse(null);
  }

  @NotNull
  public DeclarationProximity getDeclarationProximity(@NotNull Angular2Declaration declaration) {
    Set<Angular2Declaration> scope = myScope.getValue();
    if (scope == null || scope.contains(declaration)) {
      return DeclarationProximity.IN_SCOPE;
    }
    if (find(myExport2NgModuleMap
               .computeIfAbsent(declaration.getSourceElement().getProject(),
                                p -> Angular2EntitiesProvider.getExportedDeclarationToModuleMap(p))
               .get(declaration),
             Angular2Module::isPublic) != null) {
      return DeclarationProximity.PUBLIC_MODULE_EXPORT;
    }
    return DeclarationProximity.PRIVATE;
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
    DeclarationProximity result = DeclarationProximity.PRIVATE;
    for (Angular2Declaration declaration : declarations) {
      switch (getDeclarationProximity(declaration)) {
        case IN_SCOPE:
          return DeclarationProximity.IN_SCOPE;
        case PUBLIC_MODULE_EXPORT:
          result = DeclarationProximity.PUBLIC_MODULE_EXPORT;
          break;
        case PRIVATE:
          break;
      }
    }
    return result;
  }

  public enum DeclarationProximity {
    IN_SCOPE,
    PUBLIC_MODULE_EXPORT,
    PRIVATE
  }
}
