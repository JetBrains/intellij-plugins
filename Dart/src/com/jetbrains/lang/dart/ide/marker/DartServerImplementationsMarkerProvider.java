// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.marker;

import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.ide.actions.DartInheritorsSearcher;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class DartServerImplementationsMarkerProvider implements LineMarkerProvider {
  @Override
  public LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
    if (!(element instanceof DartComponentName name)) {
      return null;
    }
    return createMarker(name);
  }

  private static @Nullable LineMarkerInfo createMarker(final @NotNull DartComponentName name) {
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(name.getProject());
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    if (file == null || !file.isInLocalFileSystem()) {
      return null;
    }
    final int nameOffset = name.getTextRange().getStartOffset();
    final int nameLength = name.getTextLength();
    // ignore Object
    if ("Object".equals(name.getName())) {
      return null;
    }
    // classes
    for (DartServerData.DartRegion implementedClassRegion : service.getImplementedClasses(file)) {
      if (implementedClassRegion.getOffset() == nameOffset && implementedClassRegion.getLength() == nameLength) {
        return createMarkerClass(name);
      }
    }
    // members
    for (DartServerData.DartRegion implementedMemberRegion : service.getImplementedMembers(file)) {
      if (implementedMemberRegion.getOffset() == nameOffset && implementedMemberRegion.getLength() == nameLength) {
        return createMarkerMember(name);
      }
    }
    // not found
    return null;
  }

  private static @NotNull LineMarkerInfo createMarkerClass(final @NotNull DartComponentName name) {
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    PsiElement anchor = PsiTreeUtil.getDeepestFirst(name);
    return new LineMarkerInfo<>(anchor, anchor.getTextRange(), AllIcons.Gutter.OverridenMethod,
                                element -> DaemonBundle.message("class.is.subclassed.too.many"), (e, __) -> {
      DartAnalysisServerService das = DartAnalysisServerService.getInstance(name.getProject());
      final List<TypeHierarchyItem> items = das.search_getTypeHierarchy(file, anchor.getTextRange().getStartOffset(), false);
      if (items.isEmpty()) {
        return;
      }
      // TODO(scheglov) Consider using just Element(s), not PsiElement(s) for better performance
      final Set<DartComponent> components =
        DartInheritorsSearcher.getSubClasses(name.getProject(), GlobalSearchScope.allScope(name.getProject()), items);
      String popupTitle = DaemonBundle.message("navigation.title.subclass", name.getName(), components.size(), "");
      String findUsagesTitle = DartBundle.message("tab.title.subclasses.of.0", name.getName());
      PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(components), popupTitle, findUsagesTitle,
                                          new DefaultPsiElementCellRenderer());
    }, GutterIconRenderer.Alignment.RIGHT);
  }

  private static @NotNull LineMarkerInfo createMarkerMember(final @NotNull DartComponentName name) {
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    PsiElement anchor = PsiTreeUtil.getDeepestFirst(name);
    return new LineMarkerInfo<>(anchor, anchor.getTextRange(), AllIcons.Gutter.OverridenMethod,
                                element -> DaemonBundle.message("method.is.overridden.too.many"), (e, __) -> {
      DartAnalysisServerService das = DartAnalysisServerService.getInstance(name.getProject());
      final List<TypeHierarchyItem> items = das.search_getTypeHierarchy(file, anchor.getTextRange().getStartOffset(), false);
      if (items.isEmpty()) {
        return;
      }
      // TODO(scheglov) Consider using just Element(s), not PsiElement(s) for better performance
      final Set<DartComponent> components =
        DartInheritorsSearcher.getSubMembers(name.getProject(), GlobalSearchScope.allScope(name.getProject()), items);
      String popupTitle = DaemonBundle.message("navigation.title.overrider.method", name.getName(), components.size());
      String findUsagesTitle = DartBundle.message("tab.title.overriding.methods.of.0", name.getName());
      PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(components), popupTitle, findUsagesTitle,
                                          new DefaultPsiElementCellRenderer());
    }, GutterIconRenderer.Alignment.RIGHT);
  }
}
