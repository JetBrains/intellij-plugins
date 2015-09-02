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
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.marker.DartServerOverrideMarkerProvider;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.Element;
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
    final DartComponentType inComponentType = DartComponentType.typeOf(inComponent);
    // ask for the super type hierarchy
    final String filePath = file.getVirtualFile().getPath();
    final int offset = inComponent.getComponentName().getTextOffset();
    final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance().search_getTypeHierarchy(filePath, offset, true);
    // build list of DartComponent(s)
    final List<DartComponent> supers = Lists.newArrayList();
    boolean first = true;
    for (TypeHierarchyItem item : items) {
      // ignore the first, it is the seed element
      if (first) {
        first = false;
        continue;
      }
      // prepare Element for the current item
      final Element itemElement;
      if (inComponentType == DartComponentType.CLASS) {
        itemElement = item.getClassElement();
        // ignore Object
        if (itemElement != null && "Object".equals(itemElement.getName())) {
          continue;
        }
      }
      else {
        itemElement = item.getMemberElement();
      }
      if (itemElement == null) {
        continue;
      }
      // find the DartComponent
      final Location itemLocation = itemElement.getLocation();
      final DartComponent itemComponent =
        DartServerOverrideMarkerProvider.findDartComponent(project, itemLocation.getFile(), itemLocation.getOffset());
      if (itemComponent != null) {
        supers.add(itemComponent);
      }
    }
    // prepare the title
    final String title;
    if (inComponentType == DartComponentType.CLASS) {
      title = "Superclasses of " + inComponent.getName();
    }
    else {
      title = "Overridden members of superclasses";
    }
    // open DartComponent(s)
    final NavigatablePsiElement[] targets = DartResolveUtil.getComponentNames(supers).toArray(new NavigatablePsiElement[supers.size()]);
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
}
