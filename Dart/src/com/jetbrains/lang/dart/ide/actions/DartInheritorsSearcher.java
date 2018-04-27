// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.actions;

import com.google.common.collect.Sets;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.Processor;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DartInheritorsSearcher extends QueryExecutorBase<PsiElement, DefinitionsScopedSearch.SearchParameters> {
  @Override
  public void processQuery(@NotNull final DefinitionsScopedSearch.SearchParameters parameters,
                           @NotNull final Processor<? super PsiElement> consumer) {
    final Ref<VirtualFile> fileRef = Ref.create();
    final Ref<Integer> offsetRef = Ref.create();
    final Ref<DartComponentType> componentTypeRef = Ref.create();

    prepare(parameters, fileRef, offsetRef, componentTypeRef);

    if (fileRef.isNull() || offsetRef.isNull() || componentTypeRef.isNull()) return;

    ApplicationManager.getApplication().runReadAction(() -> {
      final List<TypeHierarchyItem> hierarchyItems = CachedValuesManager.getCachedValue(parameters.getElement(), () -> {
        final DartAnalysisServerService das = DartAnalysisServerService.getInstance(parameters.getElement().getProject());
        final List<TypeHierarchyItem> items = das.search_getTypeHierarchy(fileRef.get(), offsetRef.get(), false);
        return new CachedValueProvider.Result<>(items, PsiModificationTracker.MODIFICATION_COUNT);
      });

      final Set<DartComponent> components = componentTypeRef.get() == DartComponentType.CLASS
                                            ? getSubClasses(parameters.getElement().getProject(), parameters.getScope(), hierarchyItems)
                                            : getSubMembers(parameters.getElement().getProject(), parameters.getScope(), hierarchyItems);
      for (DartComponent component : components) {
        consumer.process(component);
      }
    });
  }

  private static void prepare(@NotNull final DefinitionsScopedSearch.SearchParameters parameters,
                              @NotNull final Ref<VirtualFile> fileRef,
                              @NotNull final Ref<Integer> offsetRef,
                              @NotNull final Ref<DartComponentType> componentTypeRef) {
    ApplicationManager.getApplication().runReadAction(() -> {
      final PsiElement element = parameters.getElement();
      if (element.getLanguage() != DartLanguage.INSTANCE) return;

      final DartComponentType componentType = DartComponentType.typeOf(element);
      if (componentType != DartComponentType.CLASS &&
          componentType != DartComponentType.METHOD &&
          componentType != DartComponentType.OPERATOR) {
        return;
      }

      final DartComponentName componentName = element instanceof DartComponentName
                                              ? (DartComponentName)element
                                              : element instanceof DartComponent
                                                ? ((DartComponent)element).getComponentName()
                                                : null;
      final VirtualFile file = componentName == null ? null : DartResolveUtil.getRealVirtualFile(componentName.getContainingFile());
      if (file != null) {
        fileRef.set(file);
        offsetRef.set(componentName.getTextRange().getStartOffset());
        componentTypeRef.set(componentType);
      }
    });
  }

  @NotNull
  public static Set<DartComponent> getSubClasses(@NotNull final Project project,
                                                 @NotNull final SearchScope scope,
                                                 @NotNull final List<TypeHierarchyItem> hierarchyItems) {
    if (hierarchyItems.isEmpty()) return Collections.emptySet();

    final Set<DartComponent> result = new THashSet<>(hierarchyItems.size());
    addSubClasses(project, scope, Sets.newHashSet(), hierarchyItems, result, hierarchyItems.get(0), false);
    return result;
  }

  @NotNull
  public static Set<DartComponent> getSubMembers(@NotNull final Project project,
                                                 @NotNull final SearchScope scope,
                                                 @NotNull final List<TypeHierarchyItem> hierarchyItems) {
    if (hierarchyItems.isEmpty()) return Collections.emptySet();

    final Set<DartComponent> result = new THashSet<>(hierarchyItems.size());
    addSubMembers(project, scope, Sets.newHashSet(), hierarchyItems, result, hierarchyItems.get(0), false);
    return result;
  }

  private static void addSubClasses(@NotNull final Project project,
                                    @NotNull final SearchScope scope,
                                    @NotNull final Set<TypeHierarchyItem> visited,
                                    @NotNull final List<TypeHierarchyItem> hierarchyItems,
                                    @NotNull final Set<DartComponent> components,
                                    @NotNull final TypeHierarchyItem currentItem,
                                    final boolean addItem) {
    if (!visited.add(currentItem)) {
      return;
    }
    if (addItem) {
      final Element element = currentItem.getClassElement();
      final Location location = element.getLocation();
      final DartComponent component = DartHierarchyUtil.findDartComponent(project, location);
      if (component != null && isInScope(scope, component)) {
        components.add(component);
      }
    }
    for (int subIndex : currentItem.getSubclasses()) {
      final TypeHierarchyItem subItem = hierarchyItems.get(subIndex);
      addSubClasses(project, scope, visited, hierarchyItems, components, subItem, true);
    }
  }

  private static void addSubMembers(@NotNull final Project project,
                                    @NotNull final SearchScope scope,
                                    @NotNull final Set<TypeHierarchyItem> visited,
                                    @NotNull final List<TypeHierarchyItem> hierarchyItems,
                                    @NotNull final Set<DartComponent> components,
                                    @NotNull final TypeHierarchyItem currentItem,
                                    final boolean addItem) {
    if (!visited.add(currentItem)) {
      return;
    }
    if (addItem) {
      final Element element = currentItem.getMemberElement();
      if (element != null) {
        final Location location = element.getLocation();
        final DartComponent component = DartHierarchyUtil.findDartComponent(project, location);
        if (component != null && isInScope(scope, component)) {
          components.add(component);
        }
      }
    }
    for (int subIndex : currentItem.getSubclasses()) {
      final TypeHierarchyItem subItem = hierarchyItems.get(subIndex);
      addSubMembers(project, scope, visited, hierarchyItems, components, subItem, true);
    }
  }

  private static boolean isInScope(@NotNull final SearchScope scope, @NotNull final PsiElement element) {
    final VirtualFile file = element.getContainingFile().getVirtualFile();
    if (file == null) return false;
    return scope.contains(file);
  }
}
