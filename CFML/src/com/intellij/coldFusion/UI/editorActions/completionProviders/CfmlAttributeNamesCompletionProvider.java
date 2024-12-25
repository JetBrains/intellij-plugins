// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.info.CfmlAttributeDescription;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.coldFusion.model.psi.impl.CfmlPropertyImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

class CfmlAttributeNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
  @Override
  public void addCompletions(final @NotNull CompletionParameters parameters,
                             final @NotNull ProcessingContext context,
                             final @NotNull CompletionResultSet result) {

    PsiElement element = parameters.getPosition();
    String tagName = "";
    while (element != null && !(element instanceof CfmlTag) && !(element instanceof CfmlComponent) &&
           !(element instanceof CfmlPropertyImpl)) {
      PsiElement prevNode = element.getPrevSibling();
      PsiElement superPrevNode = prevNode != null ? prevNode.getPrevSibling() : null;
      if (superPrevNode != null && superPrevNode.getText().equalsIgnoreCase("property")) {
        tagName = "cfproperty";
        break;
      }
      element = element.getParent();
    }
    if (element == null) {
      return;
    }
    if (tagName.isEmpty()) {
      tagName =
        element instanceof CfmlTag ? ((CfmlTag)element).getTagName() : element instanceof CfmlPropertyImpl ? "cfproperty" : "cfcomponent";
    }
    Set<String> excluded = new HashSet<>();
    final CfmlAttributeImpl[] attributes = PsiTreeUtil.getChildrenOfType(element, CfmlAttributeImpl.class);
    if (attributes != null) {
      for (CfmlAttributeImpl attribute : attributes) {
        excluded.add(attribute.getAttributeName());
      }
    }
    for (CfmlAttributeDescription s : CfmlUtil.getAttributes(tagName, element.getProject())) {
      if (s.getName() == null) {
        continue;
      }
      if (excluded.contains(s.getName())) {
        continue;
      }
      result.addElement(TailTypeDecorator.withTail(LookupElementBuilder.create(s.getName()).
        withCaseSensitivity(false), new TailType() {
        @Override
        public int processTail(Editor editor, int tailOffset) {
          HighlighterIterator iterator = editor.getHighlighter().createIterator(tailOffset);
          if (!iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.WHITE_SPACE) iterator.advance();
          if (!iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.ASSIGN) {
            iterator.advance();
          }
          else {
            editor.getDocument().insertString(tailOffset, "=\"\"");
            return moveCaret(editor, tailOffset, 2);
          }
          int offset = iterator.getStart();
          if (!iterator.atEnd() && iterator.getTokenType() == CfmlTokenTypes.WHITE_SPACE) iterator.advance();
          if (!iterator.atEnd() && CfmlTokenTypes.STRING_ELEMENTS.contains(iterator.getTokenType())) {
            return tailOffset;
          }

          editor.getDocument().insertString(offset, "\"\"");
          return moveCaret(editor, tailOffset, offset - tailOffset + 1);
        }
      }));
    }
  }
}
