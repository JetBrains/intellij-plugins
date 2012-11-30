package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.PsiElement;
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
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, Consumer<PomTarget> consumer) {
    if (element.getParent() instanceof GrLiteral) {
      final PsiElement literal = element.getParent();
      final PsiElement parent = literal.getParent();    //~literal
      if (parent != null) {
        final PsiElement pparent = parent.getParent();  //(~literal)
        if (pparent != null) {
          final PsiElement ppparent = pparent.getParent(); //When(~literal)
          if (ppparent instanceof GrMethodCall && GrCucumberUtil.isStepDefinition(ppparent)) {
            final GrMethodCall methodCall = (GrMethodCall)ppparent;
            consumer.consume(GrStepDefinition.getStepDefinition(methodCall));
          }
        }
      }
    }
  }
}
