/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.annotator;

import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeView;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.thoughtworks.gauge.GaugeBootstrapService;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CreateStepImplFix extends BaseIntentionAction {
  private static final Logger LOG = Logger.getInstance(CreateStepImplFix.class);

  private static final PsiFile NEW_FILE_HOLDER = null;
  public static final String IMPLEMENTATION = "implementation";
  private final SpecStep step;

  public CreateStepImplFix(SpecStep step) {
    this.step = step;
  }

  @NotNull
  @Override
  public String getText() {
    return GaugeBundle.message("intention.name.create.step.implementation");
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return GaugeBundle.message("intention.family.name.step.implementation");
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    Module module = GaugeUtil.moduleForPsiElement(step);
    return module != null && GaugeUtil.isGaugeFile(file.getVirtualFile()) && GaugeUtil.isGaugeModule(module);
  }

  @Override
  public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    return IntentionPreviewInfo.EMPTY;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        GaugeBootstrapService bootstrapService = GaugeBootstrapService.getInstance(project);

        List<PsiFile> javaFiles = bootstrapService.getSubModules(GaugeUtil.moduleForPsiElement(file)).stream()
          .map(FileManager::getAllJavaFiles)
          .flatMap(List::stream)
          .collect(Collectors.toList());
        javaFiles.add(0, NEW_FILE_HOLDER);
        ListPopup stepImplChooser =
          JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>(
            GaugeBundle.message("popup.title.choose.implementation.class"), javaFiles) {

            @Override
            public boolean isSpeedSearchEnabled() {
              return true;
            }

            @Override
            public PopupStep<?> onChosen(final PsiFile selectedValue, boolean finalChoice) {
              return doFinalStep(() -> {
                if (selectedValue == NEW_FILE_HOLDER) {
                  createFileAndAddImpl(project, editor);
                }
                else {
                  addImpl(project, selectedValue.getVirtualFile());
                }
              });
            }

            @Override
            public Icon getIconFor(PsiFile aValue) {
              return aValue == null ? AllIcons.Actions.IntentionBulb : aValue.getIcon(0);
            }

            @NotNull
            @Override
            public String getTextFor(PsiFile value) {
              return value == null ? GaugeBundle.message("create.new.file") : getJavaFileName(value);
            }
          });
        stepImplChooser.showCenteredInCurrentWindow(step.getProject());
      }

      private @NlsSafe String getJavaFileName(PsiFile value) {
        PsiJavaFile javaFile = (PsiJavaFile)value;
        if (!javaFile.getPackageName().isEmpty()) {
          return javaFile.getPackageName() + "." + javaFile.getName();
        }
        return javaFile.getName();
      }
    });
  }

  private void createFileAndAddImpl(@NotNull Project project, Editor editor) {
    ActionManager instance = ActionManager.getInstance();
    DataContext dataContext = SimpleDataContext.builder()
      .setParent(EditorUtil.getEditorDataContext(editor))
      .add(CommonDataKeys.PROJECT, project)
      .add(LangDataKeys.IDE_VIEW, new IdeView() {
        @Override
        public PsiDirectory @NotNull [] getDirectories() {
          List<PsiDirectory> psiDirectories = new ArrayList<>();
          PsiManager psiManager = PsiManager.getInstance(project);
          for (VirtualFile root : ProjectRootManager.getInstance(psiManager.getProject()).getContentSourceRoots()) {
            PsiDirectory directory = psiManager.findDirectory(root);
            if (directory != null) {
              psiDirectories.add(directory);
            }
          }
          return psiDirectories.toArray(PsiDirectory.EMPTY_ARRAY);
        }

        @Override
        public @Nullable PsiDirectory getOrChooseDirectory() {
          return DirectoryChooserUtil.getOrChooseDirectory(this);
        }
      })
      .build();

    AnActionEvent anActionEvent =
      new AnActionEvent(null, dataContext, ActionPlaces.UNKNOWN, new Presentation(
        GaugeBundle.message("action.create.class.text")), instance, 0);

    GaugeCreateClassAction createClassAction = new GaugeCreateClassAction();
    createClassAction.actionPerformed(anActionEvent);
    VirtualFile createdFile = createClassAction.getCreatedFile();
    if (createdFile != null) {
      addImpl(CommonDataKeys.PROJECT.getData(dataContext), createdFile);
    }
  }

  private void addImpl(Project project, VirtualFile file) {
    ApplicationManager.getApplication().invokeLater(() -> {
      PsiFile psifile = PsiManager.getInstance(project).findFile(file);
      WriteCommandAction.runWriteCommandAction(project, GaugeBundle.message("gauge.create.step.fix"), "Gauge", () -> {
        if (!FileModificationService.getInstance().prepareFileForWrite(psifile)) {
          return;
        }
        PsiMethod addedStepImpl = addStepImplMethod(psifile, project);
        if (addedStepImpl == null) return;

        TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(addedStepImpl);
        templateMethodName(addedStepImpl, builder);
        templateParams(addedStepImpl, builder);
        templateBody(addedStepImpl, builder);
        userTemplateModify(builder, project, file);
      }, psifile);
    });
  }

  @Nullable
  private PsiMethod addStepImplMethod(PsiFile psifile, Project project) {
    PsiClass psiClass = PsiTreeUtil.getChildOfType(psifile, PsiClass.class);
    if (psiClass == null) return null;

    PsiDocumentManager.getInstance(project).commitAllDocuments();

    StepValue stepValue = step.getStepValue();

    String text = String.format("@" + Step.class.getName() + "(\"%s\")\n", stepValue.getStepAnnotationText()) +
                  String.format("public void %s(%s){\n\n", getMethodName(psiClass), getParamList(stepValue.getParameters())) +
                  "}\n";
    PsiMethod stepMethod = JavaPsiFacade.getElementFactory(project).createMethodFromText(text, psiClass);
    PsiMethod addedElement = (PsiMethod)psiClass.add(stepMethod);
    JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedElement);
    CodeStyleManager.getInstance(project).reformat(psiClass);
    addedElement = CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(addedElement);
    return addedElement;
  }

  private static String getParamList(List<String> params) {
    StringBuilder paramListBuilder = new StringBuilder();
    for (int i = 0; i < params.size(); i++) {
      paramListBuilder.append("Object arg").append(i);
      if (i != params.size() - 1) {
        paramListBuilder.append(", ");
      }
    }
    return paramListBuilder.toString();
  }

  private static void templateMethodName(PsiMethod addedStepImpl, TemplateBuilder builder) {
    PsiIdentifier methodName = addedStepImpl.getNameIdentifier();
    builder.replaceElement(Objects.requireNonNull(methodName), methodName.getText());
  }

  private static void templateParams(PsiMethod addedElement, TemplateBuilder builder) {
    PsiParameterList paramsList = addedElement.getParameterList();
    PsiParameter[] parameters = paramsList.getParameters();
    for (PsiParameter parameter : parameters) {
      PsiElement nameIdentifier = parameter.getNameIdentifier();
      PsiTypeElement typeElement = parameter.getTypeElement();
      if (nameIdentifier != null) {
        builder.replaceElement(Objects.requireNonNull(typeElement), typeElement.getText());
        builder.replaceElement(nameIdentifier, nameIdentifier.getText());
      }
    }
  }

  private static void templateBody(PsiMethod addedElement, TemplateBuilder builder) {
    final PsiCodeBlock body = addedElement.getBody();
    if (body != null) {
      builder.replaceElement(body, new TextRange(2, 2), "");
    }
  }

  private static void userTemplateModify(TemplateBuilder builder, Project project, VirtualFile file) {
    Editor editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, file, 0), true);
    final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    if (editor != null) {
      documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());
      builder.run(editor, false);
    }
  }

  @NotNull
  private static String getMethodName(PsiClass psiClass) {
    try {
      for (int i = 1, length = psiClass.getAllMethods().length; i < length; i++) {
        String methodName = IMPLEMENTATION + i;
        if (psiClass.findMethodsByName(methodName, true).length == 0) {
          return methodName;
        }
      }
    }
    catch (Exception ex) {
      LOG.debug(ex);
    }
    return IMPLEMENTATION;
  }
}
