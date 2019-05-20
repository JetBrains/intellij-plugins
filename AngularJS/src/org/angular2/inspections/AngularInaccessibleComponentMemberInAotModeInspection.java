// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.*;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.documentation.JSDocumentationUtils;
import com.intellij.lang.javascript.intentions.TypeScriptPublicModifierIntention;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.jsdoc.JSDocComment;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.hash.HashSet;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static com.intellij.lang.javascript.presentable.JSFormatUtil.ANONYMOUS_ELEMENT_PRESENTATION;
import static com.intellij.lang.javascript.refactoring.JSVisibilityUtil.getPresentableAccessModifier;
import static com.intellij.openapi.util.text.StringUtil.capitalize;
import static com.intellij.util.ObjectUtils.notNull;

public class AngularInaccessibleComponentMemberInAotModeInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                        boolean isOnTheFly,
                                        @NotNull LocalInspectionToolSession session) {
    Language fileLang = holder.getFile().getLanguage();
    if (Angular2HtmlLanguage.INSTANCE.is(fileLang)
        || Angular2Language.INSTANCE.is(fileLang)) {
      return new Angular2ElementVisitor() {
        @Override
        public void visitJSReferenceExpression(JSReferenceExpression node) {
          if (node.getQualifier() == null
              || node.getQualifier() instanceof JSThisExpression) {
            PsiElement resolved = node.resolve();
            TypeScriptClass clazz = PsiTreeUtil.getContextOfType(resolved, TypeScriptClass.class);
            if (clazz != null && resolved instanceof JSElement && accept(resolved)) {
              holder.registerProblem(
                notNull(node.getReferenceNameElement(), node),
                capitalize(Angular2Bundle.message("angular.inspection.template.aot.inaccessible.symbol",
                                                  getAccessModifier((JSElement)resolved), getKind(resolved), getName(resolved))),
                new AngularMakeAccessibleQuickFix());
            }
          }
        }
      };
    }
    else if (DialectDetector.isTypeScript(holder.getFile())
             && Angular2LangUtil.isAngular2Context(holder.getFile())) {
      return new JSElementVisitor() {
        @Override
        public void visitTypeScriptClass(TypeScriptClass typeScriptClass) {
          Angular2Component component = Angular2EntitiesProvider.getComponent(typeScriptClass);
          PsiFile template;
          if (component == null || (template = component.getTemplateFile()) == null) return;
          Set<JSElement> candidates = new HashSet<>();
          for (JSElement member : typeScriptClass.getMembers()) {
            if (accept(member)) {
              candidates.add(member);
            }
          }
          retainReferenced(template, candidates);
          for (JSElement member : candidates) {
            holder.registerProblem(
              notNull(member instanceof PsiNameIdentifierOwner ? ((PsiNameIdentifierOwner)member).getNameIdentifier()
                                                               : null, member),
              capitalize(Angular2Bundle.message("angular.inspection.component.aot.inaccessible.member",
                                                getAccessModifier(member), getKind(member), getName(member))),
              new AngularMakeAccessibleQuickFix());
          }
        }
      };
    }
    return PsiElementVisitor.EMPTY_VISITOR;
  }

  private static boolean accept(@Nullable PsiElement member) {
    if (member instanceof JSAttributeListOwner
        && !(member instanceof JSFunction && ((JSFunction)member).isConstructor())) {
      JSAttributeList attributes = ((JSAttributeListOwner)member).getAttributeList();
      if (attributes == null) return false;
      JSAttributeList.AccessType accessType = attributes.getAccessType();
      return !attributes.hasModifier(JSAttributeList.ModifierType.STATIC)
             && (accessType == JSAttributeList.AccessType.PRIVATE
                 || accessType == JSAttributeList.AccessType.PROTECTED);
    }
    return false;
  }

  private static String getKind(@NotNull PsiElement member) {
    return new JSNamedElementPresenter(member).describeElementKind();
  }

  @NotNull
  private static String getAccessModifier(@NotNull JSElement member) {
    return Optional.ofNullable(getPresentableAccessModifier(member))
      .map(JSVisibilityUtil.PresentableAccessModifier::getText)
      .orElse("");
  }

  @NotNull
  private static String getName(@NotNull PsiElement member) {
    return notNull(member instanceof PsiNamedElement ? ((PsiNamedElement)member).getName() : null,
                   ANONYMOUS_ELEMENT_PRESENTATION);
  }

  private static void retainReferenced(@NotNull PsiFile template, @NotNull Set<? extends PsiElement> candidates) {
    LocalSearchScope fileScope = new LocalSearchScope(template);
    Iterator<? extends PsiElement> iterator = candidates.iterator();
    AstLoadingFilter.forceAllowTreeLoading(
      template, () -> {
        while (iterator.hasNext()) {
          if (ReferencesSearch.search(iterator.next(), fileScope, true).findFirst() == null) {
            iterator.remove();
          }
        }
      }
    );
  }

  private static class AngularMakeAccessibleQuickFix extends TypeScriptPublicModifierIntention implements LocalQuickFix {

    @NonNls private static final String INTERNAL_TAG = "internal";
    @NonNls private static final String INTERNAL_COMMENT = "/** @" + INTERNAL_TAG + " */";

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
      return Angular2Bundle.message("angular.quickfix.component.aot.make.accessible");
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @NotNull
    @Override
    public String getText() {
      return getName();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
      return accept(locateMemberToEdit(element));
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
      PsiElement member = locateMemberToEdit(element);
      if (!accept(member)) {
        return;
      }
      if (editor != null) {
        PsiNavigationSupport.getInstance().createNavigatable(
          project, member.getContainingFile().getVirtualFile(),
          member.getTextRange().getStartOffset() + 1
        ).navigate(true);
      }
      SmartPsiElementPointer<PsiElement> memberPointer = SmartPointerManager.createPointer(member);
      super.invoke(project, editor, member instanceof TypeScriptFunction
                                    || member instanceof JSParameter ? member.getFirstChild()
                                                                     : member);
      Optional.ofNullable(memberPointer.getElement())
        .filter(m -> !(m instanceof JSParameter))
        .ifPresent(m -> WriteAction.run(() -> {
          PsiComment comment = JSDocumentationUtils.findDocComment(member);
          if (comment instanceof JSDocComment) {
            JSDocumentationUtils.createOrUpdateTagsInDocComment(member, Collections.singletonList(INTERNAL_TAG), null, null);
          }
          else {
            comment = JSChangeUtil.createCommentFromText(INTERNAL_COMMENT, m);
            PsiElement added = JSChangeUtil.doDoAddBeforePure(m.getParent(), comment, m);
            JSChangeUtil.addWs(m.getParent().getNode(), m.getNode(), "\n");
            FormatFixer.create(added, FormatFixer.Mode.Reformat).fixFormat();
          }
        }));
    }

    @Nullable
    private PsiElement locateMemberToEdit(@NotNull PsiElement element) {
      if (element.getParent() instanceof JSReferenceExpression) {
        element = element.getParent();
      }
      if (element instanceof JSReferenceExpression) {
        element = ((JSReferenceExpression)element).resolve();
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
  }
}
