package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.NotIndexedCucumberExtension;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.*;

/**
 * @author Max Medvedev
 */
public class GrCucumberExtension extends NotIndexedCucumberExtension {
  @Override
  public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return child instanceof GroovyFile && ((GroovyFile)child).getName().endsWith(".groovy");
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return isStepLikeFile(child, parent);
  }

  @NotNull
  @Override
  public FileType getStepFileType() {
    return GroovyFileType.GROOVY_FILE_TYPE;
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new GrStepDefinitionCreator();
  }

  @Nullable
  public String getGlue(@NotNull GherkinStep step) {
    for (PsiReference ref : step.getReferences()) {
      PsiElement refElement = ref.resolve();
      if (refElement != null && refElement instanceof GrMethodCall) {
        GroovyFile groovyFile = (GroovyFile)refElement.getContainingFile();
        VirtualFile vfile = groovyFile.getVirtualFile();
        if (vfile != null) {
          VirtualFile parentDir = vfile.getParent();
          return PathUtil.getLocalPath(parentDir);
        }
      }
    }
    return null;
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
        final String glue = getGlue(step);
        if (glue != null) {
          glues.add(glue);
        }
      }
    });

    return glues;
  }

  @Override
  protected void loadStepDefinitionRootsFromLibraries(Module module, List<PsiDirectory> roots, Set<String> directories) {

  }

  @NotNull
  @Override
  public List<AbstractStepDefinition> getStepDefinitions(@NotNull PsiFile psiFile) {
    final List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();
    if (psiFile instanceof GroovyFile) {
      GrStatement[] statements = ((GroovyFile)psiFile).getStatements();
      for (GrStatement statement : statements) {
        if (GrCucumberUtil.isStepDefinition(statement)) {
          newDefs.add(GrStepDefinition.getStepDefinition((GrMethodCall)statement));
        }
      }
    }
    return newDefs;
  }

  @Override
  protected void collectAllStepDefsProviders(@NotNull List<VirtualFile> providers, @NotNull Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    for (Module module : modules) {
      if (ModuleType.get(module) instanceof JavaModuleType) {
        final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
        ContainerUtil.addAll(providers, roots);
      }
    }
  }


  @Override
  public void findRelatedStepDefsRoots(@NotNull final Module module, @NotNull final PsiFile featureFile,
                                       List<PsiDirectory> newStepDefinitionsRoots, Set<String> processedStepDirectories) {
    final ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();
    for (final ContentEntry contentEntry : contentEntries) {
      final SourceFolder[] sourceFolders = contentEntry.getSourceFolders();
      for (SourceFolder sf : sourceFolders) {
        // ToDo: check if inside test folder
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
