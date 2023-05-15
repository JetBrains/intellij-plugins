package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.completion.JSCompletionUtil;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.indexing.FileBasedIndex;
import org.angularjs.codeInsight.refs.AngularJSReferencesContributor;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.angularjs.index.AngularModuleIndex;
import org.angularjs.index.AngularUiRouterViewsIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class AngularJavaScriptCompletionContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    if (AngularJSCompletionContributor.getElementLanguage(parameters).isKindOf(JavascriptLanguage.INSTANCE)) {
      PsiElement originalPosition = parameters.getOriginalPosition();
      if (originalPosition == null) return;
      final Project project = originalPosition.getProject();
      if (AngularJSReferencesContributor.UI_VIEW_PATTERN.accepts(originalPosition)) {
        final FileBasedIndex instance = FileBasedIndex.getInstance();

        final Collection<String> keys = instance.getAllKeys(AngularUiRouterViewsIndex.UI_ROUTER_VIEWS_CACHE_INDEX, project);
        addCompletionVariants(result, keys, " (angular-ui-router ui-view)");
      }
      else {
        originalPosition = originalPosition instanceof LeafPsiElement &&
                           ((LeafPsiElement)originalPosition).getElementType() == JSTokenTypes.STRING_LITERAL ?
                           originalPosition.getParent() : originalPosition;
        if (AngularJSReferencesContributor.MODULE_PATTERN.accepts(originalPosition) ||
            AngularJSReferencesContributor.MODULE_DEPENDENCY_PATTERN.accepts(originalPosition)) {
          final Collection<String> keys = AngularIndexUtil.getAllKeys(AngularModuleIndex.KEY, project);
          addCompletionVariants(result, keys, " (AngularJS module)");
        }
      }
    }
  }

  static void addCompletionVariants(@NotNull CompletionResultSet result, Collection<String> keys, final @Nullable String comment) {
    for (String key : keys) {
      if (StringUtil.isEmptyOrSpaces(key)) {
        continue;
      }
      LookupElementBuilder builder = LookupElementBuilder.create(key);
      if (comment != null) {
        builder = builder.withTailText(comment, true);
      }
      final LookupElement item = JSCompletionUtil.withJSLookupPriority(builder, JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY);
      result.addElement(item);
    }
  }

  private static boolean isControllerPropertyValue(JSLiteralExpression literal) {
    return literal.isQuotedLiteral() && literal.getParent() instanceof JSProperty &&
           AngularJSIndexingHandler.CONTROLLER.equals(((JSProperty)literal.getParent()).getName());
  }
}
