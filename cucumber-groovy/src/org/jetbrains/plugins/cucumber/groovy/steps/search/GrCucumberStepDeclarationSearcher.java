package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;

/**
 * @author Max Medvedev
 */
public class GrCucumberStepDeclarationSearcher extends PomDeclarationSearcher {

  @Override
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, @NotNull Consumer<? super PomTarget> consumer) {
    PsiLanguageInjectionHost host = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
    if (host != null) {
      element = host;
    }
    if (element.getParent() instanceof GrLiteral) {
      element = element.getParent();
    }
    if (element instanceof GrLiteral) {
      final PsiElement parent = element.getParent();    //~literal
      if (parent != null) {
        final PsiElement pparent = parent.getParent();  //(~literal)
        if (pparent != null) {
          final PsiElement ppparent = pparent.getParent(); //When(~literal)
          if (ppparent instanceof GrMethodCall methodCall && GrCucumberUtil.isStepDefinition(ppparent)) {
            consumer.consume(GrStepDefinition.getStepDefinition(methodCall));
          }
        }
      }
    }
  }
}
