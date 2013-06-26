package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;

import java.util.*;

/**
 * User: Andrey.Vokin
 * Date: 7/16/12
 */
public class CucumberJavaExtension implements CucumberJvmExtensionPoint {

  public static final String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";

  @Override
  public boolean isStepLikeFile(@NotNull final PsiElement child, @NotNull final PsiElement parent) {
    if (child instanceof PsiJavaFile) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return isStepLikeFile(child, parent);
  }

  @NotNull
  @Override
  public List<AbstractStepDefinition> getStepDefinitions(@NotNull PsiFile psiFile) {
    final List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();
    psiFile.acceptChildren(new JavaRecursiveElementVisitor() {
      @Override
      public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        if (CucumberJavaUtil.isStepDefinition(method)) {
          newDefs.add(new JavaStepDefinition(method));
        }
      }
    });
    return newDefs;
  }

  @NotNull
  @Override
  public FileType getStepFileType() {
    return JavaFileType.INSTANCE;
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new JavaStepDefinitionCreator();
  }

  @NotNull
  @Override
  public String getDefaultStepFileName() {
    return "MyStepdefs";
  }

  @Override
  public void collectAllStepDefsProviders(@NotNull List<VirtualFile> providers, @NotNull Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (ModuleType.get(module) instanceof JavaModuleType) {
        final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        ContainerUtil.addAll(providers, roots);
      }
    }
  }

  @Override
  public void loadStepDefinitionRootsFromLibraries(@NotNull Module module,
                                                   List<PsiDirectory> newAbstractStepDefinitionsRoots,
                                                   @NotNull Set<String> processedStepDirectories) {
  }

  @Override
  public List<PsiElement> resolveStep(@NotNull final PsiElement element) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module == null) {
      return Collections.emptyList();
    }
    if (!(element instanceof GherkinStep)) {
      return Collections.emptyList();
    }
    final GherkinStep step = (GherkinStep)element;

    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    Collection<PsiClass> stepDefAnnotationCandidates = JavaFullClassNameIndex.getInstance().get(
      CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION.hashCode(), module.getProject(), dependenciesScope);

    PsiClass stepDefAnnotationClass = null;
    for (PsiClass candidate : stepDefAnnotationCandidates) {
      if (CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION.equals(candidate.getQualifiedName())) {
        stepDefAnnotationClass = candidate;
        break;
      }
    }
    if (stepDefAnnotationClass == null) {
      return Collections.emptyList();
    }

    final List<PsiElement> result = new ArrayList<PsiElement>();
    final Query<PsiClass> stepDefAnnotations = AnnotatedElementsSearch.searchPsiClasses(stepDefAnnotationClass, dependenciesScope);
    for (PsiClass annotationClass : stepDefAnnotations) {
      final Query<PsiMethod> javaStepDefinitions = AnnotatedElementsSearch.searchPsiMethods(annotationClass, dependenciesScope);
      for (PsiMethod stepDefMethod : javaStepDefinitions) {
        final JavaStepDefinition stepDef = new JavaStepDefinition(stepDefMethod);

        final Set<String> substitutedNameList = step.getSubstitutedNameList();
        if (substitutedNameList.size() > 0) {
          for (String s : substitutedNameList) {
            if (stepDef.matches(s)) {
              result.add(stepDef.getElement());
            }
          }
        }
      }
    }

    return result;
  }

  @Override
  public void findRelatedStepDefsRoots(@NotNull final Module module, @NotNull final PsiFile featureFile,
                                       @NotNull final List<PsiDirectory> newStepDefinitionsRoots,
                                       @NotNull final Set<String> processedStepDirectories) {


    final ModuleRootManager mrm = ModuleRootManager.getInstance(module);
    final List<Module> modules = new ArrayList<Module>(Arrays.asList(mrm.getDependencies()));
    modules.add(module);

    for (Module mod : modules) {
      final ContentEntry[] contentEntries = ModuleRootManager.getInstance(mod).getContentEntries();
      for (final ContentEntry contentEntry : contentEntries) {
        final SourceFolder[] sourceFolders = contentEntry.getSourceFolders();
        for (SourceFolder sf : sourceFolders) {
          VirtualFile sfDirectory = sf.getFile();
          if (sfDirectory != null && sfDirectory.isDirectory()) {
            PsiDirectory sourceRoot = PsiDirectoryFactory.getInstance(module.getProject()).createDirectory(sfDirectory);
            if (!processedStepDirectories.contains(sourceRoot.getVirtualFile().getPath())) {
              newStepDefinitionsRoots.add(sourceRoot);
            }
          }
        }
      }
    }
  }

  @Nullable
  public String getGlue(@NotNull GherkinStep step) {
    return CucumberJavaUtil.getPackageOfStep(step);
  }

  @NotNull
  @Override
  public Collection<String> getGlues(@NotNull GherkinFile file, Set<String> gluesFromOtherFiles) {
    if (gluesFromOtherFiles == null) {
      gluesFromOtherFiles = ContainerUtil.newHashSet();
    }
    final Set<String> glues = gluesFromOtherFiles;

    file.accept(new GherkinRecursiveElementVisitor() {
      @Override
      public void visitStep(GherkinStep step) {
        boolean covered = false;
        final String glue = getGlue(step);
        if (glue != null) {
          final Set<String> toRemove = ContainerUtil.newHashSet();
          for (String existedGlue : glues) {
            if (glue.startsWith(existedGlue + ".")) {
              covered = true;
              break;
            }
            else if (existedGlue.startsWith(glue + ".")) {
              toRemove.add(existedGlue);
            }
          }

          for (String removing : toRemove) {
            glues.remove(removing);
          }

          if (!covered) {
            glues.add(glue);
          }
        }
      }
    });

    return glues;
  }
}
