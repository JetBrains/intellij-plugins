package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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

  private final Collection<LookupElement> myLookupElements;

  public HeaderParametersProvider(String... names) {
    myLookupElements = ContainerUtil.map2List(names, name -> {
      boolean directive = StringUtil.endsWithChar(name, ':');
      if (directive) name = name.substring(0, name.length() - 1);
      return LookupElementBuilder.create(name)
        .withCaseSensitivity(false)
        .withInsertHandler(directive ? DIRECTIVE_HANDLER : ATTRIBUTE_HANDLER);
    });
  }

  @Override
  public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
    result.addAllElements(myLookupElements);
  }
}
