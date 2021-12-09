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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.thoughtworks.gauge.language.psi.SpecDetail;
import com.thoughtworks.gauge.language.psi.SpecScenario;
import com.thoughtworks.gauge.language.psi.SpecStep;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.thoughtworks.gauge.autocomplete.StepCompletionContributor.getPrefix;

final class StaticArgCompletionProvider extends CompletionProvider<CompletionParameters> {
  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                @NotNull CompletionResultSet resultSet) {
    String prefix = getPrefix(parameters);
    CompletionResultSet resultSetWithMatcher = resultSet.withPrefixMatcher(new PlainPrefixMatcher(prefix));
    PsiFile specFile = parameters.getOriginalFile();
    SpecDetail specDetail = PsiTreeUtil.getChildOfType(specFile, SpecDetail.class);
    List<SpecStep> stepsInFile = new ArrayList<>();
    addContextSteps(specDetail, stepsInFile);
    addStepsInScenarios(specFile, stepsInFile);

    Set<String> staticArgs = getArgsFromSteps(stepsInFile);
    for (String arg : staticArgs) {
      if (arg != null) {
        LookupElementBuilder item = LookupElementBuilder.create(arg);
        resultSetWithMatcher.addElement(item);
      }
    }
  }

  private static void addStepsInScenarios(PsiFile specFile, List<SpecStep> stepsInFile) {
    List<SpecScenario> scenarios = PsiTreeUtil.getChildrenOfTypeAsList(specFile, SpecScenario.class);
    for (SpecScenario scenario : scenarios) {
      stepsInFile.addAll(scenario.getStepList());
    }
  }

  private static void addContextSteps(SpecDetail specDetail, List<SpecStep> stepsInFile) {
    if (specDetail != null) {
      stepsInFile.addAll(specDetail.getContextSteps());
    }
  }

  private static Set<String> getArgsFromSteps(List<SpecStep> steps) {
    return steps.stream()
      .map(SpecStep::getStaticArgList)
      .flatMap(Collection::stream)
      .map(PsiElement::getText)
      .filter(arg -> arg != null)
      .collect(Collectors.toSet());
  }
}
