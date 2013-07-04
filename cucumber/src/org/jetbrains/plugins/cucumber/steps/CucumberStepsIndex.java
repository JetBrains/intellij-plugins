package org.jetbrains.plugins.cucumber.steps;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.*;

/**
 * @author yole
 */
public class CucumberStepsIndex {
  private static final Logger LOG = Logger.getInstance(CucumberStepsIndex.class.getName());

  private final Map<FileType, CucumberJvmExtensionPoint> myExtensionMap;

  public static CucumberStepsIndex getInstance(Project project) {
    return ServiceManager.getService(project, CucumberStepsIndex.class);
  }

  public CucumberStepsIndex(final Project project) {
    myExtensionMap = new HashMap<FileType, CucumberJvmExtensionPoint>();

    for (CucumberJvmExtensionPoint e : Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME)) {
      e.init(project);
      myExtensionMap.put(e.getStepFileType(), e);
    }
  }

  /**
   * Creates a file that will contain step definitions
   *
   * @param dir                      container for created file
   * @param fileNameWithoutExtension name of the file with out "." and extension
   * @param fileType                 type of file to create
   */
  public PsiFile createStepDefinitionFile(@NotNull final PsiDirectory dir,
                                          @NotNull final String fileNameWithoutExtension,
                                          @NotNull final FileType fileType) {
    final CucumberJvmExtensionPoint ep = myExtensionMap.get(fileType);
    if (ep == null) {
      LOG.error(String.format("Unsupported step definition file type %s", fileType.getName()));
      return null;
    }

    return ep.getStepDefinitionCreator().createStepDefinitionContainer(dir, fileNameWithoutExtension);
  }

  public boolean validateNewStepDefinitionFileName(@NotNull final PsiDirectory directory,
                                                   @NotNull final String fileName,
                                                   @NotNull final FileType fileType) {
    final CucumberJvmExtensionPoint ep = myExtensionMap.get(fileType);
    assert ep != null;
    return ep.getStepDefinitionCreator().validateNewStepDefinitionFileName(directory.getProject(), fileName);
  }


  @Nullable
  public AbstractStepDefinition findStepDefinition(final @NotNull PsiFile featureFile, final String stepName) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) return null;

    List<AbstractStepDefinition> allSteps = loadStepsFor(featureFile, module);
    for (AbstractStepDefinition stepDefinition : allSteps) {
      if (stepDefinition.matches(stepName)) {
        return stepDefinition;
      }
    }
    return null;
  }

   // ToDo: use binary search here
  public List<AbstractStepDefinition> findStepDefinitionsByPattern(@NotNull final String pattern, @NotNull final Module module) {
    final List<AbstractStepDefinition> allSteps = loadStepsFor(null, module);
    final List<AbstractStepDefinition> result = new ArrayList<AbstractStepDefinition>();
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
    final List<PsiFile> result = new ArrayList<PsiFile>();

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
    ArrayList<AbstractStepDefinition> result = new ArrayList<AbstractStepDefinition>();

    for (CucumberJvmExtensionPoint extension : myExtensionMap.values()) {
      result.addAll(extension.loadStepsFor(featureFile, module));
    }
    return result;
  }

  public Set<PsiFile> getStepDefinitionContainers(@Nullable final GherkinFile featureFile) {
    Set<PsiFile> result = new HashSet<PsiFile>();
    for (CucumberJvmExtensionPoint ep: myExtensionMap.values()) {
      result.addAll(ep.getStepDefinitionContainers(featureFile));
    }
    return result;
  }

  public void reset() {
    for (CucumberJvmExtensionPoint e : myExtensionMap.values()) {
      e.reset();
    }
  }

  public void flush() {
    for (CucumberJvmExtensionPoint e : myExtensionMap.values()) {
      e.flush();
    }
  }

  public Map<FileType, CucumberJvmExtensionPoint> getExtensionMap() {
    return myExtensionMap;
  }

  public int getExtensionCount() {
    return myExtensionMap.size();
  }

  private boolean isStepLikeFile(PsiElement child, PsiElement parent) {
    if (child instanceof PsiFile) {
      final PsiFile file = (PsiFile)child;
      CucumberJvmExtensionPoint ep = myExtensionMap.get(file.getFileType());
      return ep != null && ep.isStepLikeFile(file, parent);
    }

    return false;
  }

  private boolean isWritableStepLikeFile(PsiElement child, PsiElement parent) {
    if (child instanceof PsiFile) {
      final PsiFile file = (PsiFile)child;
      CucumberJvmExtensionPoint ep = myExtensionMap.get(file.getFileType());
      return ep != null && ep.isWritableStepLikeFile(file, parent);
    }

    return false;
  }
}
