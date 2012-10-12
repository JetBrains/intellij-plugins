package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.roots.impl.DirectoryInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.CreateClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import cucumber.runtime.java.JavaSnippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * User: Andrey.Vokin
 * Date: 8/1/12
 */
public class JavaStepDefinitionCreator implements StepDefinitionCreator {
  private static final Logger LOG = Logger.getInstance(JavaStepDefinitionCreator.class.getName());

  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    PsiClass newClass = CreateClassUtil.createClassNamed(name, CreateClassUtil.DEFAULT_CLASS_TEMPLATE, dir);
    assert newClass != null;
    return newClass.getContainingFile();
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
    if (!(file instanceof PsiJavaFile)) return false;

    final Project project = file.getProject();
    final VirtualFile vFile = ObjectUtils.assertNotNull(file.getVirtualFile());
    final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vFile);
    FileEditorManager.getInstance(project).getAllEditors(vFile);
    FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
    final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

    if (editor != null) {
      final TemplateManager templateManager = TemplateManager.getInstance(file.getProject());
      final TemplateState templateState = TemplateManagerImpl.getTemplateState(editor);
      final Template template = templateManager.getActiveTemplate(editor);
      if (templateState != null && template != null) {
        templateState.gotoEnd();
      }
    }

    // snippet text
    final PsiMethod element = buildStepDefinitionByStep(step);

    PsiClass clazz = PsiTreeUtil.getChildOfType(file, PsiClass.class);
    if (clazz != null) {
      PsiMethod addedElement = (PsiMethod)clazz.add(element);
      addedElement = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(addedElement);
      JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedElement);
    }

    return true;
  }

  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String fileName) {
    return fileName.toLowerCase(Locale.ENGLISH).endsWith("stepdefs");
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    if (featureFile != null) {
      PsiDirectory directory = featureFile.getContainingDirectory();
      if (directory != null && directory.getManager() != null) {
        PsiManager manager = directory.getManager();
        DirectoryIndex directoryIndex = DirectoryIndex.getInstance(manager.getProject());
        DirectoryInfo info = directoryIndex.getInfoForDirectory(directory.getVirtualFile());
        if (info != null) {
          VirtualFile sourceRoot = info.sourceRoot;
          if (info.sourceRoot.getName().equals("resources")) {
            final Module module = ProjectRootManager.getInstance(step.getProject()).getFileIndex().getModuleForFile(info.sourceRoot);
            if (module != null) {
              final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
              final VirtualFile resourceParent = info.sourceRoot.getParent();

              for (VirtualFile vFile : moduleRootManager.getSourceRoots()) {
                if (vFile.getPath().startsWith(resourceParent.getPath()) && vFile.getName().equals("java")) {
                  sourceRoot = vFile;
                  break;
                }
              }
            }
          }

          String packageName = CucumberJavaUtil.getPackageOfStepDef(step);
          if (packageName == null) {
            packageName = "";
          }

          packageName = packageName.replace('.', '/');
          try {
            // ToDo: I shouldn't create directories, only create VirtualFile object.
            final VirtualFile packageFile = VfsUtil.createDirectoryIfMissing(sourceRoot.getPath() + '/' + packageName);
            if (packageFile != null) {
              return PsiDirectoryFactory.getInstance(step.getProject()).createDirectory(packageFile);
            }
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
      }
    }

    assert featureFile != null;
    return ObjectUtils.assertNotNull(featureFile.getParent());
  }

  @NotNull
  @Override
  public String getStepDefinitionFilePath(@NotNull final PsiFile file) {
    final VirtualFile vFile = file.getVirtualFile();
    if (file instanceof PsiJavaFile && vFile != null) {
      String packageName = ((PsiJavaFile)file).getPackageName();
      if (StringUtil.isEmptyOrSpaces(packageName)) {
        return vFile.getNameWithoutExtension();
      }
      else {
        return packageName + "." + vFile.getNameWithoutExtension();
      }
    }
    return file.getName();
  }

  private static PsiMethod buildStepDefinitionByStep(@NotNull final GherkinStep step) {
    final PsiElementFactory factory = JavaPsiFacade.getInstance(step.getProject()).getElementFactory();
    final Step cucumberStep = new Step(new ArrayList<Comment>(), step.getKeyword().getText(), step.getStepName(), 0, null, null);
    final StringBuilder snippet =  new StringBuilder(new SnippetGenerator(new JavaSnippet()).getSnippet(cucumberStep).replace("PendingException", "cucumber.runtime.PendingException"));
    snippet.insert(1, "cucumber.annotation.en.");

    return factory.createMethodFromText(snippet.toString(), step);
  }
}
