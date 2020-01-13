// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.hierarchy;

import com.intellij.ide.hierarchy.newAPI.HierarchyBrowserManager;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.jetbrains.lang.dart.DartTokenTypes.*;

public class DartHierarchyUtil {
  private static final Comparator<NodeDescriptor<?>> NODE_DESCRIPTOR_COMPARATOR = Comparator.comparingInt(NodeDescriptor::getIndex);

  private DartHierarchyUtil() {
  }

  @Nullable
  public static DartClass findDartClass(@NotNull final Project project, @NotNull final TypeHierarchyItem item) {
    final Element classElement = item.getClassElement();
    final Location location = classElement.getLocation();
    final DartComponent component = findDartComponent(project, location);
    return component instanceof DartClass ? (DartClass)component : null;
  }

  public static Comparator<NodeDescriptor<?>> getComparator(Project project) {
    final HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(project).getState();
    if (state != null && state.SORT_ALPHABETICALLY) {
      return AlphaComparator.INSTANCE;
    }
    else {
      return NODE_DESCRIPTOR_COMPARATOR;
    }
  }

  @NotNull
  public static List<TypeHierarchyItem> getTypeHierarchyItems(@NotNull DartClass dartClass) {
    final VirtualFile file = dartClass.getContainingFile().getVirtualFile();
    final DartComponentName name = dartClass.getComponentName();
    if (name == null) return Collections.emptyList();

    return DartAnalysisServerService.getInstance(dartClass.getProject())
      .search_getTypeHierarchy(file, name.getTextRange().getStartOffset(), false);
  }

  public static boolean isExecutable(@NotNull PsiElement component) {
    if (component instanceof DartComponentName) return false;
    final DartComponentType componentType = DartComponentType.typeOf(component);
    if (componentType == null) return false;
    switch (componentType) {
      case CONSTRUCTOR:
      case FUNCTION:
      case METHOD:
      case OPERATOR:
        return true;
      default:
        return false;
    }
  }

  public static PsiElement getResolvedElementAtCursor(DataContext dataContext) {
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    if (project == null || editor == null) return null;

    final PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    final PsiElement psiElement = file == null ? null : file.findElementAt(editor.getCaretModel().getOffset());
    DartReference dartReference = PsiTreeUtil.getParentOfType(psiElement, DartReference.class);
    if (dartReference != null) {
      if (dartReference.getTokenType() == NEW_EXPRESSION) {
        DartComponent cons = DartResolveUtil.findConstructorDeclaration((DartNewExpression)dartReference);
        if (cons != null && cons.getTokenType() == METHOD_DECLARATION) {
          return cons;
        }
        else {
          return null; // Class with no constructor.
        }
      }
      if (dartReference.getTokenType() == CALL_EXPRESSION) {
        dartReference = getRightmostReference(dartReference.getFirstChild());
      }
      DartComponent comp = DartResolveUtil.findReferenceAndComponentTarget(dartReference);
      return comp != null && isExecutable(comp) ? comp : null;
    }
    else {
      if (psiElement == null) return null;
      if (isExecutable(psiElement)) return psiElement;
      DartComponentName name = PsiTreeUtil.getParentOfType(psiElement, DartComponentName.class);
      if (name == null) {
        // Cursor may be between identifier and left paren of function definition.
        if (psiElement instanceof PsiWhiteSpace) {
          name = PsiTreeUtil.getPrevSiblingOfType(psiElement, DartComponentName.class);
        }
        else if ("(".equals(psiElement.getText())) {
          name = PsiTreeUtil.getPrevSiblingOfType(psiElement.getParent(), DartComponentName.class);
        }
      }
      if (name != null) {
        PsiElement def = name.getParent();
        return def != null && isExecutable(def) ? def : null;
      }
      return null;
    }
  }

  private static DartReference getRightmostReference(PsiElement element) {
    PsiElement last = PsiTreeUtil.getDeepestLast(element);
    return PsiTreeUtil.getParentOfType(last, DartReference.class);
  }

  @Nullable
  public static DartComponent findDartComponent(@NotNull final Project project, @NotNull final Location location) {
    String filePath = location.getFile();
    if (filePath == null) {
      return null;
    }
    filePath = FileUtil.toSystemIndependentName(filePath);

    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (virtualFile == null) {
      return null;
    }

    final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (psiFile == null) {
      return null;
    }

    final int offset = DartAnalysisServerService.getInstance(project).getConvertedOffset(virtualFile, location.getOffset());
    final PsiElement elementAtOffset = psiFile.findElementAt(offset);
    return PsiTreeUtil.getParentOfType(elementAtOffset, DartComponent.class);
  }
}
