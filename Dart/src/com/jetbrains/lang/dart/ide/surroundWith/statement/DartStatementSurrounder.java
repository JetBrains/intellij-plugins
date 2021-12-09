// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.surroundWith.statement;

import com.intellij.lang.ASTNode;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartStatementSurrounder implements Surrounder {
  @Override
  public boolean isApplicable(PsiElement @NotNull [] elements) {
    return true;
  }

  @Nullable
  protected PsiElement createSurrounder(@NotNull Project project) {
    return DartElementGenerator.createStatementFromText(
      project,
      getTemplateText()
    );
  }

  @Override
  @Nullable
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, PsiElement @NotNull [] elements)
    throws IncorrectOperationException {
    PsiElement parent = elements[0].getParent();

    PsiElement surrounder = createSurrounder(project);
    if (surrounder == null) {
      throw new IncorrectOperationException("Can't surround statements!");
    }

    surrounder = parent.addBefore(surrounder, elements[0]);
    final PsiElement elementToAdd = findElementToAdd(surrounder);

    if (elementToAdd == null) {
      parent.deleteChildRange(surrounder, surrounder);
      throw new IncorrectOperationException("Can't surround statements!");
    }

    for (PsiElement element : elements) {
      ASTNode node = element.getNode();
      final ASTNode copyNode = node.copyElement();

      parent.getNode().removeChild(node);
      elementToAdd.getNode().addChild(copyNode);
    }

    afterAdd(elementToAdd);
    int endOffset = cleanUpAndGetPlaceForCaret(surrounder);
    return TextRange.create(endOffset, endOffset);
  }

  protected void afterAdd(PsiElement elementToAdd) {
    final PsiElement newLineNode = PsiParserFacade.SERVICE.getInstance(elementToAdd.getProject()).createWhiteSpaceFromText("\n");
    elementToAdd.add(newLineNode);
  }

  protected abstract String getTemplateText();

  @Nullable
  protected abstract PsiElement findElementToAdd(@NotNull final PsiElement surrounder);

  protected abstract int cleanUpAndGetPlaceForCaret(@NotNull PsiElement surrounder);
}

