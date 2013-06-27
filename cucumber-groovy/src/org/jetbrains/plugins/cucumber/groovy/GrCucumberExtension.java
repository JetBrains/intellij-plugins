package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinRecursiveElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.*;

/**
 * @author Max Medvedev
 */
public class GrCucumberExtension implements CucumberJvmExtensionPoint {
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

  @NotNull
  @Override
  public String getDefaultStepFileName() {
    return "StepDef";
  }

  @Override
  public List<PsiElement> resolveStep(@NotNull PsiElement element) {
    final CucumberStepsIndex index = CucumberStepsIndex.getInstance(element.getProject());

    if (element instanceof GherkinStep) {
      final GherkinStep step = (GherkinStep)element;
      final List<PsiElement> result = new ArrayList<PsiElement>();
      final Set<String> substitutedNameList = step.getSubstitutedNameList();
      if (substitutedNameList.size() > 0) {
        for (String s : substitutedNameList) {
          final AbstractStepDefinition definition = index.findStepDefinition(element.getContainingFile(), s);
          if (definition != null) {
            result.add(definition.getElement());
          }
        }
        return result;
      }
    }

    return Collections.emptyList();
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
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    return null;
  }

  @Override
  public void flush() {

  }

  @Override
  public void reset() {

  }

  @Override
  public void init(@NotNull Project project) {

  }
}
