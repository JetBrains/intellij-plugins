package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.HashMap;
import org.apache.oro.text.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.OptionalStepDefinitionExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.*;

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

    for (CucumberJvmExtensionPoint e : Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME)) {
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
   * More info is available in {@link #findStepDefinitions(com.intellij.psi.PsiFile, org.jetbrains.plugins.cucumber.psi.GherkinStep)} doc
   *
   * @param featureFile file with steps
   * @param step        step itself
   * @return definition or null if not found
   * @see #findStepDefinitions(com.intellij.psi.PsiFile, org.jetbrains.plugins.cucumber.psi.GherkinStep)
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

    Map<Class<? extends AbstractStepDefinition>, AbstractStepDefinition> definitionsByClass =
      new java.util.HashMap<>();
    List<AbstractStepDefinition> allSteps = loadStepsFor(featureFile, module);
    for (AbstractStepDefinition stepDefinition : allSteps) {
      if (stepDefinition.matches(step.getSubstitutedName()) && stepDefinition.supportsStep(step)) {
        final Pattern currentLongestPattern = getPatternByDefinition(definitionsByClass.get(stepDefinition.getClass()));
        final Pattern newPattern = getPatternByDefinition(stepDefinition);
        final int newPatternLength = ((newPattern != null) ? newPattern.getPattern().length() : -1);
        if ((currentLongestPattern == null) || (currentLongestPattern.getPattern().length() < newPatternLength)) {
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

  @NotNull
  public List<PsiFile> gatherStepDefinitionsFilesFromDirectory(@NotNull final PsiDirectory dir, final boolean writableOnly) {
    final List<PsiFile> result = new ArrayList<>();

    // find step definitions in current folder
    for (PsiFile file : dir.getFiles()) {
      final VirtualFile virtualFile = file.getVirtualFile();
      boolean isStepFile = writableOnly ? isWritableStepLikeFile(file, file.getParent()) : isStepLikeFile(file, file.getParent());
      if (isStepFile && virtualFile != null) {
        result.add(file);
      }
    }
    // process subfolders
    for (PsiDirectory subDir : dir.getSubdirectories()) {
      result.addAll(gatherStepDefinitionsFilesFromDirectory(subDir, writableOnly));
    }

    return result;
  }

  private List<AbstractStepDefinition> loadStepsFor(@Nullable final PsiFile featureFile, @NotNull final Module module) {
    ArrayList<AbstractStepDefinition> result = new ArrayList<>();

    for (CucumberJvmExtensionPoint extension : myExtensionMap.values()) {
      result.addAll(extension.loadStepsFor(featureFile, module));
    }
    return result;
  }

  public Set<Pair<PsiFile, BDDFrameworkType>> getStepDefinitionContainers(@NotNull final GherkinFile featureFile) {
    Set<Pair<PsiFile, BDDFrameworkType>> result = new HashSet<>();
    for (CucumberJvmExtensionPoint ep : myExtensionMap.values()) {
      // Skip if framework file creation support is optional
      if ((ep instanceof OptionalStepDefinitionExtensionPoint) &&
          !((OptionalStepDefinitionExtensionPoint)ep).participateInStepDefinitionCreation(featureFile)) {
        continue;
      }
      final Collection<? extends PsiFile> psiFiles = ep.getStepDefinitionContainers(featureFile);
      final BDDFrameworkType frameworkType = ep.getStepFileType();
      for (final PsiFile psiFile : psiFiles) {
        result.add(Pair.create(psiFile, frameworkType));
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

  private boolean isStepLikeFile(PsiElement child, PsiElement parent) {
    if (child instanceof PsiFile) {
      final PsiFile file = (PsiFile)child;
      CucumberJvmExtensionPoint ep = myExtensionMap.get(new BDDFrameworkType(file.getFileType()));
      return ep != null && ep.isStepLikeFile(file, parent);
    }

    return false;
  }

  private boolean isWritableStepLikeFile(PsiElement child, PsiElement parent) {
    if (child instanceof PsiFile) {
      final PsiFile file = (PsiFile)child;
      CucumberJvmExtensionPoint ep = myExtensionMap.get(new BDDFrameworkType(file.getFileType()));
      return ep != null && ep.isWritableStepLikeFile(file, parent);
    }

    return false;
  }
}
