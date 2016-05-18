package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Vladislav.Soroka
 */
class SimpleProvider extends CompletionProvider<CompletionParameters> {
  private final Collection<LookupElement> myLookupElements;

  public SimpleProvider(String... items) {
    myLookupElements = ContainerUtil.map2List(items, item -> {
      return LookupElementBuilder.create(item).withCaseSensitivity(false);
    });
  }

  @Override
  public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
    result.addAllElements(myLookupElements);
  }
}
