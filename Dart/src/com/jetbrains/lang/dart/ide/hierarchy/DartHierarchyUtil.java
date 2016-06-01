package com.jetbrains.lang.dart.ide.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.marker.DartServerOverrideMarkerProvider;
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

import static com.jetbrains.lang.dart.DartTokenTypes.CALL_EXPRESSION;
import static com.jetbrains.lang.dart.DartTokenTypes.METHOD_DECLARATION;
import static com.jetbrains.lang.dart.DartTokenTypes.NEW_EXPRESSION;

public class DartHierarchyUtil {
  private static final Comparator<NodeDescriptor> NODE_DESCRIPTOR_COMPARATOR = (first, second) -> first.getIndex() - second.getIndex();

  private DartHierarchyUtil() {
  }

  @Nullable
  public static DartClass findDartClass(@NotNull final Project project, @NotNull final TypeHierarchyItem item) {
    final Element classElement = item.getClassElement();
    final Location location = classElement.getLocation();
    final DartComponent component = DartServerOverrideMarkerProvider.findDartComponent(project, location);
    return component instanceof DartClass ? (DartClass)component : null;
  }

  public static Comparator<NodeDescriptor> getComparator(Project project) {
    if (HierarchyBrowserManager.getInstance(project).getState().SORT_ALPHABETICALLY) {
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

    return DartAnalysisServerService.getInstance().search_getTypeHierarchy(file, name.getTextRange().getStartOffset(), false);
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

  /// Find a method in a [aClass] that has the same name as the given [method]. Return null if not found.
  /// Fails and returns null if [method] is private and [aClass] is in a different library.
  public static DartMethodDeclaration findBaseMethodInClass(@NotNull DartMethodDeclaration method, @NotNull DartClass aClass, boolean checkBases) {
    String name = method.getName();
    if (name == null) return null;
    if (checkBases) {
      // TODO Implement
      return null;
    } else {
      DartComponent found = aClass.findMethodByName(name);
      return (DartMethodDeclaration)found;
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
      return comp != null && DartHierarchyUtil.isExecutable(comp) ? comp : null;
    }
    else {
      if (psiElement == null) return null;
      if (DartHierarchyUtil.isExecutable(psiElement)) return psiElement;
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
        return def != null && DartHierarchyUtil.isExecutable(def) ? def : null;
      }
      return null;
    }
  }

  private static DartReference getRightmostReference(PsiElement element) {
    PsiElement last = PsiTreeUtil.getDeepestLast(element);
    return PsiTreeUtil.getParentOfType(last, DartReference.class);
  }

}
