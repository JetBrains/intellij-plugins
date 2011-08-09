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

package com.intellij.lang.ognl;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.ognl.highlight.OgnlHighlighter;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlLanguage extends Language implements InjectableLanguage {

  public static final String ID = "OGNL";

  public static final OgnlLanguage INSTANCE = new OgnlLanguage();

  private OgnlLanguage() {
    super(ID);

    SyntaxHighlighterFactory.LANGUAGE_FACTORY
        .addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
          @NotNull
          protected SyntaxHighlighter createHighlighter() {
            return new OgnlHighlighter();
          }
        });
  }

  @Override
  public LanguageFileType getAssociatedFileType() {
    return OgnlFileType.INSTANCE;
  }

}
