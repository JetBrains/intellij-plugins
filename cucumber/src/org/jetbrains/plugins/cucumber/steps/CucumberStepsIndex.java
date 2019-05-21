// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.OptionalStepDefinitionExtensionPoint;
import org.jetbrains.plugins.cucumber.inspections.CucumberStepDefinitionCreationContext;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class CucumberStepsIndex {
  private static final Logger LOG = Logger.getInstance(CucumberStepsIndex.class.getName());

  private final Map<BDDFrameworkType, CucumberJvmExtensionPoint> myExtensionMap;
  private final Map<CucumberJvmExtensionPoint, Object> myExtensionData;
  private Project myProject;

  public static CucumberStepsIndex getInstance(Project project) {
    CucumberStepsIndex result = ServiceManager.getService(project, CucumberStepsIndex.class);
    result.myProject = project;

    return result;
  }

  public CucumberStepsIndex(final Project project) {
    myExtensionMap = new HashMap<>();
    myExtensionData = new HashMap<>();

    for (CucumberJvmExtensionPoint e : CucumberJvmExtensionPoint.EP_NAME.getExtensionList()) {
      myExtensionMap.put(e.getStepFileType(), e);
      myExtensionData.put(e, e.getDataObject(project));
    }
  }

  public Object getExtensionDataObject(CucumberJvmExtensionPoint e) {
    return myExtensionData.get(e);
  }

  /**
   * Creates a file that will contain step definitions
   *
   * @param dir                      container for created file
   * @param fileNameWithoutExtension name of the file with out "." and extension
   * @param frameworkType            type of file to create
   */
  public PsiFile createStepDefinitionFile(@NotNull final PsiDirectory dir,
                                          @NotNull final String fileNameWithoutExtension,
                                          @NotNull final BDDFrameworkType frameworkType) {
    final CucumberJvmExtensionPoint ep = myExtensionMap.get(frameworkType);
    if (ep == null) {
      LOG.error(String.format("Unsupported step definition file type %s", frameworkType.toString()));
      return null;
    }

    return ep.getStepDefinitionCreator().createStepDefinitionContainer(dir, fileNameWithoutExtension);
  }

  public boolean validateNewStepDefinitionFileName(@NotNull final PsiDirectory directory,
                                                   @NotNull final String fileName,
                                                   @NotNull final BDDFrameworkType frameworkType) {
    final CucumberJvmExtensionPoint ep = myExtensionMap.get(frameworkType);
    assert ep != null;
    return ep.getStepDefinitionCreator().validateNewStepDefinitionFileName(directory.getProject(), fileName);
  }


  /**
   * Searches for step definition.
   * More info is available in {@link #findStepDefinitions(PsiFile, GherkinStep)} doc
   *
   * @param featureFile file with steps
   * @param step        step itself
   * @return definition or null if not found
   * @see #findStepDefinitions(PsiFile, GherkinStep)
   */
  @Nullable
  public AbstractStepDefinition findStepDefinition(@NotNull final PsiFile featureFile, @NotNull final GherkinStep step) {
    final Collection<AbstractStepDefinition> definitions = findStepDefinitions(featureFile, step);
    return (definitions.isEmpty() ? null : definitions.iterator().next());
  }

  /**
   * Searches for ALL step definitions, groups it by step definition class and sorts by pattern size.
   * For each step definition class it finds the largest pattern.
   *
   * @param featureFile file with steps
   * @param step        step itself
   * @return definitions
   */
  @NotNull
  public Collection<AbstractStepDefinition> findStepDefinitions(@NotNull final PsiFile featureFile, @NotNull final GherkinStep step) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) {
      return Collections.emptyList();
    }
    String substitutedName = step.getSubstitutedName();
    if (substitutedName == null) {
      return Collections.emptyList();
    }

    Map<Class<? extends AbstractStepDefinition>, AbstractStepDefinition> definitionsByClass =
      new HashMap<>();
    List<AbstractStepDefinition> allSteps = loadStepsFor(featureFile, module);

    for (AbstractStepDefinition stepDefinition : allSteps) {
      if (stepDefinition.matches(substitutedName) && stepDefinition.supportsStep(step)) {
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

  /**
   * Returns pattern from step definition (if exists)
   *
   * @param definition step definition
   * @return pattern or null if does not exist
   */
  @Nullable
  private static Pattern getPatternByDefinition(@Nullable final AbstractStepDefinition definition) {
    if (definition == null) {
      return null;
    }
    return definition.getPattern();
  }

  // ToDo: use binary search here
  public List<AbstractStepDefinition> findStepDefinitionsByPattern(@NotNull final String pattern, @NotNull final Module module) {
    final List<AbstractStepDefinition> allSteps = loadStepsFor(null, module);
    final List<AbstractStepDefinition> result = new ArrayList<>();
    for (AbstractStepDefinition stepDefinition : allSteps) {
      final String elementText = stepDefinition.getCucumberRegex();
      if (elementText != null && elementText.equals(pattern)) {
        result.add(stepDefinition);
      }
    }
    return result;
  }

  public List<AbstractStepDefinition> getAllStepDefinitions(@NotNull final PsiFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) return Collections.emptyList();
    return loadStepsFor(featureFile, module);
  }


  private List<AbstractStepDefinition> loadStepsFor(@Nullable final PsiFile featureFile, @NotNull final Module module) {
    ArrayList<AbstractStepDefinition> result = new ArrayList<>();

    for (CucumberJvmExtensionPoint extension : myExtensionMap.values()) {
      result.addAll(extension.loadStepsFor(featureFile, module));
    }
    return result;
  }

  public Set<CucumberStepDefinitionCreationContext> getStepDefinitionContainers(@NotNull final GherkinFile featureFile) {
    Set<CucumberStepDefinitionCreationContext> result = new HashSet<>();
    for (CucumberJvmExtensionPoint ep : myExtensionMap.values()) {
      // Skip if framework file creation support is optional
      if ((ep instanceof OptionalStepDefinitionExtensionPoint) &&
          !((OptionalStepDefinitionExtensionPoint)ep).participateInStepDefinitionCreation(featureFile)) {
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

  public void reset() {
    for (CucumberJvmExtensionPoint e : myExtensionMap.values()) {
      e.reset(myProject);
    }
  }

  public void flush() {
    for (CucumberJvmExtensionPoint e : myExtensionMap.values()) {
      e.flush(myProject);
    }
  }

  public Map<BDDFrameworkType, CucumberJvmExtensionPoint> getExtensionMap() {
    return myExtensionMap;
  }

  public int getExtensionCount() {
    return myExtensionMap.size();
  }
}
