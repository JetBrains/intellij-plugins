package org.jetbrains.plugins.cucumber;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * {@link CucumberJvmExtensionPoint} that may not want to participate in step definition creation process.
 * Some frameworks support step definition resolving, but not step definition creation.
 * <p/>
 * Implement this interface only if your EP is kind of described above.
 *
 * @author Ilya.Kazakevich
 */
public interface OptionalStepDefinitionExtensionPoint extends CucumberJvmExtensionPoint {
  /**
   * Participate in steps definition creation or not
   *
   * @param anchor gherkin step or gherkin file, or some other element pointing to context when this method is called
   * @return participate or not
   */
  boolean participateInStepDefinitionCreation(@NotNull PsiElement anchor);
}
