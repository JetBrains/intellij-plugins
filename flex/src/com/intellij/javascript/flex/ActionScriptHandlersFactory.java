package com.intellij.javascript.flex;

import com.intellij.javascript.flex.completion.ActionScriptCompletionKeywordsContributor;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.dialects.JSHandlersFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptHandlersFactory extends JSHandlersFactory {

  @NotNull
  @Override
  public JSCompletionKeywordsContributor newCompletionKeywordsContributor() {
    return new ActionScriptCompletionKeywordsContributor();
  }
}
