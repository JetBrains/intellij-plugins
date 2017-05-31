package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.*;
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
  private static final InsertHandler<LookupElement> ATTRIBUTE_HANDLER = new InsertHandler<LookupElement>() {
    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
      context.setAddCompletionChar(false);
      EditorModificationUtil.insertStringAtCaret(context.getEditor(), "=");
      context.commitDocument();
    }
  };

  private static final InsertHandler<LookupElement> DIRECTIVE_HANDLER = new InsertHandler<LookupElement>() {
    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
      context.setAddCompletionChar(false);
      EditorModificationUtil.insertStringAtCaret(context.getEditor(), ":=");
      context.commitDocument();
    }
  };

  private final String[] myNames;

  public HeaderParametersProvider(String... names) {
    myNames = names;
  }

  @Override
  public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
    for (String name : myNames) {
      boolean directive = StringUtil.endsWithChar(name, ':');
      if (directive) name = name.substring(0, name.length() - 1);
      result.addElement(LookupElementBuilder.create(name)
                          .withCaseSensitivity(false)
                          .withInsertHandler(directive ? DIRECTIVE_HANDLER : ATTRIBUTE_HANDLER));
    }
  }
}
