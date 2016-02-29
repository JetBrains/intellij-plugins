package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.codeInsight.refs.AngularJSReferencesContributor;
import org.angularjs.index.AngularJSIndexingHandler;
import org.angularjs.index.AngularUiRouterViewsIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Irina.Chernushina on 2/22/2016.
 */
public class AngularJavaScriptCompletionContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    if (AngularJSCompletionContributor.getElementLanguage(parameters).isKindOf(JavascriptLanguage.INSTANCE)) {
      final PsiElement originalPosition = parameters.getOriginalPosition();
      if (originalPosition == null) return;
      if (AngularJSReferencesContributor.UI_VIEW_PATTERN.accepts(originalPosition)) {
        final FileBasedIndex instance = FileBasedIndex.getInstance();
        final Project project = originalPosition.getProject();

        final Collection<String> keys = instance.getAllKeys(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, project);
        for (String key : keys) {
          if (StringUtil.isEmptyOrSpaces(key)) {
            continue;
          }
          final LookupElementBuilder builder = LookupElementBuilder.create(key)
            .withTailText(" (angular-ui-router ui-view)", true);
          final LookupElement item = PrioritizedLookupElement.withPriority(builder, JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY);
          result.addElement(item);
        }
      }
    }
  }

  private static boolean isControllerPropertyValue(JSLiteralExpression literal) {
    return literal.isQuotedLiteral() && literal.getParent() instanceof JSProperty &&
           AngularJSIndexingHandler.CONTROLLER.equals(((JSProperty)literal.getParent()).getName());
  }
}
