package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.wordSelection.AbstractWordSelectioner;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartTokenTypesSets;

public class DartDocLineCommentSelectionHandler extends AbstractWordSelectioner {

  public boolean canSelect(final PsiElement e) {
    final ASTNode astNode = e.getNode();
    return astNode != null && astNode.getElementType() == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT;
  }
}
