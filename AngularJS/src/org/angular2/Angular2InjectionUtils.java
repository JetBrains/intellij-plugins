// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.injected.editor.EditorWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2InjectionUtils {

  public static PsiFile getFirstInjectedFile(@Nullable PsiElement element) {
    if (element != null) {
      List<Pair<PsiElement, TextRange>> injections =
        InjectedLanguageManager.getInstance(element.getProject()).getInjectedPsiFiles(element);
      if (injections != null) {
        for (Pair<PsiElement, TextRange> injection : injections) {
          if (injection.getFirst() instanceof PsiFile) {
            return (PsiFile)injection.getFirst();
          }
        }
      }
    }
    return null;
  }

  public static @Nullable PsiElement getElementAtCaretFromContext(@NotNull DataContext context) {
    Editor editor = context.getData(CommonDataKeys.EDITOR);
    PsiFile file = context.getData(CommonDataKeys.PSI_FILE);
    Project project = context.getData(CommonDataKeys.PROJECT);
    if (editor == null || project == null
        || file == null || DumbService.isDumb(project)) {
      return null;
    }
    int caretOffset = editor.getCaretModel().getOffset();
    if (!(editor instanceof EditorWindow)) {
      PsiElement injected = InjectedLanguageManager.getInstance(project)
        .findInjectedElementAt(file, caretOffset);
      if (injected != null && injected.isValid()) {
        return injected;
      }
    }
    return file.findElementAt(caretOffset);
  }

  public static @Nullable <T extends Angular2EmbeddedExpression> T findInjectedAngularExpression(@NotNull XmlAttribute attribute,
                                                                                                 @NotNull Class<T> expressionClass) {
    XmlAttributeValue value = attribute.getValueElement();
    if (value != null && value.getTextLength() >= 2) {
      PsiElement injection = InjectedLanguageManager.getInstance(attribute.getProject()).findInjectedElementAt(
        value.getContainingFile(), value.getTextOffset() + 1);
      return PsiTreeUtil.getParentOfType(injection, expressionClass);
    }
    return null;
  }
}
