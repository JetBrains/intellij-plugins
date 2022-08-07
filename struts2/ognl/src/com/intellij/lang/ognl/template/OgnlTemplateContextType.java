/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.highlight.OgnlHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

/**
 * Provides live template context for OGNL.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlTemplateContextType extends TemplateContextType {

  public OgnlTemplateContextType() {
    super(OgnlLanguage.ID);
  }

  @Override
  public boolean isInContext(@NotNull final PsiFile psiFile, final int offset) {
    if (psiFile.getFileType() == OgnlFileType.INSTANCE) {
      return true;
    }

    return PsiUtilCore.getLanguageAtOffset(psiFile, offset) == OgnlLanguage.INSTANCE;
  }

  @Override
  public SyntaxHighlighter createHighlighter() {
    return new OgnlHighlighter();
  }

}