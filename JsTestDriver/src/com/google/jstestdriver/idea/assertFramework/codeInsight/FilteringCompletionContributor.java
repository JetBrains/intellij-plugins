package com.google.jstestdriver.idea.assertFramework.codeInsight;

import com.google.jstestdriver.idea.assertFramework.codeInsight.JsCallTemplateContextProvider;
import com.google.jstestdriver.idea.assertFramework.codeInsight.MethodTemplateLookupElement;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.template.Template;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FilteringCompletionContributor extends CompletionContributor {
  public FilteringCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull final CompletionResultSet result) {
        final List<JsCallTemplateContextProvider> providers = filterContextProviders(
          result.getPrefixMatcher().getPrefix(),
          parameters
        );
        final Ref<CompletionSorter> firstSorterRef = Ref.create(null);
        result.runRemainingContributors(parameters, new Consumer<CompletionResult>() {
          @Override
          public void consume(CompletionResult completionResult) {
            LookupElement element = completionResult.getLookupElement();
            if (!canWeDoBetter(element, providers)) {
              result.passResult(completionResult);
              if (firstSorterRef.isNull()) {
                firstSorterRef.set(completionResult.getSorter());
              }
            }
          }
        }, true);
        CompletionSorter firstSorter = firstSorterRef.get();
        CompletionResultSet newResult = firstSorter != null ? result.withRelevanceSorter(firstSorter) : result;
        fillCallTemplateVariants(newResult, providers);
      }
    });
  }

  @NotNull
  private static List<JsCallTemplateContextProvider> filterContextProviders(@NotNull final String prefix,
                                                                                    @NotNull final CompletionParameters parameters) {
    return ContainerUtil.filter(JsCallTemplateContextProvider.EP_NAME.getExtensions(), new Condition<JsCallTemplateContextProvider>() {
      @Override
      public boolean value(JsCallTemplateContextProvider contextProvider) {
        if (!contextProvider.getCalledFunctionName().startsWith(prefix)) {
          return false;
        }
        return contextProvider.isInContext(parameters);
      }
    });
  }

  private static boolean canWeDoBetter(@NotNull LookupElement lookupElement, List<JsCallTemplateContextProvider> providers) {
    final String lookupString = lookupElement.getLookupString();
    return ContainerUtil.or(providers, new Condition<JsCallTemplateContextProvider>() {
      @Override
      public boolean value(JsCallTemplateContextProvider contextProvider) {
        return contextProvider.getCalledFunctionName().equals(lookupString);
      }
    });
  }

  private static void fillCallTemplateVariants(@NotNull CompletionResultSet result,
                                               @NotNull List<JsCallTemplateContextProvider> providers) {
    for (final JsCallTemplateContextProvider provider : providers) {
      final String expectedCallFunctionName = provider.getCalledFunctionName();
      Template template = provider.getTemplate();
      MethodTemplateLookupElement item = new MethodTemplateLookupElement(expectedCallFunctionName, template) {
        @Override
        public void renderElement(LookupElementPresentation presentation) {
          presentation.setItemText(expectedCallFunctionName);
          presentation.setTailText(provider.getTailText(), true);
          presentation.setTypeText(provider.getTypeText());
        }
      };
      result.addElement(item);
    }
  }

}
