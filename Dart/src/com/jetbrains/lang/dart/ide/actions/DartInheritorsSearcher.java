package com.jetbrains.lang.dart.ide.actions;

import com.google.common.collect.Sets;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Processor;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.marker.DartServerOverrideMarkerProvider;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DartInheritorsSearcher extends QueryExecutorBase<PsiElement, DefinitionsScopedSearch.SearchParameters> {
  @Override
  public void processQuery(@NotNull final DefinitionsScopedSearch.SearchParameters parameters,
                           @NotNull final Processor<PsiElement> consumer) {
    final Ref<VirtualFile> fileRef = Ref.create();
    final Ref<Integer> offsetRef = Ref.create();
    final Ref<DartComponentType> componentTypeRef = Ref.create();

    prepare(parameters, fileRef, offsetRef, componentTypeRef);

    if (fileRef.isNull() || offsetRef.isNull() || componentTypeRef.isNull()) return;

    final List<TypeHierarchyItem> hierarchyItems =
      DartAnalysisServerService.getInstance().search_getTypeHierarchy(fileRef.get(), offsetRef.get(), false);

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        final List<DartComponent> components = componentTypeRef.get() == DartComponentType.CLASS
                                               ? getSubClasses(parameters.getElement().getProject(), parameters.getScope(), hierarchyItems)
                                               : getSubMembers(parameters.getElement().getProject(), parameters.getScope(), hierarchyItems);
        for (DartComponent component : components) {
          consumer.process(component);
        }
      }
    });
  }

  private static void prepare(@NotNull final DefinitionsScopedSearch.SearchParameters parameters,
                              @NotNull final Ref<VirtualFile> fileRef,
                              @NotNull final Ref<Integer> offsetRef,
                              @NotNull final Ref<DartComponentType> componentTypeRef) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
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
      }
    });
  }

  @NotNull
  public static List<DartComponent> getSubClasses(@NotNull final Project project,
                                                  @NotNull final SearchScope scope,
                                                  @NotNull final List<TypeHierarchyItem> hierarchyItems) {
    if (hierarchyItems.isEmpty()) return Collections.emptyList();

    final List<DartComponent> result = new ArrayList<DartComponent>(hierarchyItems.size());
    addSubClasses(project, scope, Sets.<TypeHierarchyItem>newHashSet(), hierarchyItems, result, hierarchyItems.get(0), false);
    return result;
  }

  @NotNull
  public static List<DartComponent> getSubMembers(@NotNull final Project project,
                                                  @NotNull final SearchScope scope,
                                                  @NotNull final List<TypeHierarchyItem> hierarchyItems) {
    if (hierarchyItems.isEmpty()) return Collections.emptyList();

    final List<DartComponent> result = new ArrayList<DartComponent>(hierarchyItems.size());
    addSubMembers(project, scope, Sets.<TypeHierarchyItem>newHashSet(), hierarchyItems, result, hierarchyItems.get(0), false);
    return result;
  }

  private static void addSubClasses(@NotNull final Project project,
                                    @NotNull final SearchScope scope,
                                    @NotNull final Set<TypeHierarchyItem> visited,
                                    @NotNull final List<TypeHierarchyItem> hierarchyItems,
                                    @NotNull final List<DartComponent> components,
                                    @NotNull final TypeHierarchyItem currentItem,
                                    final boolean addItem) {
    if (!visited.add(currentItem)) {
      return;
    }
    if (addItem) {
      final Element element = currentItem.getClassElement();
      final Location location = element.getLocation();
      final DartComponent component = DartServerOverrideMarkerProvider.findDartComponent(project, location);
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
                                    @NotNull final List<DartComponent> components,
                                    @NotNull final TypeHierarchyItem currentItem,
                                    final boolean addItem) {
    if (!visited.add(currentItem)) {
      return;
    }
    if (addItem) {
      final Element element = currentItem.getMemberElement();
      if (element != null) {
        final Location location = element.getLocation();
        final DartComponent component = DartServerOverrideMarkerProvider.findDartComponent(project, location);
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
    return scope instanceof LocalSearchScope ? ((LocalSearchScope)scope).isInScope(file)
                                             : ((GlobalSearchScope)scope).contains(file);
  }
}
