// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.steps;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import cucumber.runtime.groovy.GroovySnippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberCommonClassNames;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.java.config.CucumberConfigUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.actions.GroovyTemplatesFactory;
import org.jetbrains.plugins.groovy.intentions.base.IntentionUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import java.util.Collections;
import java.util.Objects;

/**
 * @author Max Medvedev
 */
public class GrStepDefinitionCreator implements StepDefinitionCreator {

  public static final String GROOVY_STEP_DEFINITION_FILE_TMPL_PREFIX = "GroovyStepDefinitionFile";

  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    String fileName = name + '.' + GroovyFileType.DEFAULT_EXTENSION;
    final String version = CucumberConfigUtil.getCucumberCoreVersion(dir);
    String templateFileName = GROOVY_STEP_DEFINITION_FILE_TMPL_PREFIX
            + GrCucumberCommonClassNames.cucumberTemplateVersion(version)
            + ".groovy";
    return GroovyTemplatesFactory.createFromTemplate(dir, name, fileName, templateFileName, true);
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull final PsiFile file, boolean withTemplate) {
    if (!(file instanceof GroovyFile)) return false;

    final Project project = file.getProject();
    final VirtualFile vFile = Objects.requireNonNull(file.getVirtualFile());
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
    final GrMethodCall element = buildStepDefinitionByStep(step);

    GrMethodCall methodCall = (GrMethodCall)((GroovyFile)file).addStatementBefore(element, null);
    JavaCodeStyleManager.getInstance(project).shortenClassReferences(methodCall);
    methodCall = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(methodCall);

    PsiDocumentManager.getInstance(project).commitAllDocuments();

    if (ApplicationManager.getApplication().isUnitTestMode()) return true;

    final TemplateBuilderImpl builder = (TemplateBuilderImpl)TemplateBuilderFactory.getInstance().createTemplateBuilder(methodCall);

    // regexp str
    GrLiteral pattern = GrCucumberUtil.getStepDefinitionPattern(methodCall);
    assert pattern != null;

    String patternText = pattern.getText();

    builder.replaceElement(pattern,
                           new TextRange(1, patternText.length() - 1),
                           patternText.substring(1, patternText.length() - 1));

    // block vars
    GrClosableBlock closure = methodCall.getClosureArguments()[0];
    final GrParameter[] blockVars = closure.getAllParameters();
    for (GrParameter var : blockVars) {
      PsiElement identifier = var.getNameIdentifierGroovy();
      builder.replaceElement(identifier, identifier.getText());
    }

    if (!withTemplate) {
      return true;
    }

    TemplateManager manager = TemplateManager.getInstance(project);

    final Editor editorToRunTemplate;
    if (editor == null) {
      editorToRunTemplate = IntentionUtils.positionCursor(project, file, methodCall);
    }
    else {
      editorToRunTemplate = editor;
    }

    Template template = builder.buildTemplate();

    TextRange range = methodCall.getTextRange();
    editorToRunTemplate.getDocument().deleteString(range.getStartOffset(), range.getEndOffset());
    editorToRunTemplate.getCaretModel().moveToOffset(range.getStartOffset());

    manager.startTemplate(editorToRunTemplate, template, new TemplateEditingAdapter() {
      @Override
      public void templateFinished(@NotNull Template template, boolean brokenOff) {
        if (brokenOff) return;

        ApplicationManager.getApplication().runWriteAction(() -> {
          PsiDocumentManager.getInstance(project).commitDocument(editorToRunTemplate.getDocument());
          final int offset = editorToRunTemplate.getCaretModel().getOffset();
          GrMethodCall methodCall1 = PsiTreeUtil.findElementOfClassAtOffset(file, offset - 1, GrMethodCall.class, false);
          if (methodCall1 != null) {
            GrClosableBlock[] closures = methodCall1.getClosureArguments();
            if (closures.length == 1) {
              GrClosableBlock closure1 = closures[0];
              selectBody(closure1, editor);
            }
          }
        });
      }
    });

    return true;
  }

  private static void selectBody(GrClosableBlock closure, Editor editor) {
    PsiElement arrow = closure.getArrow();
    PsiElement leftBound = PsiUtil.skipWhitespaces((arrow != null ? arrow : closure.getParameterList()).getNextSibling(), true);

    PsiElement rbrace = closure.getRBrace();
    PsiElement rightBound = rbrace != null ? PsiUtil.skipWhitespaces(rbrace.getPrevSibling(), false) : null;

    if (leftBound != null && rightBound != null) {
      editor.getSelectionModel().setSelection(leftBound.getTextRange().getStartOffset(), rightBound.getTextRange().getEndOffset());
      editor.getCaretModel().moveToOffset(leftBound.getTextRange().getStartOffset());
    }
  }

  private static GrMethodCall buildStepDefinitionByStep(@NotNull final GherkinStep step) {
    final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(step.getProject());

    final Step cucumberStep = new Step(Collections.emptyList(), step.getKeyword().getText(), step.getName(), 0, null, null);

    SnippetGenerator generator = new SnippetGenerator(new GroovySnippet());
    String snippet = generator.getSnippet(cucumberStep, null);

    return (GrMethodCall)factory.createStatementFromText(snippet, step);
  }

  @NotNull
  @Override
  public String getDefaultStepDefinitionFolderPath(@NotNull GherkinStep step) {
    final PsiFile featureFile = step.getContainingFile();
    return Objects.requireNonNull(featureFile.getContainingDirectory()).getVirtualFile().getPath();
  }

  @NotNull
  @Override
  public String getStepDefinitionFilePath(@NotNull PsiFile file) {
    final VirtualFile vFile = file.getVirtualFile();
    if (file instanceof GroovyFile && vFile != null) {
      String packageName = ((GroovyFile)file).getPackageName();
      if (StringUtil.isEmptyOrSpaces(packageName)) {
        return vFile.getNameWithoutExtension();
      }
      else {
        return packageName + "." + vFile.getNameWithoutExtension();
      }
    }
    return file.getName();
  }

  @NotNull
  @Override
  public String getDefaultStepFileName(@NotNull final GherkinStep step) {
    return "StepDef";
  }
}
