// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.presentable.JSNamedElementPresenter;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.AstLoadingFilter;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.inspections.quickfixes.AngularMakePublicQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2ElementVisitor;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static com.intellij.lang.javascript.refactoring.JSVisibilityUtil.getPresentableAccessModifier;
import static com.intellij.openapi.util.text.StringUtil.capitalize;
import static com.intellij.util.ObjectUtils.notNull;

public class AngularInaccessibleComponentMemberInAotModeInspection extends LocalInspectionTool {

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                 boolean isOnTheFly,
                                                 @NotNull LocalInspectionToolSession session) {
    Language fileLang = holder.getFile().getLanguage();
    if (fileLang.isKindOf(Angular2HtmlLanguage.INSTANCE)
        || Angular2Language.INSTANCE.is(fileLang)) {
      return new Angular2ElementVisitor() {
        @Override
        public void visitJSReferenceExpression(JSReferenceExpression node) {
          if (node.getQualifier() == null
              || node.getQualifier() instanceof JSThisExpression) {
            PsiElement resolved = node.resolve();
            TypeScriptClass clazz = PsiTreeUtil.getContextOfType(resolved, TypeScriptClass.class);
            if (clazz != null && resolved instanceof JSElement && accept(resolved)) {
              //noinspection HardCodedStringLiteral
              holder.registerProblem(
                notNull(node.getReferenceNameElement(), node),
                capitalize(Angular2Bundle.message("angular.inspection.aot-inaccessible-member.message.template-symbol",
                                                  getAccessModifier((JSElement)resolved), getKind(resolved), getName(resolved))),

                new AngularMakePublicQuickFix());
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
            //noinspection HardCodedStringLiteral
            holder.registerProblem(
              notNull(member instanceof PsiNameIdentifierOwner
                      ? ((PsiNameIdentifierOwner)member).getNameIdentifier()
                      : null, member),
              capitalize(Angular2Bundle.message("angular.inspection.aot-inaccessible-member.message.member",
                                                getAccessModifier(member), getKind(member), getName(member))),
              new AngularMakePublicQuickFix());
          }
        }
      };
    }
    return PsiElementVisitor.EMPTY_VISITOR;
  }

  public static boolean accept(@Nullable PsiElement member) {
    if (member instanceof JSAttributeListOwner
        && !(member instanceof JSFunction && ((JSFunction)member).isConstructor())) {
      JSAttributeList attributes = ((JSAttributeListOwner)member).getAttributeList();
      if (attributes == null) return false;
      JSAttributeList.AccessType accessType = attributes.getAccessType();
      return !attributes.hasModifier(JSAttributeList.ModifierType.STATIC) &&
             accessType == JSAttributeList.AccessType.PRIVATE;
    }
    return false;
  }

  private static String getKind(@NotNull PsiElement member) {
    return new JSNamedElementPresenter(member).describeElementKind();
  }

  private static @NotNull String getAccessModifier(@NotNull JSElement member) {
    return Optional.ofNullable(getPresentableAccessModifier(member))
      .map(JSVisibilityUtil.PresentableAccessModifier::getText)
      .orElse("");
  }

  private static @NotNull String getName(@NotNull PsiElement member) {
    return notNull(member instanceof PsiNamedElement ? ((PsiNamedElement)member).getName() : null,
                   JSFormatUtil.getAnonymousElementPresentation());
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
}
