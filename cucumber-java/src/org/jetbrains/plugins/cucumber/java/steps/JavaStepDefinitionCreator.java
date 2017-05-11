package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.CreateClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import cucumber.runtime.snippets.CamelCaseConcatenator;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.ArrayList;

public class JavaStepDefinitionCreator extends AbstractStepDefinitionCreator {
  public static final String STEP_DEFINITION_SUFFIX = "MyStepdefs";

  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    PsiClass newClass = CreateClassUtil.createClassNamed(name, CreateClassUtil.DEFAULT_CLASS_TEMPLATE, dir);
    assert newClass != null;
    return newClass.getContainingFile();
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
    if (!(file instanceof PsiClassOwner)) return false;

    final Project project = file.getProject();
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    assert editor != null;

    closeActiveTemplateBuilders(file);

    final PsiClass clazz = PsiTreeUtil.getChildOfType(file, PsiClass.class);
    if (clazz != null) {
      PsiDocumentManager.getInstance(project).commitAllDocuments();

      // snippet text
      final PsiMethod element = buildStepDefinitionByStep(step, file.getLanguage());
      PsiMethod addedElement = (PsiMethod)clazz.add(element);
      addedElement = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(addedElement);
      JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedElement);
      editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      assert editor != null;

      final PsiParameterList blockVars = addedElement.getParameterList();
      final PsiCodeBlock body = addedElement.getBody();
      final PsiAnnotation annotation = addedElement.getModifierList().getAnnotations()[0];
      final PsiElement regexpElement = annotation.getParameterList().getAttributes()[0];

      runTemplateBuilderOnAddedStep(editor, addedElement, regexpElement, blockVars, body);
    }

    return true;
  }

  protected void runTemplateBuilderOnAddedStep(Editor editor,
                                               PsiElement addedElement,
                                               PsiElement regexpElement,
                                               PsiParameterList blockVars, PsiCodeBlock body) {
    Project project = regexpElement.getProject();
    final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(addedElement);

    final TextRange range = new TextRange(1, regexpElement.getTextLength() - 1);
    builder.replaceElement(regexpElement, range, regexpElement.getText().substring(range.getStartOffset(), range.getEndOffset()));

    for (PsiParameter var : blockVars.getParameters()) {
      final PsiElement nameIdentifier = var.getNameIdentifier();
      if (nameIdentifier != null) {
        builder.replaceElement(nameIdentifier, nameIdentifier.getText());
      }
    }

    if (body.getStatements().length > 0) {
      final PsiElement firstStatement = body.getStatements()[0];
      final TextRange pendingRange = new TextRange(0, firstStatement.getTextLength() - 1);
      builder.replaceElement(firstStatement, pendingRange,
                             firstStatement.getText().substring(pendingRange.getStartOffset(), pendingRange.getEndOffset()));
    }

    final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());
    builder.run(editor, false);
  }


  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull final Project project, @NotNull final String name) {
    if (name.length() == 0) return false;
    if (!Character.isJavaIdentifierStart(name.charAt(0))) return false;
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i))) return false;
    }
    return true;
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull final GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    if (featureFile != null) {
      PsiDirectory psiDirectory = featureFile.getContainingDirectory();
      final Project project = step.getProject();
      if (psiDirectory != null) {
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        VirtualFile directory = psiDirectory.getVirtualFile();
        if (projectFileIndex.isInContent(directory)) {
          VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(directory);
          //noinspection ConstantConditions
          final Module module = projectFileIndex.getModuleForFile(featureFile.getVirtualFile());
          if (module != null) {
            final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
            if (sourceRoot != null && sourceRoot.getName().equals("resources")) {
              final VirtualFile resourceParent = sourceRoot.getParent();
              for (VirtualFile vFile : sourceRoots) {
                if (vFile.getPath().startsWith(resourceParent.getPath()) && vFile.getName().equals("java")) {
                  sourceRoot = vFile;
                  break;
                }
              }
            }
            else {
              if (sourceRoots.length > 0) {
                sourceRoot = sourceRoots[sourceRoots.length - 1];
              }
            }
          }
          String packageName = "";
          if (sourceRoot != null) {
            packageName = CucumberJavaUtil.getPackageOfStepDef(step);
          }

          final String packagePath = packageName.replace('.', '/');
          final String path = sourceRoot != null ? sourceRoot.getPath() : directory.getPath();
          // ToDo: I shouldn't create directories, only create VirtualFile object.
          final Ref<PsiDirectory> resultRef = new Ref<>();
          new WriteAction() {
            protected void run(@NotNull Result result) throws Throwable {
              final VirtualFile packageFile = VfsUtil.createDirectoryIfMissing(path + '/' + packagePath);
              if (packageFile != null) {
                resultRef.set(PsiDirectoryFactory.getInstance(project).createDirectory(packageFile));
              }
            }
          }.execute();
          return resultRef.get();
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
    if (file instanceof PsiClassOwner && vFile != null) {
      String packageName = ((PsiClassOwner)file).getPackageName();
      if (StringUtil.isEmptyOrSpaces(packageName)) {
        return vFile.getNameWithoutExtension();
      }
      else {
        return vFile.getNameWithoutExtension() + " (" + packageName + ")";
      }
    }
    return file.getName();
  }

  @NotNull
  @Override
  public String getDefaultStepFileName(@NotNull final GherkinStep step) {
    return STEP_DEFINITION_SUFFIX;
  }

  private static PsiMethod buildStepDefinitionByStep(@NotNull final GherkinStep step, Language language) {
    String annotationPackage = new AnnotationPackageProvider().getAnnotationPackageFor(step);
    String methodAnnotation = String.format("@%s.", annotationPackage);

    final Step cucumberStep = new Step(new ArrayList<>(), step.getKeyword().getText(), step.getStepName(), 0, null, null);
    final SnippetGenerator generator = new SnippetGenerator(new JavaSnippet());

    final String snippet = generator.getSnippet(cucumberStep, new FunctionNameGenerator(new CamelCaseConcatenator()))
      .replace("PendingException", CucumberJavaUtil.getCucumberPendingExceptionFqn(step))
      .replaceFirst("@", methodAnnotation)
      .replaceAll("\\\\\\\\", "\\\\")
      .replaceAll("\\\\d", "\\\\\\\\d");

    JVMElementFactory factory = JVMElementFactories.requireFactory(language, step.getProject());
    return factory.createMethodFromText(snippet, step);
  }
}

