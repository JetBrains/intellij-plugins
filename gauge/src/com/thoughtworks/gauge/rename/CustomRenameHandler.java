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

package com.thoughtworks.gauge.rename;

import com.intellij.execution.ExecutionBundle;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.refactoring.rename.RenameHandler;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.thoughtworks.gauge.util.StepUtil.*;

final class CustomRenameHandler implements RenameHandler {
  @Override
  public boolean isAvailableOnDataContext(@NotNull DataContext dataContext) {
    PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
    VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);

    if (file == null || !GaugeUtil.isGaugeFile(file)) return false;

    if (element == null) {
      if (editor == null) return false;
      int offset = editor.getCaretModel().getOffset();
      if (offset > 0 && offset == editor.getDocument().getTextLength()) offset--;

      PsiFile data = CommonDataKeys.PSI_FILE.getData(dataContext);
      if (data == null) return false;
      PsiElement psiElement = getStepElement(data.findElementAt(offset));
      return isConcept(psiElement) || isStep(psiElement);
    }
    return CommonDataKeys.PROJECT.getData(dataContext) != null
           && (isMethod(element) || isConcept(element) || isStep(element));
  }

  @Override
  public boolean isRenaming(@NotNull DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    if (editor == null) return;

    int offset = editor.getCaretModel().getOffset();
    if (offset > 0 && offset == editor.getDocument().getTextLength()) offset--;

    PsiElement psiStepElement = getStepElement(file.findElementAt(offset));

    PsiElement element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
    if (element == null) element = psiStepElement;

    String text = element.toString();

    //Finding text from annotation
    if (isMethod(element)) {
      List<String> values = getGaugeStepAnnotationValues((PsiMethod)element);
      if (values.isEmpty()) {
        return;
      }
      else if (values.size() == 1) {
        text = values.get(0);
      }
      else {
        Messages.showWarningDialog(GaugeBundle.message("dialog.message.refactoring.for.steps.with.aliases.unsupported"),
                                   ExecutionBundle.message("warning.common.title"));
        return;
      }
    }
    else if (isStep(element)) {
      text = ((SpecStepImpl)element).getStepValue().getStepAnnotationText();
    }
    else if (isConcept(element)) {
      text = removeIdentifiers(((ConceptStepImpl)element).getStepValue().getStepAnnotationText());
    }
    RefactoringDialog form = new RefactoringDialog(editor.getProject(), file, editor, text);
    form.show();
  }

  private static String removeIdentifiers(String text) {
    text = text.trim();
    return text.charAt(0) == '*' || text.charAt(0) == '#' ? text.substring(1).trim() : text;
  }

  @Override
  public void invoke(@NotNull Project project, PsiElement @NotNull [] elements, DataContext dataContext) {
  }

  private static PsiElement getStepElement(PsiElement selectedElement) {
    if (selectedElement == null) return null;
    if (selectedElement instanceof SpecStepImpl || selectedElement instanceof ConceptStepImpl) {
      return selectedElement;
    }
    if (selectedElement.getParent() == null) return null;
    return getStepElement(selectedElement.getParent());
  }
}
