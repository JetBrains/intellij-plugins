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
import org.angular2.lang.html.psi.Angular2HtmlReferenceVariable;
import org.angularjs.codeInsight.refs.AngularJSReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.angular2.Angular2DecoratorUtil.findAngularComponentTemplate;

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
      final TypeScriptClass cls = PsiTreeUtil.getParentOfType(getElement(), TypeScriptClass.class);
      if (cls != null) {
        final HtmlFileImpl template = findAngularComponentTemplate(cls);
        final String refName = myElement.getStringValue();
        if (template != null && refName != null) {
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
      final TypeScriptClass cls = PsiTreeUtil.getParentOfType(getElement(), TypeScriptClass.class);
      if (cls != null) {
        final List<JSVariable> result = new ArrayList<>();
        final HtmlFileImpl template = findAngularComponentTemplate(cls);
        if (template != null) {
          Angular2Processor.process(template, (el) ->
            ObjectUtils.consumeIfCast(el, Angular2HtmlReferenceVariable.class, result::add));
        }
        return result.toArray();
      }
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
