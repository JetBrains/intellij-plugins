package com.jetbrains.lang.dart.util;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class DartRefactoringUtil {
  public static Set<String> collectUsedNames(PsiElement context) {
    return new THashSet<String>(ContainerUtil.map(collectUsedComponents(context), new Function<DartComponentName, String>() {
      @Nullable
      @Override
      public String fun(DartComponentName componentName) {
        return componentName.getName();
      }
    }));
  }

  public static Set<DartComponentName> collectUsedComponents(PsiElement context) {
    final Set<DartComponentName> usedComponentNames = new THashSet<DartComponentName>();
    PsiTreeUtil.treeWalkUp(new ComponentNameScopeProcessor(usedComponentNames), context, null, new ResolveState());
    return usedComponentNames;
  }

  @Nullable
  public static DartExpression getSelectedExpression(@NotNull final Project project,
                                                     @NotNull PsiFile file,
                                                     @NotNull final PsiElement element1,
                                                     @NotNull final PsiElement element2) {
    PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
    if (parent == null) {
      return null;
    }
    if (parent instanceof DartExpression) {
      return (DartExpression)parent;
    }
    return PsiTreeUtil.getParentOfType(parent, DartExpression.class);
  }

  @NotNull
  public static List<PsiElement> getOccurrences(@NotNull final PsiElement pattern, @Nullable final PsiElement context) {
    if (context == null) {
      return Collections.emptyList();
    }
    final List<PsiElement> occurrences = new ArrayList<PsiElement>();
    context.acceptChildren(new DartRecursiveVisitor() {
      public void visitElement(@NotNull final PsiElement element) {
        if (DartComponentType.typeOf(element) == DartComponentType.PARAMETER) {
          return;
        }
        if (PsiEquivalenceUtil.areElementsEquivalent(element, pattern)) {
          occurrences.add(element);
          return;
        }
        super.visitElement(element);
      }
    });
    return occurrences;
  }

  @Nullable
  public static PsiElement findOccurrenceUnderCaret(List<PsiElement> occurrences, Editor editor) {
    if (occurrences.isEmpty()) {
      return null;
    }
    int offset = editor.getCaretModel().getOffset();
    for (PsiElement occurrence : occurrences) {
      if (occurrence.getTextRange().contains(offset)) {
        return occurrence;
      }
    }
    int line = editor.getDocument().getLineNumber(offset);
    for (PsiElement occurrence : occurrences) {
      if (occurrence.isValid() && editor.getDocument().getLineNumber(occurrence.getTextRange().getStartOffset()) == line) {
        return occurrence;
      }
    }
    for (PsiElement occurrence : occurrences) {
      if (occurrence.isValid()) {
        return occurrence;
      }
    }
    return null;
  }

  public static PsiElement[] findStatementsInRange(PsiFile file, int startOffset, int endOffset) {
    PsiElement element1 = file.findElementAt(startOffset);
    PsiElement element2 = file.findElementAt(endOffset - 1);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
      element1 = file.findElementAt(startOffset);
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
      element2 = file.findElementAt(endOffset - 1);
    }

    if (element1 != null && element2 != null) {
      PsiElement commonParent = PsiTreeUtil.findCommonParent(element1, element2);
      if (commonParent instanceof DartExpression) {
        return new PsiElement[]{commonParent};
      }
    }

    final DartStatements statements = PsiTreeUtil.getParentOfType(element1, DartStatements.class);
    if (statements == null || element1 == null || element2 == null || !PsiTreeUtil.isAncestor(statements, element2, true)) {
      return PsiElement.EMPTY_ARRAY;
    }

    // don't forget about leafs (ex. ';')
    final ASTNode[] astResult = UsefulPsiTreeUtil.findChildrenRange(statements.getNode().getChildren(null), startOffset, endOffset);
    return ContainerUtil.map2Array(astResult, PsiElement.class, new Function<ASTNode, PsiElement>() {
      @Override
      public PsiElement fun(ASTNode node) {
        return node.getPsi();
      }
    });
  }

  @Nullable
  public static DartExpression findExpressionInRange(PsiFile file, int startOffset, int endOffset) {
    PsiElement element1 = file.findElementAt(startOffset);
    PsiElement element2 = file.findElementAt(endOffset - 1);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
    }
    DartExpression expression = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, DartExpression.class);
    if (expression == null || expression.getTextRange().getEndOffset() != endOffset) return null;
    if (expression instanceof DartReference && expression.getParent() instanceof DartCallExpression) return null;
    return expression;
  }
}
