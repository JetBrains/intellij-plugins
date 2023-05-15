// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.javascript.flex.refactoring.moveMembers;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ActionScriptMoveMembersHandler extends MoveHandlerDelegate {
  @Override
  public boolean tryToMove(PsiElement element, Project project, DataContext dataContext, @Nullable PsiReference reference, Editor editor) {
    if (JSClassUtils.isStaticMethodOrField(element)) {
      if (!JSRefactoringUtil.checkReadOnlyStatus(element, editor, getRefactoringName())) return true;
      doMove(project, new PsiElement[]{element}, null, null);
      return true;
    }
    return false;
  }

  @Override
  public void doMove(Project project, PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable MoveCallback callback) {
    if (elements.length == 0) {
      return;
    }

    final JSClass sourceClass = JSUtils.getMemberContainingClass(elements[0]);
    if (sourceClass == null) {
      return;
    }

    final Set<JSElement> preselectMembers = new HashSet<>();
    for (PsiElement element : elements) {
      if (element instanceof JSFunction && !sourceClass.equals(JSUtils.getMemberContainingClass(element))) {
        String message =
          RefactoringBundle.getCannotRefactorMessage(RefactoringBundle.message("members.to.be.moved.should.belong.to.the.same.class"));
        CommonRefactoringUtil.showErrorMessage(getRefactoringName(), message, null, project);
        return;
      }

      if (element instanceof JSVariable field) {
        if (field.getAttributeList() == null || !field.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC)) {
          String fieldName = field.getName();
          String message = RefactoringBundle.message("field.0.is.not.static", fieldName, getRefactoringName());
          CommonRefactoringUtil.showErrorMessage(getRefactoringName(), message, null, project);
          return;
        }
        preselectMembers.add(field);
      }
      else if (element instanceof JSFunction method) {
        String methodName = method.getName();
        if (method.isConstructor()) {
          String message = RefactoringBundle.message("0.refactoring.cannot.be.applied.to.constructors", getRefactoringName());
          CommonRefactoringUtil.showErrorMessage(getRefactoringName(), message, null, project);
          return;
        }
        if (method.getAttributeList() == null || !method.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC)) {
          String message = RefactoringBundle.message("method.0.is.not.static", methodName, getRefactoringName());
          CommonRefactoringUtil.showErrorMessage(getRefactoringName(), message, null, project);
          return;
        }
        preselectMembers.add(method);
      }
    }

    if (!JSRefactoringUtil.checkReadOnlyStatus(sourceClass, null, getRefactoringName())) return;

    final JSClass initialTargetClass = targetContainer instanceof JSClass ? (JSClass)targetContainer : null;
    ActionScriptMoveMembersDialog
      dialog = new ActionScriptMoveMembersDialog(project, sourceClass, initialTargetClass, preselectMembers, callback);
    dialog.show();
  }

  @Override
  public boolean isValidTarget(PsiElement psiElement, PsiElement[] sources) {
    return psiElement instanceof JSClass; // TODO check classes from swc
  }

  @Override
  public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable PsiReference reference) {
    for (PsiElement element : elements) {
      if (!JSClassUtils.isStaticMethodOrField(element)) return false;
    }
    return targetContainer == null || super.canMove(elements, targetContainer, reference);
  }

  @Override
  public boolean supportsLanguage(@NotNull Language language) {
    return language.isKindOf(JavascriptLanguage.INSTANCE);
  }

  public static String getRefactoringName() {
    return JavaScriptBundle.message("move.members.refactoring.name");
  }
}
