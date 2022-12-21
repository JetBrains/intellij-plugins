// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptParameterImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.AstLoadingFilter;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static org.angular2.Angular2DecoratorUtil.COMPONENT_DEC;
import static org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC;


public class Angular2ImplicitUsageProvider implements ImplicitUsageProvider {

  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    if (element instanceof TypeScriptFunction || element instanceof TypeScriptField) {
      var name = ((JSAttributeListOwner)element).getName();
      if (name != null && ("ngTemplateContextGuard".equals(name) || name.startsWith(Angular2ControlFlowBuilder.CUSTOM_GUARD_PREFIX))) {
        // ngTemplateGuard_ suffix should actually match the name of directive input, but it does not matter much
        JSClass cls = JSUtils.getMemberContainingClass(element);
        return null != Angular2DecoratorUtil.findDecorator(cls, COMPONENT_DEC, DIRECTIVE_DEC);
      }
    }

    if (element instanceof TypeScriptFunction function) {
      if (function.isSetProperty() || function.isGetProperty()) {
        Ref<JSFunction> theOtherOne = new Ref<>();
        Angular2ReferenceExpressionResolver.findPropertyAccessor(function, function.isGetProperty(), theOtherOne::set);
        if (!theOtherOne.isNull() && Angular2DecoratorUtil.findDecorator(
          theOtherOne.get(), Angular2DecoratorUtil.INPUT_DEC, Angular2DecoratorUtil.OUTPUT_DEC) != null) {
          return true;
        }
      }
    }

    // todo check: TypeScriptFunction case doesn't seem to be needed anymore, and with Ivy the other one might be needless too
    if (element instanceof TypeScriptFunction
        || ((element instanceof TypeScriptField
             || element instanceof TypeScriptParameterImpl)
            && Angular2DecoratorUtil.isPrivateMember((JSPsiElementBase)element))) {
      JSClass cls = JSUtils.getMemberContainingClass(element);
      if (cls instanceof TypeScriptClass && Angular2LangUtil.isAngular2Context(element)) {
        Angular2Component component = Angular2EntitiesProvider.getComponent(cls);
        if (component != null) {
          PsiFile template = component.getTemplateFile();
          if (template != null) {
            return isReferencedInTemplate(element, template);
          }
        }
      }
    }
    return false;
  }

  private static boolean isReferencedInTemplate(@NotNull PsiElement node, @NotNull PsiFile template) {
    Predicate<PsiReference> predicate = reference ->
      reference instanceof PsiElement &&
      !(JSResolveUtil.isSelfReference((PsiElement)reference) ||
        node instanceof JSFunction && PsiTreeUtil.isAncestor(node, (PsiElement)reference, false));

    SearchScope scope = new LocalSearchScope(new PsiElement[]{template},
                                             Angular2Bundle.message("angular.search-scope.template"),
                                             true);
    // TODO stub references in Angular templates
    if (AstLoadingFilter.forceAllowTreeLoading(template, () ->
      ReferencesSearch.search(node, scope, true).anyMatch(predicate))) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return false;
  }
}
