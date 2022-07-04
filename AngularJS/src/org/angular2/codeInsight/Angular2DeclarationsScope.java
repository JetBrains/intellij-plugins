// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
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

/**
 * Objects of this class should not be cached or stored. It is intended for single use.
 */
public class Angular2DeclarationsScope {

  private final NotNullLazyValue<ScopeResult> myScope;
  private final Map<Project, MultiMap<Angular2Declaration, Angular2Module>> myExport2NgModuleMap = new HashMap<>();
  private final NotNullLazyValue<ProjectFileIndex> myFileIndex;

  public Angular2DeclarationsScope(@NotNull PsiElement element) {
    myScope = NotNullLazyValue.createValue(() -> {
      PsiFile file = element.getContainingFile();
      if (file == null) {
        return new ScopeResult(null, null, false);
      }
      return CachedValuesManager.getCachedValue(file, () -> {
        Angular2ImportsOwner importsOwner = null;
        Set<Angular2Declaration> declarations = null;
        boolean fullyResolved = false;

        var currentComponent = Angular2EntitiesProvider.getComponent(Angular2ComponentLocator.findComponentClass(file));
        if (currentComponent != null) {
          if (currentComponent.isStandalone()) {
            importsOwner = currentComponent;
            declarations = currentComponent.getDeclarationsInScope();
            fullyResolved = true;
          }
          else {
            var module = selectModule(currentComponent, file);
            if (module != null) {
              importsOwner = module;
              declarations = module.getDeclarationsInScope();
              fullyResolved = module.isScopeFullyResolved();
            }
          }
        }

        ScopeResult result = new ScopeResult(importsOwner, declarations, fullyResolved);
        return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
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

  public @Nullable Angular2ImportsOwner getImportsOwner() {
    return myScope.getValue().owner;
  }

  public boolean isFullyResolved() {
    return myScope.getValue().fullyResolved;
  }

  public boolean contains(@NotNull Angular2Declaration declaration) {
    Set<Angular2Declaration> declarations = myScope.getValue().declarations;
    return declarations == null || declarations.contains(declaration);
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

    if (declaration.isStandalone()) {
      return DeclarationProximity.IMPORTABLE;
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
      return DeclarationProximity.IMPORTABLE;
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
    IMPORTABLE, // standalone or exported by public module
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

  private static class ScopeResult {
    public final @Nullable Angular2ImportsOwner owner;
    public final @Nullable Set<Angular2Declaration> declarations;
    public final boolean fullyResolved;

    private ScopeResult(@Nullable Angular2ImportsOwner owner, @Nullable Set<Angular2Declaration> declarations, boolean fullyResolved) {
      this.owner = owner;
      this.declarations = declarations;
      this.fullyResolved = fullyResolved;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ScopeResult result = (ScopeResult)o;
      return fullyResolved == result.fullyResolved &&
             Objects.equals(owner, result.owner) &&
             Objects.equals(declarations, result.declarations);
    }

    @Override
    public int hashCode() {
      return Objects.hash(owner, declarations, fullyResolved);
    }

    @Override
    public String toString() {
      return "ScopeResult{" +
             "owner=" + owner +
             ", declarations=" + declarations +
             ", fullyResolved=" + fullyResolved +
             '}';
    }
  }
}
