package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import cucumber.runtime.java.JavaSnippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.ArrayList;

/**
 * User: Andrey.Vokin
 * Date: 8/1/12
 */
public class JavaStepDefinitionCreator implements StepDefinitionCreator {
  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
    if (!(file instanceof PsiJavaFile)) {
      return false;
    }

    final Project project = file.getProject();
    final VirtualFile vFile = file.getVirtualFile();
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
    }

    //final RCall call = (RCall)addedElement.getCall();
    //final RRegexpLiteral regexpLiteral = (RRegexpLiteral)call.getArguments().get(0);
    //final int regexpLiteralLength = regexpLiteral.getTextLength();
    //
    //final List<RPsiElement> statements = addedElement.getBlock().getCompoundStatement().getStatements();
    //
    //final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(addedElement);
    //
    //// regexp str
    //builder.replaceElement(regexpLiteral,
    //                       new TextRange(2, regexpLiteralLength - 2),
    //                       regexpLiteral.getText().substring(2, regexpLiteralLength - 2));
    //
    //// block vars
    //final RBlockVariables blockVars = addedElement.getBlock().getBlockVariables();
    //if (blockVars != null) {
    //  final List<RIdentifier> varsList = blockVars.getVariables();
    //  for (RIdentifier var : varsList) {
    //    builder.replaceElement(var, var.getText());
    //  }
    //}
    //
    //// pending
    //builder.replaceElement(statements.get(0), "pending");
    //builder.run();

    return true;

  }

  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String fileName) {
    return fileName.endsWith("MyStepdefs.java");
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull GherkinStep step) {
    final PsiFile featureFile = step.getContainingFile();
    PsiDirectory dir = featureFile.getParent();
    return dir;
  }

  @NotNull
  @Override
  public String getStepDefinitionFilePath(@NotNull final PsiFile file) {
    final VirtualFile vFile = file.getVirtualFile();
    if (file instanceof PsiJavaFile && vFile != null) {
      return ((PsiJavaFile)file).getPackageName() + "." + vFile.getNameWithoutExtension();
    }
    return file.getName();
  }

  private PsiMethod buildStepDefinitionByStep(@NotNull final GherkinStep step) {
    final PsiElementFactory factory = JavaPsiFacade.getInstance(step.getProject()).getElementFactory();
    final Step cucumberStep = new Step(new ArrayList<Comment>(), step.getKeyword().getText(), step.getStepName(), 0, null, null);
    final StringBuilder snippet =  new StringBuilder(new SnippetGenerator(new JavaSnippet()).getSnippet(cucumberStep).replace("PendingException", "cucumber.runtime.PendingException"));
    snippet.insert(1, "cucumber.annotation.en.");

    final PsiMethod element = factory.createMethodFromText(snippet.toString(), step);
    return element;
  }
}
