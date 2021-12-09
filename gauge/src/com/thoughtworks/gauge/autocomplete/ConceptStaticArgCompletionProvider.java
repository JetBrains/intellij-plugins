/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.autocomplete;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.thoughtworks.gauge.language.psi.ConceptStaticArg;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.thoughtworks.gauge.autocomplete.StepCompletionContributor.getPrefix;

public class ConceptStaticArgCompletionProvider extends CompletionProvider<CompletionParameters> {
  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext processingContext,
                                @NotNull CompletionResultSet resultSet) {
    String prefix = getPrefix(parameters);
    resultSet = resultSet.withPrefixMatcher(new PlainPrefixMatcher(prefix));
    Collection<ConceptStaticArg> staticArgs = PsiTreeUtil.collectElementsOfType(parameters.getOriginalFile(), ConceptStaticArg.class);
    for (ConceptStaticArg arg : staticArgs) {
      if (arg != null) {
        String text = arg.getText().replaceFirst("\"", "");
        String textWithoutQuotes = text.substring(0, text.length() - 1);
        if (!textWithoutQuotes.isEmpty()) {
          resultSet.addElement(LookupElementBuilder.create(textWithoutQuotes));
        }
      }
    }
  }
}
