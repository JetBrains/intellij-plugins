// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinStepImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.Collection;
import java.util.List;

@NotNullByDefault
public interface CucumberJvmExtensionPoint {
  ExtensionPointName<CucumberJvmExtensionPoint> EP_NAME =
    ExtensionPointName.create("org.jetbrains.plugins.cucumber.steps.cucumberJvmExtensionPoint");

  // ToDo: remove parent

  /**
   * Checks if the child could be step definition file
   *
   * @param child  a PsiFile
   * @param parent container of the child
   * @return true if the child could be step definition file, else otherwise
   */
  boolean isStepLikeFile(PsiElement child, PsiElement parent);

  /**
   * Checks if the child could be a step definition container
   *
   * @param child  PsiElement to check
   * @param parent it's container
   * @return true if child could be step definition container and it's possible to write in it
   */
  boolean isWritableStepLikeFile(PsiElement child, PsiElement parent);

  /**
   * Returns type of the step definition file handled by this extension point.
   */
  BDDFrameworkType getStepFileType();

  StepDefinitionCreator getStepDefinitionCreator();

  /// Returns all step definitions available from `featureFile`.
  ///
  /// In large projects there can be a huge number of step definitions, so implementations are encouraged to
  /// [cache the results][com.intellij.psi.util.CachedValue], [use an index][com.intellij.util.indexing.FileBasedIndexExtension],
  /// or use other performance-enhancing techniques.
  List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, Module module);

  Collection<? extends PsiFile> getStepDefinitionContainers(GherkinFile file);

  default boolean isGherkin6Supported(Module module) {
    return false;
  }

  default @Nullable String getStepName(PsiElement step) {
    if (!(step instanceof GherkinStepImpl gherkinStep)) {
      return null;
    }
    return gherkinStep.getSubstitutedName();
  }
}
