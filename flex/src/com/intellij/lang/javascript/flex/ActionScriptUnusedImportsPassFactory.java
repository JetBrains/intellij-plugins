package com.intellij.lang.javascript.flex;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ImportOptimizer;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.inspections.JSUnusedLocalSymbolsInspection;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpressionCodeFragment;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.validation.JSUnusedImportsHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 29, 2008
 *         Time: 10:23:03 PM
 */
public class ActionScriptUnusedImportsPassFactory extends AbstractProjectComponent implements TextEditorHighlightingPassFactory {
  public ActionScriptUnusedImportsPassFactory(Project project, TextEditorHighlightingPassRegistrar registrar) {
    super(project);
    registrar.registerTextEditorHighlightingPass(
        this,
        new int[]{Pass.UPDATE_ALL},
        null,
        true,
        -1
    );
  }

  public TextEditorHighlightingPass createHighlightingPass(@NotNull final PsiFile file, @NotNull final Editor editor) {
    if (file instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile(file) ||
        file instanceof JSFile && !(file instanceof PsiCompiledElement) && file.getLanguage().is(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
       ) {
      final HighlightDisplayKey key = HighlightDisplayKey.find(JSUnusedLocalSymbolsInspection.SHORT_NAME);
      if (InspectionProjectProfileManager.getInstance(file.getProject()).getInspectionProfile().isToolEnabled(key, file)) {
        return new ActionScriptUnusedImportsHighlightingPass(file, editor);
      }
    }
    return null;
  }

  private static IntentionAction createOptimizeImportsIntention() {
    return new IntentionAction() {

      @NotNull
      public String getText() {
        return JSBundle.message("javascript.fix.optimize.imports");
      }

      @NotNull
      public String getFamilyName() {
        return getText();
      }

      public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
      }

      public void invoke(@NotNull final Project project, Editor editor, PsiFile file) {
        ImportOptimizer optimizer = new ECMAScriptImportOptimizer();
        final Runnable runnable = optimizer.processFile(file);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            CommandProcessor.getInstance().executeCommand(project, runnable, getFamilyName(), this);
          }
        });
      }

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

    public void doCollectInformation(@NotNull final ProgressIndicator progress) {
      if (myFile instanceof JSExpressionCodeFragment) {
        importStatements = Collections.emptyList();
        fqnsToReplaceWithShortName = Collections.emptyList();
      }
      else {
        final JSUnusedImportsHelper.Results results = JSUnusedImportsHelper.getUnusedImports(myFile);
        importStatements = results.unusedImports;
        fqnsToReplaceWithShortName = results.fqnsToReplaceWithShortName;
      }
    }

    public void doApplyInformationToEditor() {
      UpdateHighlightersUtil.setHighlightersToEditor(myProject, myDocument, 0, myFile.getTextLength(), getHighlights(), getColorsScheme(), getId());
    }

    private List<HighlightInfo> getHighlights() {
      final AnnotationHolder annotationHolder = new AnnotationHolderImpl(new AnnotationSession(myFile));
      final List<HighlightInfo> infos = new ArrayList<HighlightInfo>(importStatements.size() + fqnsToReplaceWithShortName.size());
      IntentionAction action = createOptimizeImportsIntention();

      createHighlights(importStatements, annotationHolder, action, JSBundle.message("javascript.validation.unused.import"), infos,
                       ProblemHighlightType.LIKE_UNUSED_SYMBOL);
      createHighlights(fqnsToReplaceWithShortName, annotationHolder, action, JSBundle.message("javascript.validation.fqn.to.replace.with.import"), infos,
                       ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      return infos;
    }

    private static void createHighlights(Collection<? extends JSElement> elements, AnnotationHolder annotationHolder, IntentionAction action, String message, List<HighlightInfo> result,
                                  ProblemHighlightType type) {
      for (JSElement unusedImport : elements) {
        TextRange range = InjectedLanguageManager.getInstance(unusedImport.getProject()).injectedToHost(unusedImport, unusedImport.getTextRange());
        if (range.isEmpty()) continue;

        final Annotation annotation = annotationHolder.createWarningAnnotation(range, message);
        annotation.setHighlightType(type);
        annotation.registerFix(action);
        result.add(HighlightInfo.fromAnnotation(annotation));
      }
    }
  }
}
