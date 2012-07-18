package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.PsiElement;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 7/16/12
 */
public class JavaStepDefinition extends AbstractStepDefinition {
  public JavaStepDefinition(PsiElement element) {
    super(element);
  }

  @Override
  public List<String> getVariableNames() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getElementText() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
