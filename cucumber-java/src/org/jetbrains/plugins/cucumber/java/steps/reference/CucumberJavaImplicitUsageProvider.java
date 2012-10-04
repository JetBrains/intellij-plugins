package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

/**
 * User: Andrey.Vokin
 * Date: 10/4/12
 */
public class CucumberJavaImplicitUsageProvider implements ImplicitUsageProvider {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    return CucumberJavaUtil.isStepDefinition(element);
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
