package org.jetbrains.plugins.cucumber.java;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CucumberJavaInjector implements MultiHostInjector {
  public static final Language regexpLanguage = Language.findLanguageByID("RegExp");

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement element) {
    if (element instanceof PsiLiteralExpression && element instanceof PsiLanguageInjectionHost) {
      final PsiElement firstChild = element.getFirstChild();
      if (firstChild != null && firstChild.getNode().getElementType() == JavaTokenType.STRING_LITERAL) {
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
        if (annotation != null &&
            (CucumberJavaUtil.isCucumberStepAnnotation(annotation) || CucumberJavaUtil.isCucumberHookAnnotation(annotation))) {
          final TextRange range = new TextRange(1, element.getTextLength() - 1);
          registrar.startInjecting(regexpLanguage).addPlace(null, null, (PsiLanguageInjectionHost)element, range).doneInjecting();
        }
      }
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(PsiLiteralExpression.class);
  }
}
