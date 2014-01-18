package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JavaScriptCompletionData;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import org.angularjs.lang.AngularJSLanguage;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSCompletionContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(final CompletionParameters parameters, final CompletionResultSet result) {
    if (!getElementLanguage(parameters).is(AngularJSLanguage.INSTANCE)) return;
    PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());

    if (ref instanceof JSReferenceExpressionImpl) {
      final JavaScriptCompletionData.JSInsertHandler insertHandler = new JavaScriptCompletionData.JSInsertHandler();
      AngularJSProcessor.process(parameters.getPosition(), new Consumer<JSNamedElement>() {
        @Override
        public void consume(JSNamedElement element) {
          result.consume(LookupElementBuilder.createWithIcon(element).withInsertHandler(insertHandler));
        }
      });
    }
  }

  private static Language getElementLanguage(final CompletionParameters parameters) {
    final AccessToken l = ReadAction.start();
    try {
      return PsiUtilCore.getLanguageAtOffset(parameters.getPosition().getContainingFile(), parameters.getOffset());
    } finally {
      l.finish();
    }
  }
}
