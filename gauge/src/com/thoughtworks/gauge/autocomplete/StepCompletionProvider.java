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
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.thoughtworks.gauge.GaugeBootstrapService;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.connection.GaugeConnection;
import com.thoughtworks.gauge.core.GaugeCli;
import com.thoughtworks.gauge.language.psi.ConceptArg;
import com.thoughtworks.gauge.language.psi.SpecArg;
import com.thoughtworks.gauge.util.GaugeUtil;
import com.thoughtworks.gauge.util.StepUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.thoughtworks.gauge.autocomplete.StepCompletionContributor.getPrefix;
import static com.thoughtworks.gauge.language.psi.SpecPsiImplUtil.getStepValueFor;
import static com.thoughtworks.gauge.util.StepUtil.getGaugeStepAnnotationValues;

public final class StepCompletionProvider extends CompletionProvider<CompletionParameters> {
  public static final String STEP = "step";
  public static final String CONCEPT = "concept";
  private boolean isConcept = false;

  public void setConcept(boolean isConcept) {
    this.isConcept = isConcept;
  }

  @Override
  public void addCompletions(final @NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                             @NotNull CompletionResultSet resultSet) {
    resultSet.stopHere();
    final String prefix = getPrefix(parameters);

    resultSet = resultSet.withPrefixMatcher(new GaugePrefixMatcher(prefix));
    Module moduleForPsiElement = GaugeUtil.moduleForPsiElement(parameters.getPosition());
    if (moduleForPsiElement == null) {
      return;
    }
    for (Type item : getStepsInModule(moduleForPsiElement)) {
      LookupElementBuilder element = LookupElementBuilder.create(item.getText()).withTypeText(item.getType(), true);
      element = element.withInsertHandler((InsertionContext context1, LookupElement item1) -> {
        if (context1.getCompletionChar() == '\t') {
          context1.getDocument().insertString(context1.getEditor().getCaretModel().getOffset(), "\n");
          PsiDocumentManager.getInstance(context1.getProject()).commitDocument(context1.getDocument());
        }
        PsiElement stepElement = context1.getFile().findElementAt(context1.getStartOffset()).getParent();
        TemplateBuilder templateBuilder = TemplateBuilderFactory.getInstance().createTemplateBuilder(stepElement);
        replaceStepParamElements(prefix, context1, stepElement, templateBuilder);
        templateBuilder.run(context1.getEditor(), false);
      });
      resultSet.addElement(element);
    }
  }

  private void replaceStepParamElements(String prefix, InsertionContext context1, PsiElement stepElement, TemplateBuilder templateBuilder) {
    Class<? extends PsiElement> stepParamsClass = isConcept ? ConceptArg.class : SpecArg.class;
    List<? extends PsiElement> stepParams = PsiTreeUtil.getChildrenOfTypeAsList(stepElement, stepParamsClass);
    List<String> filledParams = getFilledParams(prefix);
    for (int i = 0; i < stepParams.size(); i++) {
      PsiElement stepParam = stepParams.get(i);
      String replacementText = i + 1 > filledParams.size() ? stepParam.getText() : filledParams.get(i);
      replaceStepParamWithStaticArg(context1, stepParam, replacementText);
      replaceElement(templateBuilder, stepParam, replacementText);
    }
  }

  private static void replaceStepParamWithStaticArg(InsertionContext context1, PsiElement stepParam, String replacementText) {
    String stepV = StringUtils.replace(StringUtils.replace(stepParam.getText(), "<", "\""), ">", "\"");
    context1.getDocument().replaceString(stepParam.getTextOffset(), stepParam.getTextOffset() + replacementText.length(), stepV);
  }

  private static void replaceElement(TemplateBuilder templateBuilder, PsiElement stepParam, String replacementText) {
    String substring = StringUtils.substring(replacementText, 1, replacementText.length() - 1);
    templateBuilder.replaceElement(stepParam, new TextRange(1, replacementText.length() - 1), substring);
  }

  private static List<String> getFilledParams(String prefix) {
    Pattern filledParamPattern = Pattern.compile("\"[\\w ]+\"");
    Matcher matcher = filledParamPattern.matcher(prefix);
    List<String> filledParams = new ArrayList<>();
    while (matcher.find()) {
      filledParams.add(matcher.group());
    }
    return filledParams;
  }

  private static class Type {
    private final String text;
    private final String type;

    Type(String text, String type) {
      this.text = text;
      this.type = type;
    }

    public String getText() {
      return text;
    }

    public String getType() {
      return type;
    }
  }

  private static Collection<Type> getStepsInModule(Module module) {
    Map<String, Type> steps = getImplementedSteps(module);
    try {
      GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(module.getProject());
      GaugeCli gaugeCli = bootstrapService.getGaugeCli(module, true);
      if (gaugeCli == null) return steps.values();

      GaugeConnection gaugeConnection = gaugeCli.getGaugeConnection();
      if (gaugeConnection != null) {
        gaugeConnection.fetchAllSteps().forEach(s -> addStep(steps, s, STEP));
        gaugeConnection.fetchAllConcepts().forEach(concept -> addStep(steps, concept.getStepValue(), CONCEPT));
      }
    }
    catch (IOException ex) {
      Logger.getInstance(StepCompletionProvider.class).debug(ex);
    }
    return steps.values();
  }

  private static @NotNull Map<String, Type> getImplementedSteps(Module module) {
    Map<String, Type> steps = new HashMap<>();
    Collection<PsiMethod> methods = StepUtil.getStepMethods(module);
    for (PsiMethod m : methods) {
      for (String s : getGaugeStepAnnotationValues(m)) {
        steps.put(getStepValueFor(module, m, s, false).getStepText(), new Type(s, STEP));
      }
    }
    return steps;
  }

  private static void addStep(Map<String, Type> steps, StepValue stepValue, String entity) {
    if (stepValue.getStepAnnotationText().trim().isEmpty() || steps.containsKey(stepValue.getStepText())) return;
    steps.put(stepValue.getStepText(), new Type(stepValue.getStepAnnotationText(), entity));
  }
}
