// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.javascript.intentions.JSPublicModifierIntention;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;
import org.angular2.inspections.AngularInaccessibleComponentMemberInAotModeInspection;
import org.angular2.lang.expr.Angular2Language;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.notNull;

public class AngularMakePublicQuickFix extends JSPublicModifierIntention implements LocalQuickFix {

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getName() {
    return getText();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    return isAngularTemplateElement(element)
           ? AngularInaccessibleComponentMemberInAotModeInspection.accept(locateMemberToEdit(element))
           : super.isAvailable(project, editor, element);
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    if (!isAngularTemplateElement(element)) {
      super.invoke(project, editor, element);
      return;
    }

    PsiElement member = locateMemberToEdit(element);
    if (!AngularInaccessibleComponentMemberInAotModeInspection.accept(member)) {
      return;
    }
    if (editor != null) {
      PsiNavigationSupport.getInstance().createNavigatable(
        project, member.getContainingFile().getVirtualFile(), member.getTextOffset()
      ).navigate(true);
    }

    super.invoke(project, editor,
                 member instanceof PsiNameIdentifierOwner
                 ? notNull(((PsiNameIdentifierOwner)member).getNameIdentifier(), member)
                 : member
    );
  }

  private @Nullable PsiElement locateMemberToEdit(@NotNull PsiElement element) {
    element = element instanceof PsiWhiteSpace
              ? element.getPrevSibling()
              : element.getParent();

    if (element != null && !(element instanceof JSReferenceExpression)) {
      element = element.getPrevSibling();
    }
    if (element instanceof JSReferenceExpression) {
      element = ((JSReferenceExpression)element).resolve();
    }
    if (element instanceof PsiNameIdentifierOwner) {
      element = ((PsiNameIdentifierOwner)element).getNameIdentifier();
    }
    if (element == null) {
      return null;
    }
    PsiElement member = getField(element);
    if (member == null) {
      member = element instanceof JSFunction ? element : getFunction(element);
    }
    return member;
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    invoke(project, null, descriptor.getPsiElement());
  }

  @Override
  public @NotNull Priority getPriority() {
    return Priority.HIGH;
  }

  private static boolean isAngularTemplateElement(final @NotNull PsiElement element) {
    Language language = element.getContainingFile().getLanguage();
    return language instanceof HTMLLanguage || language instanceof Angular2Language;
  }
}
