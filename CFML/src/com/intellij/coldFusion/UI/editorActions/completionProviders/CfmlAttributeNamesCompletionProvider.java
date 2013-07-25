/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 17.03.2009
 * Time: 14:18:15
 * To change this template use File | Settings | File Templates.
 */
class CfmlAttributeNamesCompletionProvider extends CompletionProvider<CompletionParameters> {
  public void addCompletions(@NotNull final CompletionParameters parameters,
                             final ProcessingContext context,
                             @NotNull final CompletionResultSet result) {

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
    Set<String> excluded = new HashSet<String>();
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
        public int processTail(Editor editor, int tailOffset) {
          HighlighterIterator iterator = ((EditorEx)editor).getHighlighter().createIterator(tailOffset);
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
