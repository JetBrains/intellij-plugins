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
package com.jetbrains.lang.dart.ide.actions;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.marker.DartServerOverrideMarkerProvider;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartServerGotoSuperHandler implements LanguageCodeInsightActionHandler {
  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    final PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());
    final DartComponent inComponent = PsiTreeUtil.getParentOfType(at, DartComponent.class);
    final DartComponent inClass = PsiTreeUtil.getParentOfType(at, DartClass.class);
    if (inClass == null || inComponent == null || inComponent.getComponentName() == null) {
      return;
    }
    final boolean isInClass = inComponent instanceof DartClass;
    // ask for the super type hierarchy
    final VirtualFile virtualFile = file.getVirtualFile();
    final int offset = inComponent.getComponentName().getTextRange().getStartOffset();
    final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance().search_getTypeHierarchy(virtualFile, offset, true);
    // build list of DartComponent(s)
    final List<DartComponent> supers = Lists.newArrayList();
    if (!items.isEmpty()) {
      TypeHierarchyItem seed = items.get(0);
      {
        final Integer superIndex = seed.getSuperclass();
        if (superIndex != null) {
          final TypeHierarchyItem superItem = items.get(superIndex);
          addSuperComponent(project, supers, isInClass, superItem);
        }
      }
      for (int superIndex : seed.getMixins()) {
        final TypeHierarchyItem superItem = items.get(superIndex);
        addSuperComponent(project, supers, isInClass, superItem);
      }
      for (int superIndex : seed.getInterfaces()) {
        final TypeHierarchyItem superItem = items.get(superIndex);
        addSuperComponent(project, supers, isInClass, superItem);
      }
    }
    // prepare the title
    final String title;
    if (isInClass) {
      title = DartBundle.message("goto.super.class.chooser.title");
    }
    else {
      title = CodeInsightBundle.message("goto.super.method.chooser.title");
    }
    // open DartComponent(s)
    final NavigatablePsiElement[] targets = DartResolveUtil.getComponentNameArray(supers);
    PsiElementListNavigator.openTargets(editor, targets, title, null, new DefaultPsiElementCellRenderer());
  }

  @Override
  public boolean isValidFor(Editor editor, PsiFile file) {
    return file.getLanguage() == DartLanguage.INSTANCE;
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  private static void addSuperComponent(@NotNull Project project, List<DartComponent> supers, boolean isInClass, TypeHierarchyItem item) {
    // prepare Element for the current item
    final Element itemElement = isInClass ? item.getClassElement() : item.getMemberElement();
    if (itemElement == null) {
      return;
    }

    // ignore Object
    if (ElementKind.CLASS.equals(itemElement.getKind()) && "Object".equals(itemElement.getName())) {
      return;
    }

    // find the DartComponent
    final Location itemLocation = itemElement.getLocation();
    final DartComponent itemComponent = DartServerOverrideMarkerProvider.findDartComponent(project, itemLocation);
    if (itemComponent != null) {
      supers.add(itemComponent);
    }
  }
}
