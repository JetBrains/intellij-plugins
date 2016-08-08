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
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.OverriddenMember;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

public class DartServerOverrideMarkerProvider implements LineMarkerProvider {

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
  }

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    if (!(element instanceof DartComponentName)) {
      return null;
    }
    final PsiElement parent = element.getParent();
    if (parent instanceof DartComponent) {
      final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
      return dartClass == null ? null : createOverrideMarker((DartComponentName)element);
    }
    return null;
  }

  private static void addDartComponent(List<DartComponent> components, Project project, OverriddenMember member) {
    final DartComponent component = member == null ? null : DartHierarchyUtil.findDartComponent(project, member.getElement().getLocation());
    if (component != null) {
      components.add(component);
    }
  }

  @Nullable
  private static LineMarkerInfo createOverrideMarker(DartComponentName componentName) {
    final VirtualFile virtualFile = componentName.getContainingFile().getVirtualFile();
    if (virtualFile == null || !virtualFile.isInLocalFileSystem()) {
      return null;
    }

    final List<DartServerData.DartOverrideMember> overrideMembers = DartAnalysisServerService.getInstance().getOverrideMembers(virtualFile);
    final Project project = componentName.getProject();
    final int nameOffset = componentName.getTextRange().getStartOffset();
    DartComponent superclassComponent = null;
    List<DartComponent> interfaceComponents = Lists.newArrayList();
    for (DartServerData.DartOverrideMember overrideMember : overrideMembers) {
      if (overrideMember.getOffset() == nameOffset) {
        final OverriddenMember member = overrideMember.getSuperclassMember();
        superclassComponent = member == null ? null : DartHierarchyUtil.findDartComponent(project, member.getElement().getLocation());
        if (overrideMember.getInterfaceMembers() != null) {
          for (OverriddenMember overriddenMember : overrideMember.getInterfaceMembers()) {
            addDartComponent(interfaceComponents, project, overriddenMember);
          }
        }
      }
    }
    return tryCreateOverrideMarker(componentName, superclassComponent, interfaceComponents);
  }

  @Nullable
  private static LineMarkerInfo tryCreateOverrideMarker(@NotNull final DartComponentName componentName,
                                                        @Nullable final DartComponent superclassComponent,
                                                        @NotNull final List<DartComponent> interfaceComponents) {
    if (superclassComponent == null && interfaceComponents.isEmpty()) {
      return null;
    }
    final String name = componentName.getName();
    final boolean overrides;
    final DartComponent superComponent;
    if (superclassComponent != null) {
      overrides = true;
      superComponent = superclassComponent;
    }
    else {
      overrides = false;
      superComponent = interfaceComponents.iterator().next();
    }
    final Icon icon = overrides ? AllIcons.Gutter.OverridingMethod : AllIcons.Gutter.ImplementingMethod;
    return new LineMarkerInfo<>(componentName, componentName.getTextRange(), icon, Pass.UPDATE_ALL,
                                element -> {
                                  final DartClass superClass = PsiTreeUtil.getParentOfType(superComponent, DartClass.class);
                                  if (superClass == null) return "null";
                                  if (overrides) {
                                    return superclassComponent.isOperator()
                                           ? DartBundle.message("overrides.operator.in", name, superClass.getName())
                                           : DartBundle.message("overrides.method.in", name, superClass.getName());
                                  }
                                  return DartBundle.message("implements.method.in", name, superClass.getName());
                                }, new GutterIconNavigationHandler<PsiElement>() {
      @Override
      public void navigate(MouseEvent e, PsiElement elt) {
        List<DartComponent> superComponents = Lists.newArrayList();
        if (superclassComponent != null) {
          superComponents.add(superclassComponent);
        }
        superComponents.addAll(interfaceComponents);
        PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(superComponents),
                                            DaemonBundle.message("navigation.title.super.method", name),
                                            DaemonBundle.message("navigation.findUsages.title.super.method", name),
                                            new DefaultPsiElementCellRenderer());
      }
    }, GutterIconRenderer.Alignment.LEFT);
  }
}
