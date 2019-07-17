// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ObjectUtils;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2InjectionUtils {

  public static PsiFile getFirstInjectedFile(@Nullable PsiElement element) {
    return element == null ? null : tryRunWithDocumentCommitted(element.getProject(), () -> {
      List<Pair<PsiElement, TextRange>> injections =
        InjectedLanguageManager.getInstance(element.getProject()).getInjectedPsiFiles(element);
      if (injections != null) {
        for (Pair<PsiElement, TextRange> injection : injections) {
          if (injection.getFirst() instanceof PsiFile) {
            return (PsiFile)injection.getFirst();
          }
        }
      }
      return null;
    });
  }

  @Nullable
  public static PsiElement getTargetElementFromContext(@NotNull DataContext context) {
    Project project = context.getData(CommonDataKeys.PROJECT);
    //noinspection deprecation
    return project == null ? null : tryRunWithDocumentCommitted(project, () ->
      doIfNotNull(
        InjectedLanguageUtil.getEditorForInjectedLanguageNoCommit(context.getData(CommonDataKeys.EDITOR),
                                                                  context.getData(CommonDataKeys.PSI_FILE)),
        editor -> ObjectUtils.tryCast(FileEditorManagerEx.getInstanceEx(project)
                                        .getData(CommonDataKeys.PSI_ELEMENT.getName(), editor, editor.getCaretModel().getCurrentCaret()),
                                      PsiElement.class))
    );
  }

  @Nullable
  public static <T extends Angular2EmbeddedExpression> T findInjectedAngularExpression(@NotNull XmlAttribute attribute,
                                                                                       @NotNull Class<T> expressionClass) {
    XmlAttributeValue value = attribute.getValueElement();
    if (value != null && value.getTextLength() >= 2) {
      return tryRunWithDocumentCommitted(attribute.getProject(), () ->
        PsiTreeUtil.getParentOfType(
          InjectedLanguageManager.getInstance(attribute.getProject()).findInjectedElementAt(
            value.getContainingFile(), value.getTextOffset() + 1),
          expressionClass)
      );
    }
    return null;
  }

  private static <T> T tryRunWithDocumentCommitted(@NotNull Project project, @NotNull Computable<T> action) {
    if (ApplicationManager.getApplication().isReadAccessAllowed()
        && !SwingUtilities.isEventDispatchThread()) {
      // Cannot upgrade read access to write access outside of EDT, but we should be OK in this case
      return action.compute();
    }
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    return documentManager.commitAndRunReadAction(action);
  }
}
