// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Soroka
 */
class HeaderParametersProvider extends CompletionProvider<CompletionParameters> {
  private static final InsertHandler<LookupElement> ATTRIBUTE_HANDLER = new InsertHandler<>() {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      context.setAddCompletionChar(false);
      EditorModificationUtil.insertStringAtCaret(context.getEditor(), "=");
      context.commitDocument();
    }
  };

  private static final InsertHandler<LookupElement> DIRECTIVE_HANDLER = new InsertHandler<>() {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      context.setAddCompletionChar(false);
      EditorModificationUtil.insertStringAtCaret(context.getEditor(), ":=");
      context.commitDocument();
    }
  };

  private final String[] myNames;

  HeaderParametersProvider(String... names) {
    myNames = names;
  }

  @Override
  public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
    for (String name : myNames) {
      boolean directive = StringUtil.endsWithChar(name, ':');
      if (directive) name = name.substring(0, name.length() - 1);
      result.addElement(LookupElementBuilder.create(name)
                          .withCaseSensitivity(false)
                          .withInsertHandler(directive ? DIRECTIVE_HANDLER : ATTRIBUTE_HANDLER));
    }
  }
}
