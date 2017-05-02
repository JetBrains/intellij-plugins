package com.intellij.coldFusion.injection;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.PsiManagerEx;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Karashevich
 */
public class CfmlSplittedInjectionManager extends CfmlSplittedInjectionManagerKt {

  public CfmlSplittedInjectionManager(@NotNull Project project,
                                      @NotNull PsiManagerEx psiManagerEx) {
    super(project, psiManagerEx);
  }

  public static CfmlSplittedInjectionManager getInstance(Project project) {
    return ServiceManager.getService(project, CfmlSplittedInjectionManager.class);
  }
}
