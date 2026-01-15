// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.OptionalStepDefinitionExtensionPoint;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepDefinitionCreationContext;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.*;
import java.util.regex.Pattern;

@NotNullByDefault
public final class CucumberStepHelper {
  private static final Logger LOG = Logger.getInstance(CucumberStepHelper.class);

  /// Creates a file that will contain step definitions.
  ///
  /// @param dir                      container for a created file
  /// @param fileNameWithoutExtension name of the file without "." and extension
  /// @param frameworkType            type of file to create
  public static @Nullable PsiFile createStepDefinitionFile(PsiDirectory dir,
                                                           String fileNameWithoutExtension,
                                                           BDDFrameworkType frameworkType) {
    final CucumberJvmExtensionPoint ep = getExtensionMap().get(frameworkType);
    if (ep == null) {
      LOG.error(String.format("Unsupported step definition file type %s", frameworkType));
      return null;
    }

    return ep.getStepDefinitionCreator().createStepDefinitionContainer(dir, fileNameWithoutExtension);
  }

  public static boolean validateNewStepDefinitionFileName(Project project, String fileName, BDDFrameworkType frameworkType) {
    final CucumberJvmExtensionPoint ep = getExtensionMap().get(frameworkType);
    if (ep == null) {
      LOG.error(String.format("No extension point registered for framework type %s", frameworkType));
      return false;
    }
    return ep.getStepDefinitionCreator().validateNewStepDefinitionFileName(project, fileName);
  }

  /// Searches for all step definitions that are available from `featureFile` and returns them.
  ///
  /// @see CucumberJvmExtensionPoint#loadStepsFor(PsiFile, Module)
  /// @deprecated Use [#loadStepsFor] instead.
  @Deprecated(forRemoval = true)
  public static Collection<AbstractStepDefinition> findAllStepDefinitions(PsiFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptyList();
    }
    return loadStepsFor(featureFile, module);
  }

  /// Searches for all step definitions that [match][AbstractStepDefinition#matches] `step`,
  /// groups them by step definition class, and sorts them by pattern size.
  /// For each step definition class it finds the largest pattern.
  ///
  /// @param featureFile file with steps
  /// @param step        step itself
  public static Collection<AbstractStepDefinition> findStepDefinitions(PsiFile featureFile, GherkinStep step) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptyList();
    }

    final String substitutedName = step.getSubstitutedName();
    if (substitutedName == null) {
      return Collections.emptyList();
    }

    final Map<Class<? extends AbstractStepDefinition>, AbstractStepDefinition> definitionsByClass = new HashMap<>();
    final List<AbstractStepDefinition> allSteps = loadStepsFor(featureFile, module);

    for (final AbstractStepDefinition stepDefinition : allSteps) {
      final boolean matches = stepDefinition.matches(substitutedName);
      if (matches && stepDefinition.supportsStep(step)) {
        final Pattern currentLongestPattern = getPatternByDefinition(definitionsByClass.get(stepDefinition.getClass()));
        final Pattern newPattern = getPatternByDefinition(stepDefinition);
        final int newPatternLength = ((newPattern != null) ? newPattern.pattern().length() : -1);
        if ((currentLongestPattern == null) || (currentLongestPattern.pattern().length() < newPatternLength)) {
          definitionsByClass.put(stepDefinition.getClass(), stepDefinition);
        }
      }
    }

    return definitionsByClass.values();
  }

  /// Returns all step definitions available from `featureFile`.
  ///
  /// This is a helper method that calls [AbstractCucumberExtension#loadStepsFor] of all installed language-specific Cucumber plugins.
  public static List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, Module module) {
    final ArrayList<AbstractStepDefinition> result = new ArrayList<>();
    for (final CucumberJvmExtensionPoint ep : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      result.addAll(ep.loadStepsFor(featureFile, module));
    }
    return result;
  }

  public static Set<CucumberStepDefinitionCreationContext> getStepDefinitionContainers(GherkinFile featureFile) {
    final Set<CucumberStepDefinitionCreationContext> result = new HashSet<>();
    for (final CucumberJvmExtensionPoint ep : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      // Skip if framework file creation support is optional
      if ((ep instanceof OptionalStepDefinitionExtensionPoint point) && !point.participateInStepDefinitionCreation(featureFile)) {
        continue;
      }
      final Collection<? extends PsiFile> psiFiles = ep.getStepDefinitionContainers(featureFile);
      final BDDFrameworkType frameworkType = ep.getStepFileType();
      for (final PsiFile psiFile : psiFiles) {
        result.add(new CucumberStepDefinitionCreationContext(psiFile, frameworkType));
      }
    }
    return result;
  }

  public static Map<BDDFrameworkType, CucumberJvmExtensionPoint> getExtensionMap() {
    final HashMap<BDDFrameworkType, CucumberJvmExtensionPoint> result = new HashMap<>();
    for (final CucumberJvmExtensionPoint ep : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      result.put(ep.getStepFileType(), ep);
    }
    return result;
  }

  public static boolean isGherkin6Supported(Module module) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return true;
    }
    for (final CucumberJvmExtensionPoint ep : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      if (ep.isGherkin6Supported(module)) {
        return true;
      }
    }
    return false;
  }

  @Contract("null -> null")
  private static @Nullable Pattern getPatternByDefinition(@Nullable AbstractStepDefinition definition) {
    if (definition == null) {
      return null;
    }
    return definition.getPattern();
  }

  //region Deprecated and to be removed

  /// @deprecated Use [#loadStepsFor(PsiFile, Module)] instead.
  @Deprecated(forRemoval = true)
  public static List<AbstractStepDefinition> getAllStepDefinitions(PsiFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptyList();
    }
    return loadStepsFor(featureFile, module);
  }

  /// @deprecated Use `CucumberJvmExtensionPoint.EP_NAME.getExtensionList()` directly.
  @Deprecated(forRemoval = true)
  public static List<CucumberJvmExtensionPoint> getCucumberExtensions() {
    return CucumberJvmExtensionPoint.EP_NAME.getExtensionList();
  }

  /// @deprecated Use `CucumberJvmExtensionPoint.EP_NAME.getExtensionList().size()` directly.
  @Deprecated(forRemoval = true)
  public static int getExtensionCount() {
    return getCucumberExtensions().size();
  }

  //endregion
}
