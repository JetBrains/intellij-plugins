// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.codeHighlighting.*;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemDescriptorUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.validation.ActionScriptUnusedImportsHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim.Mossienko
 */
final class ActionScriptUnusedImportsPassFactory implements TextEditorHighlightingPassFactory, TextEditorHighlightingPassFactoryRegistrar {
  @Override
  public void registerHighlightingPassFactory(@NotNull TextEditorHighlightingPassRegistrar registrar, @NotNull Project project) {
    registrar.registerTextEditorHighlightingPass(this, new int[]{Pass.UPDATE_ALL}, null, true, -1);
  }

  @Override
  public TextEditorHighlightingPass createHighlightingPass(@NotNull final PsiFile file, @NotNull final Editor editor) {
    if (file instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile(file) ||
        file instanceof JSFile && !(file instanceof PsiCompiledElement) && file.getLanguage().is(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
       ) {
      final HighlightDisplayKey key = HighlightDisplayKey.find(JSUnusedLocalSymbolsInspection.SHORT_NAME);
      if (InspectionProjectProfileManager.getInstance(file.getProject()).getCurrentProfile().isToolEnabled(key, file)) {
        return new ActionScriptUnusedImportsHighlightingPass(file, editor);
      }
    }
    return null;
  }

  private static IntentionAction createOptimizeImportsIntention() {
    return new IntentionAction() {

      @Override
      @NotNull
      public String getText() {
        return JavaScriptBundle.message("javascript.fix.optimize.imports");
      }

      @Override
      @NotNull
      public String getFamilyName() {
        return getText();
      }

      @Override
      public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
      }

      @Override
      public void invoke(@NotNull final Project project, Editor editor, PsiFile file) {
        ImportOptimizer optimizer = new ECMAScriptImportOptimizer();
        final Runnable runnable = optimizer.processFile(file);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            CommandProcessor.getInstance().executeCommand(project, runnable, getFamilyName(), this);
          }
        });
      }

      @Override
      public boolean startInWriteAction() {
        return true;
      }
    };

  }

  @NotNull
  public String getComponentName() {
    return "ActionScript.UnusedImportReporter";
  }

  public static class ActionScriptUnusedImportsHighlightingPass extends TextEditorHighlightingPass {
    private Collection<JSImportStatement> importStatements;
    private Collection<JSReferenceExpression> fqnsToReplaceWithShortName;
    private final PsiFile myFile;

    public ActionScriptUnusedImportsHighlightingPass(final PsiFile file, final Editor editor) {
      super(file.getProject(), editor.getDocument(), true);
      myFile = file;
    }

    @Override
    public void doCollectInformation(@NotNull final ProgressIndicator progress) {
      if (myFile instanceof JSExpressionCodeFragment) {
        importStatements = Collections.emptyList();
        fqnsToReplaceWithShortName = Collections.emptyList();
      }
      else {
        final ActionScriptUnusedImportsHelper.Results results = ActionScriptUnusedImportsHelper.getUnusedImports(myFile);
        importStatements = results.unusedImports;
        fqnsToReplaceWithShortName = results.fqnsToReplaceWithShortName;
      }
    }

    @Override
    public void doApplyInformationToEditor() {
      UpdateHighlightersUtil.setHighlightersToEditor(myProject, myDocument, 0, myFile.getTextLength(), getHighlights(), getColorsScheme(), getId());
    }

    private List<HighlightInfo> getHighlights() {
      final List<HighlightInfo> infos = new ArrayList<>(importStatements.size() + fqnsToReplaceWithShortName.size());
      IntentionAction action = createOptimizeImportsIntention();

      createHighlights(importStatements, action, JavaScriptBundle.message("javascript.validation.unused.import"), infos,
                       ProblemHighlightType.LIKE_UNUSED_SYMBOL);
      createHighlights(fqnsToReplaceWithShortName, action, JavaScriptBundle.message("javascript.validation.fqn.to.replace.with.import"), infos,
                       ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      return infos;
    }

    private static void createHighlights(Collection<? extends JSElement> elements,
                                         IntentionAction action,
                                         String message,
                                         List<? super HighlightInfo> result,
                                         @NotNull ProblemHighlightType type) {
      for (JSElement unusedImport : elements) {
        TextRange range = InjectedLanguageManager.getInstance(unusedImport.getProject()).injectedToHost(unusedImport, unusedImport.getTextRange());
        if (range.isEmpty()) continue;

        HighlightInfoType highlightInfoType =
          ProblemDescriptorUtil.getHighlightInfoType(type, HighlightSeverity.WARNING, SeverityRegistrar.getSeverityRegistrar(unusedImport.getProject()));
        HighlightInfo info = HighlightInfo.newHighlightInfo(highlightInfoType).range(range).descriptionAndTooltip(message)
          .registerFix(action, null, null, null, null).create();
        ContainerUtil.addIfNotNull(result, info);
      }
    }
  }
}
