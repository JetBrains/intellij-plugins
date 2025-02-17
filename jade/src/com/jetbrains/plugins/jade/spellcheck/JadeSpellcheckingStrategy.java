package com.jetbrains.plugins.jade.spellcheck;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlText;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeFileImpl;
import org.jetbrains.annotations.NotNull;

public final class JadeSpellcheckingStrategy extends SpellcheckingStrategy implements DumbAware {

  @Override
  public @NotNull Tokenizer getTokenizer(PsiElement element) {
    final ASTNode node = element.getNode();
    if (node == null || !(element.getContainingFile() instanceof JadeFileImpl)) {
      return EMPTY_TOKENIZER;
    }

    final IElementType type = node.getElementType();
    if (type == JadeElementTypes.COMMENT) {
      return myCommentTokenizer;
    }
    if (element instanceof XmlText) {
      return TEXT_TOKENIZER;
    }

    return EMPTY_TOKENIZER;
  }
}
