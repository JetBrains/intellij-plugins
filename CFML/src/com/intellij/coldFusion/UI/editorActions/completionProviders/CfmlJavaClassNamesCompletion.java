package com.intellij.coldFusion.UI.editorActions.completionProviders;

import com.intellij.codeInsight.completion.AllClassesGetter;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.coldFusion.model.psi.CfmlExpression;
import com.intellij.coldFusion.model.psi.CfmlFunctionCallExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 * @date 23.01.11
 */
public class CfmlJavaClassNamesCompletion extends CompletionProvider<CompletionParameters> {
  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                ProcessingContext context,
                                @NotNull final CompletionResultSet result) {
    PsiElement element = parameters.getPosition();
    CfmlFunctionCallExpression parentOfType = PsiTreeUtil.getParentOfType(element, CfmlFunctionCallExpression.class);
    if (parentOfType != null && parentOfType.isCreateObject()) {
      CfmlExpression[] arguments = parentOfType.getArguments();
      if (arguments.length == 2 && "\"java\"".equalsIgnoreCase(arguments[0].getText())) {
        AllClassesGetter
          .processJavaClasses(parameters, result.getPrefixMatcher(), parameters.getInvocationCount() <= 1, new Consumer<PsiClass>() {
            @Override
            public void consume(PsiClass psiClass) {
              result.addElement(AllClassesGetter.createLookupItem(psiClass, AllClassesGetter.TRY_SHORTENING));
            }
          });
      }
    }
  }
}
