// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.actions;

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
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class DartServerGotoSuperHandler implements LanguageCodeInsightActionHandler {
  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
    final PsiElement at = psiFile.findElementAt(editor.getCaretModel().getOffset());
    final DartComponent inComponent = PsiTreeUtil.getParentOfType(at, DartComponent.class);
    final DartComponent inClass = PsiTreeUtil.getParentOfType(at, DartClass.class);
    if (inClass == null || inComponent == null || inComponent.getComponentName() == null) {
      return;
    }
    final boolean isInClass = inComponent instanceof DartClass;
    // ask for the super type hierarchy
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    final int offset = inComponent.getComponentName().getTextRange().getStartOffset();
    final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance(project).search_getTypeHierarchy(virtualFile, offset, true);
    // build list of DartComponent(s)
    final List<DartComponent> supers = new ArrayList<>();
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
    final DartComponent itemComponent = DartHierarchyUtil.findDartComponent(project, itemLocation);
    if (itemComponent != null) {
      supers.add(itemComponent);
    }
  }
}
