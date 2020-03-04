// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.marker;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.DaemonBundle;
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
import java.util.List;

public class DartServerOverrideMarkerProvider implements LineMarkerProvider {
  @Override
  public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
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

    final List<DartServerData.DartOverrideMember> overrideMembers =
      DartAnalysisServerService.getInstance(componentName.getProject()).getOverrideMembers(virtualFile);
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
    PsiElement anchor = PsiTreeUtil.getDeepestFirst(componentName);

    return new LineMarkerInfo<>(anchor, anchor.getTextRange(), icon, __ -> {
                                      final DartClass superClass = PsiTreeUtil.getParentOfType(superComponent, DartClass.class);
                                      if (superClass == null) return "null";
                                      if (overrides) {
                                        return DartBundle.message(superclassComponent.isOperator() ? "overrides.operator.in"
                                                                                                   : "overrides.method.in",
                                                                  name,
                                                                  superClass.getName());
                                      }
                                      return DartBundle.message("implements.method.in", name, superClass.getName());
                                    }, (e, __) -> {
          List<DartComponent> superComponents = Lists.newArrayList();
          if (superclassComponent != null) {
            superComponents.add(superclassComponent);
          }
          superComponents.addAll(interfaceComponents);
          PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(superComponents),
                                              DaemonBundle.message("navigation.title.super.method", name),
                                              DaemonBundle.message("navigation.findUsages.title.super.method", name),
                                              new DefaultPsiElementCellRenderer());
        }, GutterIconRenderer.Alignment.LEFT);
  }
}
