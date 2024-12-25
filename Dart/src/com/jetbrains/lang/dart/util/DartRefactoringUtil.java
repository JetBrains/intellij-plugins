// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.util;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.ComponentNameScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DartRefactoringUtil {
  public static Set<String> collectUsedNames(PsiElement context) {
    return new HashSet<>(ContainerUtil.map(collectUsedComponents(context), NavigationItem::getName));
  }

  public static Set<DartComponentName> collectUsedComponents(PsiElement context) {
    final Set<DartComponentName> usedComponentNames = new HashSet<>();
    PsiTreeUtil.treeWalkUp(new ComponentNameScopeProcessor(usedComponentNames), context, null, ResolveState.initial());
    return usedComponentNames;
  }

  public static @Nullable DartExpression getSelectedExpression(final @NotNull Project project,
                                                               @NotNull PsiFile file,
                                                               final @NotNull PsiElement element1,
                                                               final @NotNull PsiElement element2) {
    PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
    if (parent == null) {
      return null;
    }
    if (parent instanceof DartExpression) {
      return (DartExpression)parent;
    }
    return PsiTreeUtil.getParentOfType(parent, DartExpression.class);
  }

  public static @NotNull List<PsiElement> getOccurrences(final @NotNull PsiElement pattern, final @Nullable PsiElement context) {
    if (context == null) {
      return Collections.emptyList();
    }
    final List<PsiElement> occurrences = new ArrayList<>();
    context.acceptChildren(new DartRecursiveVisitor() {
      @Override
      public void visitElement(final @NotNull PsiElement element) {
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

  public static PsiElement @NotNull [] findStatementsInRange(PsiFile file, int startOffset, int endOffset) {
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
    if (statements == null || element2 == null || !PsiTreeUtil.isAncestor(statements, element2, true)) {
      return PsiElement.EMPTY_ARRAY;
    }

    // don't forget about leafs (ex. ';')
    final ASTNode[] astResult = UsefulPsiTreeUtil.findChildrenRange(statements.getNode().getChildren(null), startOffset, endOffset);
    return ContainerUtil.map2Array(astResult, PsiElement.class, ASTNode::getPsi);
  }

  public static @Nullable DartExpression findExpressionInRange(PsiFile file, int startOffset, int endOffset) {
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

  public static PsiElement @NotNull [] findListExpressionInRange(@NotNull PsiFile file, int startOffset, int endOffset) {
    // startOffset and endOffset are at the beginning of lines
    // return an expression that spans those lines, plus optional comma if any
    PsiElement element1 = file.findElementAt(startOffset);
    PsiElement element2 = file.findElementAt(endOffset - 1);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
    }
    DartExpression expression = PsiTreeUtil.findElementOfClassAtRange(file, startOffset, endOffset, DartExpression.class);
    if (expression == null) {
      return PsiElement.EMPTY_ARRAY;
    }
    if (expression.getTextRange().getEndOffset() != endOffset) {
      element2 = file.findElementAt(endOffset - 1);
      if (element2 != null && isComma(element2)) {
        PsiElement prev = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(element2, true);
        if (prev instanceof DartExpressionList) {
          prev = prev.getLastChild();
        }
        else if (prev instanceof DartNamedArgument && prev == expression.getParent()) {
          return new PsiElement[]{prev, element2};
        }
        if (prev == expression) {
          return new PsiElement[]{expression, element2};
        }
        else {
          return PsiElement.EMPTY_ARRAY;
        }
      }
      else {
        return PsiElement.EMPTY_ARRAY;
      }
    }
    return new PsiElement[]{expression};
  }

  public static boolean isRightBracket(@NotNull PsiElement element) {
    return element.getNode().getElementType() == DartTokenTypes.RBRACKET;
  }

  public static boolean isRightParen(@NotNull PsiElement element) {
    return element.getNode().getElementType() == DartTokenTypes.RPAREN;
  }

  public static boolean isComma(@NotNull PsiElement element) {
    return element.getNode().getElementType() == DartTokenTypes.COMMA;
  }
}
