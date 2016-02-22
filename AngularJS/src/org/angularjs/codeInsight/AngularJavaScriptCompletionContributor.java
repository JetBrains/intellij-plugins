package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.text.StringUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 2/22/2016.
 */
public class AngularJavaScriptCompletionContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    if (AngularJSCompletionContributor.getElementLanguage(parameters).isKindOf(JavascriptLanguage.INSTANCE)) {
      final String prefix = result.getPrefixMatcher().getPrefix();
      if (StringUtil.isEmptyOrSpaces(prefix)) return;
      final String[] parts = prefix.split(" ");
      if (parameters.getOriginalPosition() != null && parameters.getOriginalPosition().getParent() instanceof JSLiteralExpression) {
        final JSLiteralExpression literal = (JSLiteralExpression)parameters.getOriginalPosition().getParent();
        if (literal.isQuotedLiteral() && literal.getParent() instanceof JSProperty &&
            AngularJSIndexingHandler.CONTROLLER.equals(((JSProperty)literal.getParent()).getName())) {
          result.addElement(LookupElementBuilder.create(parts[0] + AngularJSIndexingHandler.AS_CONNECTOR_WITH_SPACES));
        }
      }
    }
  }
}
