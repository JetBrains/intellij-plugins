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

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.ide.actions.DartInheritorsSearcher;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

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

  @Nullable
  private static LineMarkerInfo createMarker(@NotNull final DartComponentName name) {
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

  @NotNull
  private static LineMarkerInfo createMarkerClass(@NotNull final DartComponentName name) {
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    final int nameOffset = name.getTextRange().getStartOffset();
    return new LineMarkerInfo<>(name, name.getTextRange(), AllIcons.Gutter.OverridenMethod, Pass.LINE_MARKERS,
                                element -> DaemonBundle.message("class.is.subclassed.too.many"),
                                new GutterIconNavigationHandler<PsiElement>() {
                                  @Override
                                  public void navigate(MouseEvent e, PsiElement elt) {
                                    final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance(name.getProject())
                                      .search_getTypeHierarchy(file, nameOffset, false);
                                    if (items.isEmpty()) {
                                      return;
                                    }
                                    // TODO(scheglov) Consider using just Element(s), not PsiElement(s) for better performance
                                    final List<DartComponent> components =
                                      DartInheritorsSearcher
                                        .getSubClasses(name.getProject(), GlobalSearchScope.allScope(name.getProject()), items);
                                    PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(components),
                                                                        DaemonBundle.message("navigation.title.subclass", name.getName(),
                                                                                             components.size(), ""),
                                                                        "Subclasses of " + name.getName(),
                                                                        new DefaultPsiElementCellRenderer());
                                  }
                                }, GutterIconRenderer.Alignment.RIGHT);
  }

  @NotNull
  private static LineMarkerInfo createMarkerMember(@NotNull final DartComponentName name) {
    final VirtualFile file = name.getContainingFile().getVirtualFile();
    final int nameOffset = name.getTextRange().getStartOffset();
    return new LineMarkerInfo<>(name, name.getTextRange(), AllIcons.Gutter.OverridenMethod, Pass.LINE_MARKERS,
                                element -> DaemonBundle.message("method.is.overridden.too.many"),
                                new GutterIconNavigationHandler<PsiElement>() {
                                  @Override
                                  public void navigate(MouseEvent e, PsiElement elt) {
                                    final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance(name.getProject())
                                      .search_getTypeHierarchy(file, nameOffset, false);
                                    if (items.isEmpty()) {
                                      return;
                                    }
                                    // TODO(scheglov) Consider using just Element(s), not PsiElement(s) for better performance
                                    final List<DartComponent> components =
                                      DartInheritorsSearcher
                                        .getSubMembers(name.getProject(), GlobalSearchScope.allScope(name.getProject()), items);
                                    PsiElementListNavigator.openTargets(e, DartResolveUtil.getComponentNameArray(components),
                                                                        DaemonBundle
                                                                          .message("navigation.title.overrider.method", name.getName(),
                                                                                   components.size()),
                                                                        "Overriding methods of " + name.getName(),
                                                                        new DefaultPsiElementCellRenderer());
                                  }
                                }, GutterIconRenderer.Alignment.RIGHT);
  }
}
