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
package com.intellij.coldFusion.UI.highlighting;

import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.impl.CfmlAttributeImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlAnnotator implements Annotator {
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    final ASTNode elementNode = element.getNode();
    if (elementNode != null) {
      if (elementNode.getElementType() == CfscriptTokenTypes.IDENTIFIER &&
          element.getParent() instanceof CfmlAttributeImpl) {
        holder.createWeakWarningAnnotation(element, null).setTextAttributes(CfmlHighlighter.CfmlFileHighlighter.CFML_ATTRIBUTE);
      }
      if (elementNode.getElementType() == CfscriptTokenTypes.ACTION_NAME) {
        holder.createInfoAnnotation(elementNode, null).setTextAttributes(DefaultLanguageHighlighterColors.KEYWORD);
      }
    }
  }
}
