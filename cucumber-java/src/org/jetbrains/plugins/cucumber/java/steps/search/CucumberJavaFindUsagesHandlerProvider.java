package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.find.findUsages.CustomJavaFindUsagesHandler;
import com.intellij.find.findUsages.CustomJavaFindUsagesHandlerProvider;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.findUsages.JavaMethodFindUsagesOptions;
import com.intellij.openapi.application.ReadActionProcessor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

/**
 * User: Andrey.Vokin
 * Date: 7/26/12
 */
public class CucumberJavaFindUsagesHandlerProvider extends CustomJavaFindUsagesHandlerProvider {
  @Override
  public CustomJavaFindUsagesHandler getHandler() {
    return new CustomJavaFindUsagesHandler() {
      @Override
      public void processElementUsages(@NotNull final PsiElement element,
                                       @NotNull final Processor<UsageInfo> processor,
                                       @NotNull final FindUsagesOptions options) {
        final ReadActionProcessor<PsiReference> refProcessor = new ReadActionProcessor<PsiReference>() {
          @Override
          public boolean processInReadAction(final PsiReference ref) {
            TextRange rangeInElement = ref.getRangeInElement();
            return processor.process(new UsageInfo(ref.getElement(), rangeInElement.getStartOffset(), rangeInElement.getEndOffset(), false));
          }
        };

        final SearchScope scope = options.searchScope;

        if (options instanceof JavaMethodFindUsagesOptions && options.isUsages) {
          ReferencesSearch.search(new ReferencesSearch.SearchParameters(element, scope, false, options.fastTrack)).forEach(refProcessor);
        }

      }
    };
  }
}
