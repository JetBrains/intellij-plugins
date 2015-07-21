package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

public class DartServerCompletionWeigher extends CompletionWeigher {
  @Override
  public Integer weigh(@NotNull final LookupElement element, @NotNull final CompletionLocation location) {
    final Object lookupObject = element.getObject();
    return lookupObject instanceof DartLookupObject ? ((DartLookupObject)lookupObject).getRelevance() : 0;
  }
}
