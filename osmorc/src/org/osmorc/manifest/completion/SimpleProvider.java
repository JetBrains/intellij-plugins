package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Soroka
 */
class SimpleProvider extends CompletionProvider<CompletionParameters> {
  private final String[] myItems;

  SimpleProvider(String... items) {
    myItems = items;
  }

  @Override
  public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
    for (String item : myItems) {
      result.addElement(LookupElementBuilder.create(item).withCaseSensitivity(false));
    }
  }
}
