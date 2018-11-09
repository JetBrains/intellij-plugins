// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.psi.Angular2HtmlReferenceVariable;
import org.angularjs.codeInsight.refs.AngularJSReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Angular2ViewChildReferencesProvider extends PsiReferenceProvider {

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new Angular2ViewChildReference((JSLiteralExpression)element)};
  }


  public static class Angular2ViewChildReference extends AngularJSReferenceBase<JSLiteralExpression> {

    public Angular2ViewChildReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
      Ref<PsiElement> result = new Ref<>();
      final HtmlFileImpl template = getTemplate();
      if (template != null) {
        final String refName = myElement.getStringValue();
        if (refName != null) {
          Angular2Processor.process(template, (el) -> {
            if (el instanceof Angular2HtmlReferenceVariable
                && refName.equals(el.getName())) {
              result.set(el);
            }
          });
        }
      }
      return result.get();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      final HtmlFileImpl template = getTemplate();
      if (template != null) {
        final List<JSVariable> result = new ArrayList<>();
        Angular2Processor.process(template, (el) ->
          ObjectUtils.consumeIfCast(el, Angular2HtmlReferenceVariable.class, result::add));
        return result.toArray();
      }
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    private HtmlFileImpl getTemplate() {
      final TypeScriptClass cls = PsiTreeUtil.getContextOfType(getElement(), TypeScriptClass.class);
      if (cls != null) {
        Angular2Component component = Angular2EntitiesProvider.getComponent(cls);
        if (component != null) {
          return component.getHtmlTemplate();
        }
      }
      return null;
    }
  }
}
