/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.marker;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DartServerImplementationsMarkerProvider implements LineMarkerProvider {

  @Override
  public void collectSlowLineMarkers(@NotNull final List<PsiElement> elements, @NotNull final Collection<LineMarkerInfo> result) {
  }

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    if (!(element instanceof DartComponentName)) {
      return null;
    }
    final DartComponentName name = (DartComponentName)element;
    return createMarker(name);
  }

  private static void addSubClasses(@NotNull final Project project,
                                    @NotNull final Set<TypeHierarchyItem> visited,
                                    @NotNull final List<TypeHierarchyItem> items,
                                    @NotNull final List<DartComponent> components,
                                    @NotNull final TypeHierarchyItem item,
                                    final boolean addItem) {
    if (!visited.add(item)) {
      return;
    }
    if (addItem) {
      final Element element = item.getClassElement();
      final Location location = element.getLocation();
      final DartComponent component = DartServerOverrideMarkerProvider.findDartComponent(project, location);
      if (component != null) {
        components.add(component);
      }
    }
    for (int subIndex : item.getSubclasses()) {
      final TypeHierarchyItem subItem = items.get(subIndex);
      addSubClasses(project, visited, items, components, subItem, true);
    }
  }

  private static void addSubMembers(@NotNull final Project project,
                                    @NotNull final Set<TypeHierarchyItem> visited,
                                    @NotNull final List<TypeHierarchyItem> items,
                                    @NotNull final List<DartComponent> components,
                                    @NotNull final TypeHierarchyItem item,
                                    final boolean addItem) {
    if (!visited.add(item)) {
      return;
    }
    if (addItem) {
      final Element element = item.getMemberElement();
      if (element != null) {
        final Location location = element.getLocation();
        final DartComponent component = DartServerOverrideMarkerProvider.findDartComponent(project, location);
        if (component != null) {
          components.add(component);
        }
      }
    }
    for (int subIndex : item.getSubclasses()) {
      final TypeHierarchyItem subItem = items.get(subIndex);
      addSubMembers(project, visited, items, components, subItem, true);
    }
  }

  @Nullable
  private static LineMarkerInfo createMarker(@NotNull final DartComponentName name) {
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance();
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    final int nameOffset = name.getTextOffset();
    final int nameLength = name.getTextLength();
    // ignore Object
    if ("Object".equals(name.getName())) {
      return null;
    }
    // classes
    for (ImplementedClass clazz : service.getImplementedClasses(file)) {
      if (clazz.getOffset() == nameOffset && clazz.getLength() == nameLength) {
        return createMarkerClass(name);
      }
    }
    // members
    for (ImplementedMember member : service.getImplementedMembers(file)) {
      if (member.getOffset() == nameOffset && member.getLength() == nameLength) {
        return createMarkerMember(name);
      }
    }
    // not found
    return null;
  }

  @NotNull
  private static LineMarkerInfo createMarkerClass(@NotNull final DartComponentName name) {
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    final int nameOffset = name.getTextOffset();
    return new LineMarkerInfo<PsiElement>(name, name.getTextRange(), AllIcons.Gutter.ImplementedMethod, Pass.UPDATE_ALL,
                                          new Function<PsiElement, String>() {
                                            @Override
                                            public String fun(PsiElement element) {
                                              return DaemonBundle.message("method.is.implemented.too.many");
                                            }
                                          }, new GutterIconNavigationHandler<PsiElement>() {
      @Override
      public void navigate(MouseEvent e, PsiElement elt) {
        final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance().search_getTypeHierarchy(file, nameOffset, false);
        if (items.isEmpty()) {
          return;
        }
        // TODO(scheglov) Consider using just Element(s), not PsiElement(s) for better performance
        final List<DartComponent> components = Lists.newArrayList();
        addSubClasses(name.getProject(), Sets.<TypeHierarchyItem>newHashSet(), items, components, items.get(0), false);
        PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(components),
                                            DaemonBundle.message("navigation.title.subclass", name.getName(), components.size()),
                                            "Implementations of " + name.getName(), new DefaultPsiElementCellRenderer());
      }
    }, GutterIconRenderer.Alignment.RIGHT);
  }

  @NotNull
  private static LineMarkerInfo createMarkerMember(@NotNull final DartComponentName name) {
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    final int nameOffset = name.getTextOffset();
    return new LineMarkerInfo<PsiElement>(name, name.getTextRange(), AllIcons.Gutter.ImplementedMethod, Pass.UPDATE_ALL,
                                          new Function<PsiElement, String>() {
                                            @Override
                                            public String fun(PsiElement element) {
                                              return DaemonBundle.message("method.is.implemented.too.many");
                                            }
                                          }, new GutterIconNavigationHandler<PsiElement>() {
      @Override
      public void navigate(MouseEvent e, PsiElement elt) {
        final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance().search_getTypeHierarchy(file, nameOffset, false);
        if (items.isEmpty()) {
          return;
        }
        // TODO(scheglov) Consider using just Element(s), not PsiElement(s) for better performance
        final List<DartComponent> components = Lists.newArrayList();
        addSubMembers(name.getProject(), Sets.<TypeHierarchyItem>newHashSet(), items, components, items.get(0), false);
        PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(components), DaemonBundle
                                              .message("navigation.title.implementation.method", name.getName(), components.size()), "Implementations of " + name.getName(),
                                            new DefaultPsiElementCellRenderer());
      }
    }, GutterIconRenderer.Alignment.RIGHT);
  }
}
