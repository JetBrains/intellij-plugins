// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.Language;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import cucumber.runtime.snippets.CamelCaseConcatenator;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.ArrayList;

public class Java8StepDefinitionCreator extends JavaStepDefinitionCreator {
  public static final String CUCUMBER_API_JAVA8_EN = "cucumber.api.java8.En";
  private static final String FILE_TEMPLATE_CUCUMBER_JAVA_8_STEP_DEFINITION_JAVA = "Cucumber Java 8 Step Definition.java";

  @Override
  public @NotNull PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    final PsiFile result =  super.createStepDefinitionContainer(dir, name);

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(dir.getProject()).getFileIndex();
    final Module module = fileIndex.getModuleForFile(result.getVirtualFile());
    assert module != null;
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    final PsiClass stepDefContainerInterface =
      JavaPsiFacade.getInstance(module.getProject()).findClass(CUCUMBER_API_JAVA8_EN, dependenciesScope);

    if (stepDefContainerInterface != null) {
      final PsiClass createPsiClass = PsiTreeUtil.getChildOfType(result, PsiClass.class);
      assert createPsiClass != null;
      final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(dir.getProject()).getElementFactory();
      PsiJavaCodeReferenceElement ref = elementFactory.createClassReferenceElement(stepDefContainerInterface);
      if (stepDefContainerInterface.isInterface()) {
        PsiReferenceList implementsList = createPsiClass.getImplementsList();
        if (implementsList != null) {
          WriteAction.run(() -> implementsList.add(ref));
        }
      }
    }

    return result;
  }

  @Override
  public @NotNull String getStepDefinitionFilePath(@NotNull PsiFile file) {
    return super.getStepDefinitionFilePath(file) + " (Java 8 style)";
  }

  private static PsiMethod getConstructor(PsiClass clazz) {
    if (clazz.getConstructors().length == 0) {
      JVMElementFactory factory = JVMElementFactories.requireFactory(clazz.getLanguage(), clazz.getProject());
      PsiMethod constructor = factory.createConstructor(clazz.getName());
      return (PsiMethod)clazz.add(constructor);
    }
    return clazz.getConstructors()[0];
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file, boolean withTemplate) {
    if (!(file instanceof PsiClassOwner)) return false;

    final PsiClass clazz = PsiTreeUtil.getChildOfType(file, PsiClass.class);
    if (clazz == null) {
      return false;
    }

    final Project project = file.getProject();
    closeActiveTemplateBuilders(file);
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    final PsiElement stepDef = buildStepDefinitionByStep(step, file.getLanguage());

    final PsiMethod constructor = getConstructor(clazz);
    final PsiCodeBlock constructorBody = constructor.getBody();
    if (constructorBody == null) {
      return false;
    }

    PsiElement anchor = constructorBody.getFirstChild();
    if (constructorBody.getStatements().length > 0) {
      anchor = constructorBody.getStatements()[constructorBody.getStatements().length - 1];
    }
    PsiElement addedStepDef = constructorBody.addAfter(stepDef, anchor);
    wrapStepDefWithLineBreakAndSemicolon(addedStepDef);

    addedStepDef = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(addedStepDef);

    JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedStepDef);

    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    assert editor != null;

    if (!(addedStepDef instanceof PsiMethodCallExpression stepDefCall)) {
      return false;
    }
    if (stepDefCall.getArgumentList().getExpressions().length < 2) {
      return false;
    }

    final PsiExpression regexpElement = stepDefCall.getArgumentList().getExpressions()[0];

    final PsiExpression secondArgument = stepDefCall.getArgumentList().getExpressions()[1];
    if (!(secondArgument instanceof PsiLambdaExpression lambda)) {
      return false;
    }
    final PsiParameterList blockVars = lambda.getParameterList();
    PsiElement lambdaBody = lambda.getBody();
    if (!(lambdaBody instanceof PsiCodeBlock body)) {
      return false;
    }

    if (withTemplate) {
      runTemplateBuilderOnAddedStep(editor, addedStepDef, regexpElement, blockVars, body);
    }

    return true;
  }

  protected void wrapStepDefWithLineBreakAndSemicolon(PsiElement addedStepDef) {
    LeafElement linebreak = Factory.createSingleLeafElement(TokenType.WHITE_SPACE, "\n", 0, 1, null, addedStepDef.getManager());
    addedStepDef.getParent().addBefore(linebreak.getPsi(), addedStepDef);

    LeafElement semicolon = Factory.createSingleLeafElement(JavaTokenType.SEMICOLON, ";", 0, 1, null, addedStepDef.getManager());
    addedStepDef.getParent().addAfter(semicolon.getPsi(), addedStepDef);
  }

  private static PsiElement buildStepDefinitionByStep(final @NotNull GherkinStep step, Language language) {
    final Step cucumberStep = new Step(new ArrayList<>(), step.getKeyword().getText(), step.getName(), 0, null, null);
    final SnippetGenerator generator = new SnippetGenerator(new Java8Snippet());

    String snippetTemplate = generator.getSnippet(cucumberStep, new FunctionNameGenerator(new CamelCaseConcatenator()));
    String snippet = processGeneratedStepDefinition(snippetTemplate, step);

    JVMElementFactory factory = JVMElementFactories.requireFactory(language, step.getProject());
    PsiElement expression =  factory.createExpressionFromText(snippet, step);

    try {
      return createStepDefinitionFromSnippet(expression, step, factory);
    } catch (Exception e) {
      return expression;
    }
  }

  private static PsiElement createStepDefinitionFromSnippet(@NotNull PsiElement snippetExpression, @NotNull GherkinStep step,
                                                            @NotNull JVMElementFactory factory) {
    PsiMethodCallExpression callExpression = (PsiMethodCallExpression)snippetExpression;
    PsiExpression[] arguments = callExpression.getArgumentList().getExpressions();
    PsiLambdaExpression lambda = (PsiLambdaExpression)arguments[1];

    FileTemplateDescriptor fileTemplateDescriptor = new FileTemplateDescriptor(FILE_TEMPLATE_CUCUMBER_JAVA_8_STEP_DEFINITION_JAVA);
    FileTemplate fileTemplate = FileTemplateManager.getInstance(snippetExpression.getProject()).getCodeTemplate(fileTemplateDescriptor.getFileName());
    String text = fileTemplate.getText().replace("${STEP_KEYWORD}", callExpression.getMethodExpression().getText())
      .replace("${STEP_REGEXP}", arguments[0].getText())
      .replace("${PARAMETERS}", lambda.getParameterList().getText())
      .replace("${BODY}\n", "");

    text = processGeneratedStepDefinition(text, snippetExpression);

    return factory.createExpressionFromText(text, step);
  }
}
