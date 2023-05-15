package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlToken;
import org.angularjs.codeInsight.refs.AngularJSXmlReferencesContributor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularModuleIndex;
import org.angularjs.index.AngularUiRouterStatesIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static org.angularjs.codeInsight.AngularJavaScriptCompletionContributor.addCompletionVariants;

public class AngularJSXmlCompletionContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    final PsiElement originalPosition = parameters.getOriginalPosition();
    if (originalPosition != null) {
      final PsiElement position = originalPosition instanceof XmlToken ? originalPosition.getParent() : originalPosition;
      if (AngularJSXmlReferencesContributor.NG_APP_REF.accepts(position)) {
        if (!AngularIndexUtil.hasAngularJS(originalPosition.getProject())) return;
        final Collection<String> keys = AngularIndexUtil.getAllKeys(AngularModuleIndex.KEY, originalPosition.getProject());
        addCompletionVariants(result, keys, " (AngularJS module)");
        result.stopHere();
      }
      else if (AngularJSXmlReferencesContributor.UI_VIEW_REF.accepts(position)) {
        if (!AngularIndexUtil.hasAngularJS(originalPosition.getProject())) return;
        final Collection<String> keys = AngularIndexUtil.getAllKeys(AngularUiRouterStatesIndex.KEY, originalPosition.getProject());
        addCompletionVariants(result, keys, " (angular-ui-router state)");
        result.stopHere();
      }
    }
  }
}
