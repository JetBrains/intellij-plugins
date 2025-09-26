// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.CreateClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import cucumber.runtime.snippets.CamelCaseConcatenator;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.getCucumberStepAnnotations;

@NotNullByDefault
public class JavaStepDefinitionCreator extends AbstractStepDefinitionCreator {
  private static final String STEP_DEFINITION_SUFFIX = "MyStepdefs";
  private static final String FILE_TEMPLATE_CUCUMBER_JAVA_STEP_DEFINITION_JAVA = "Cucumber Java Step Definition.java";
  private static final String DEFAULT_STEP_KEYWORD = "Given";

  @Override
  public PsiFile createStepDefinitionContainer(PsiDirectory dir, String name) {
    PsiClass newClass = CreateClassUtil.createClassNamed(name, CreateClassUtil.DEFAULT_CLASS_TEMPLATE, dir);
    assert newClass != null;
    return newClass.getContainingFile();
  }

  @Override
  public boolean createStepDefinition(GherkinStep step, PsiFile file, boolean withTemplate) {
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

      if (withTemplate) {
        runTemplateBuilderOnAddedStep(editor, addedElement, regexpElement, blockVars, body);
      }
    }

    return true;
  }

  void runTemplateBuilderOnAddedStep(Editor editor,
                                     PsiElement addedElement,
                                     PsiElement regexpElement,
                                     PsiParameterList blockVars,
                                     PsiCodeBlock body) {
    Project project = regexpElement.getProject();
    final TemplateBuilderImpl builder = (TemplateBuilderImpl)TemplateBuilderFactory.getInstance().createTemplateBuilder(addedElement);

    final TextRange range = new TextRange(1, regexpElement.getTextLength() - 1);
    builder.replaceElement(regexpElement, range, range.substring(regexpElement.getText()));

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
                             pendingRange.substring(firstStatement.getText()));
    }

    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());

    Template template = builder.buildInlineTemplate();

    editor.getCaretModel().moveToOffset(addedElement.getTextRange().getStartOffset());
    TemplateManager.getInstance(project).startTemplate(editor, template);
  }

  @Override
  public boolean validateNewStepDefinitionFileName(Project project, String name) {
    if (name.isEmpty()) return false;
    if (!Character.isJavaIdentifierStart(name.charAt(0))) return false;
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isJavaIdentifierPart(name.charAt(i))) return false;
    }
    return true;
  }

  @Override
  public String getDefaultStepDefinitionFolderPath(GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    if (featureFile != null) {
      PsiDirectory psiDirectory = featureFile.getContainingDirectory();
      final Project project = step.getProject();
      if (psiDirectory != null) {
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        VirtualFile directory = psiDirectory.getVirtualFile();
        if (projectFileIndex.isInContent(directory)) {
          VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(directory);
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
          return FileUtil.join(path, packagePath);
        }
      }
    }

    assert featureFile != null;
    return Objects.requireNonNull(featureFile.getContainingDirectory()).getVirtualFile().getPath();
  }

  @Override
  public String getStepDefinitionFilePath(PsiFile file) {
    final VirtualFile vFile = file.getVirtualFile();
    if (file instanceof PsiClassOwner owner && vFile != null) {
      String packageName = owner.getPackageName();
      if (StringUtil.isEmptyOrSpaces(packageName)) {
        return vFile.getNameWithoutExtension();
      }
      else {
        return vFile.getNameWithoutExtension() + " (" + packageName + ")";
      }
    }
    return file.getName();
  }

  public static String processGeneratedStepDefinition(String stepDefinition, PsiElement context) {
    return stepDefinition.replace("PendingException", CucumberJavaUtil.getCucumberPendingExceptionFqn(context));
  }

  @Override
  public String getDefaultStepFileName(GherkinStep step) {
    return STEP_DEFINITION_SUFFIX;
  }

  private static PsiMethod buildStepDefinitionByStep(GherkinStep step, Language language) {
    String annotationPackage = new AnnotationPackageProvider().getAnnotationPackageFor(step);
    String methodAnnotation = String.format("@%s.", annotationPackage);

    final Step cucumberStep = new Step(new ArrayList<>(), step.getKeyword().getText(), step.getName(), 0, null, null);
    final SnippetGenerator generator = new SnippetGenerator(new JavaSnippet());

    String snippet = generator.getSnippet(cucumberStep, new FunctionNameGenerator(new CamelCaseConcatenator()));

    if (CucumberJavaUtil.isCucumberExpressionsAvailable(step)) {
      snippet = CucumberJavaUtil.replaceRegexpWithCucumberExpression(snippet, step.getName());
    }

    snippet = snippet.replaceFirst("@", methodAnnotation);
    snippet = processGeneratedStepDefinition(snippet, step);

    JVMElementFactory factory = JVMElementFactories.requireFactory(language, step.getProject());
    PsiMethod methodFromCucumberLibraryTemplate = factory.createMethodFromText(snippet, step);

    try {
      return createStepDefinitionFromSnippet(methodFromCucumberLibraryTemplate, step, factory);
    }
    catch (Exception e) {
      return methodFromCucumberLibraryTemplate;
    }
  }

  private static PsiMethod createStepDefinitionFromSnippet(PsiMethod methodFromSnippet,
                                                           GherkinStep step,
                                                           JVMElementFactory factory) {
    List<PsiAnnotation> annotationsFromSnippetMethod = getCucumberStepAnnotations(methodFromSnippet);
    PsiAnnotation cucumberStepAnnotation = annotationsFromSnippetMethod.getFirst();
    String regexp = CucumberJavaUtil.getPatternFromStepDefinition(cucumberStepAnnotation);
    String stepAnnotationName = cucumberStepAnnotation.getQualifiedName();
    if (stepAnnotationName == null) {
      stepAnnotationName = DEFAULT_STEP_KEYWORD;
    }

    FileTemplateDescriptor fileTemplateDescriptor = new FileTemplateDescriptor(FILE_TEMPLATE_CUCUMBER_JAVA_STEP_DEFINITION_JAVA);
    FileTemplate fileTemplate = FileTemplateManager.getInstance(step.getProject()).getCodeTemplate(fileTemplateDescriptor.getFileName());
    String text = fileTemplate.getText();
    text = text.replace("${STEP_KEYWORD}", stepAnnotationName).replace("${STEP_REGEXP}", "\"" + regexp + "\"")
      .replace("${METHOD_NAME}", methodFromSnippet.getName())
      .replace("${PARAMETERS}", methodFromSnippet.getParameterList().getText())
      .replace("${BODY}\n", """
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
        """);

    text = processGeneratedStepDefinition(text, methodFromSnippet);

    return factory.createMethodFromText(text, step);
  }
}
