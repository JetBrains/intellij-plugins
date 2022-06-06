// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.entities.*;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;

/**
 * Objects of this class should not be cached or stored. It is intended for single use.
 */
public class Angular2DeclarationsScope {

  private final NotNullLazyValue<Trinity<Angular2Module, Set<Angular2Declaration>, Boolean>> myScope;
  private final Map<Project, MultiMap<Angular2Declaration, Angular2Module>> myExport2NgModuleMap = new HashMap<>();
  private final NotNullLazyValue<ProjectFileIndex> myFileIndex;

  public Angular2DeclarationsScope(@NotNull PsiElement element) {
    myScope = NotNullLazyValue.createValue(() -> {
      PsiFile file = element.getContainingFile();
      if (file == null) {
        return Trinity.create(null, null, false);
      }
      return CachedValuesManager.getCachedValue(file, () -> {
        Angular2Module module = doIfNotNull(Angular2EntitiesProvider.getComponent(Angular2ComponentLocator.findComponentClass(file)),
                                            c -> selectModule(c, file));
        return CachedValueProvider.Result.create(
          module != null ? Trinity.create(module, module.getDeclarationsInScope(), module.isScopeFullyResolved())
                         : Trinity.create(null, null, false),
          PsiModificationTracker.MODIFICATION_COUNT);
      });
    });
    myFileIndex = NotNullLazyValue.createValue(
      () -> ProjectRootManager.getInstance(element.getProject()).getFileIndex());
  }

  public @Nullable <T extends Angular2Declaration> Pair<T, DeclarationProximity> getClosestDeclaration(@NotNull Collection<T> declarations) {
    return StreamEx.of(declarations)
      .map(d -> pair(d, getDeclarationProximity(d)))
      .min(Pair.comparingBySecond())
      .orElse(null);
  }

  public @Nullable Angular2Module getModule() {
    return myScope.getValue().first;
  }

  public boolean isFullyResolved() {
    return myScope.getValue().third;
  }

  public boolean contains(@NotNull Angular2Declaration declaration) {
    Set<Angular2Declaration> scope = myScope.getValue().second;
    return scope == null || scope.contains(declaration);
  }

  public List<Angular2Module> getPublicModulesExporting(@NotNull Angular2Declaration declaration) {
    return ContainerUtil.filter(myExport2NgModuleMap
                                  .computeIfAbsent(declaration.getSourceElement().getProject(),
                                                   p -> Angular2EntitiesProvider.getExportedDeclarationToModuleMap(p))
                                  .get(declaration),
                                module -> module.isPublic() && module.getTypeScriptClass() != null);
  }

  public @NotNull DeclarationProximity getDeclarationProximity(@NotNull Angular2Declaration declaration) {
    if (contains(declaration)) {
      return DeclarationProximity.IN_SCOPE;
    }
    Collection<Angular2Module> modules = myExport2NgModuleMap
      .computeIfAbsent(declaration.getSourceElement().getProject(),
                       p -> Angular2EntitiesProvider.getExportedDeclarationToModuleMap(p))
      .get(declaration);
    if (modules.isEmpty()) {
      if (!isInSource(declaration)) {
        return DeclarationProximity.NOT_REACHABLE;
      }
      return declaration.getAllDeclaringModules().isEmpty()
             ? DeclarationProximity.NOT_DECLARED_IN_ANY_MODULE
             : DeclarationProximity.NOT_EXPORTED_BY_MODULE;
    }
    else if (ContainerUtil.exists(modules, Angular2Module::isPublic)) {
      return DeclarationProximity.EXPORTED_BY_PUBLIC_MODULE;
    }
    return DeclarationProximity.NOT_REACHABLE;
  }

  public @NotNull DeclarationProximity getDeclarationsProximity(@NotNull Iterable<? extends Angular2Declaration> declarations) {
    if (myScope == null) {
      return DeclarationProximity.IN_SCOPE;
    }
    DeclarationProximity result = DeclarationProximity.NOT_REACHABLE;
    for (Angular2Declaration declaration : declarations) {
      DeclarationProximity current = getDeclarationProximity(declaration);
      if (current == DeclarationProximity.IN_SCOPE) {
        return DeclarationProximity.IN_SCOPE;
      }
      if (current.ordinal() < result.ordinal()) {
        result = current;
      }
    }
    return result;
  }

  public boolean isInSource(@NotNull Angular2Entity entity) {
    if (entity instanceof Angular2MetadataEntity
        || entity.getDecorator() == null) {
      return false;
    }
    PsiFile file = entity.getDecorator().getContainingFile();
    if (file == null) {
      return false;
    }
    VirtualFile vf = file.getViewProvider().getVirtualFile();
    return myFileIndex.getValue().isInContent(vf)
           && !myFileIndex.getValue().isInLibrary(vf);
  }

  public enum DeclarationProximity {
    IN_SCOPE,
    EXPORTED_BY_PUBLIC_MODULE,
    NOT_DECLARED_IN_ANY_MODULE,
    NOT_EXPORTED_BY_MODULE,
    NOT_REACHABLE
  }

  private static @Nullable Angular2Module selectModule(@NotNull Angular2Component component, @NotNull PsiFile context) {
    Collection<Angular2Module> modules = component.getAllDeclaringModules();
    if (modules.size() > 1) {
      for (Angular2FrameworkHandler handler : Angular2FrameworkHandler.EP_NAME.getExtensionList()) {
        Angular2Module result = handler.selectModuleForDeclarationsScope(modules, component, context);
        if (result != null) {
          return result;
        }
      }
      return Angular2EntityUtils.defaultChooseModule(modules.stream());
    }
    return ContainerUtil.getFirstItem(modules);
  }
}
