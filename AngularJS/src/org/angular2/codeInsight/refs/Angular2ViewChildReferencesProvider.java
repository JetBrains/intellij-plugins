// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angularjs.codeInsight.refs.AngularJSReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class Angular2ViewChildReferencesProvider extends PsiReferenceProvider {

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new Angular2ViewChildReference((JSLiteralExpression)element)};
  }


  public static class Angular2ViewChildReference extends AngularJSReferenceBase<JSLiteralExpression> {

    public Angular2ViewChildReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Override
    public boolean isSoft() {
      return false;
    }

    @Override
    public @Nullable PsiElement resolveInner() {
      Ref<PsiElement> result = new Ref<>();
      final String refName = myElement.getStringValue();
      if (refName != null) {
        processVariables((name, psi) -> {
          if (refName.equals(name)) {
            result.set(psi);
            return false;
          }
          return true;
        });
      }
      return result.get();
    }

    @Override
    public Object @NotNull [] getVariants() {
      final List<PsiElement> result = new ArrayList<>();
      final Set<String> names = new HashSet<>();
      processVariables((name, psi) -> {
        if (names.add(name)) result.add(psi);
        return true;
      });
      return result.toArray();
    }

    private void processVariables(BiPredicate<? super String, ? super PsiElement> processor) {
      final PsiFile template = getTemplate();
      if (template != null) {
        if (template.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE)) {
          template.accept(new Angular2HtmlRecursiveElementWalkingVisitor() {
            @Override
            public void visitReference(Angular2HtmlReference reference) {
              JSVariable refVar = reference.getVariable();
              if (refVar != null && !processor.test(refVar.getName(), refVar)) {
                stopWalking();
              }
            }
          });
        }
        else {
          template.accept(new XmlRecursiveElementWalkingVisitor() {
            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
              AttributeInfo info = Angular2AttributeNameParser.parse(attribute.getName(), attribute.getParent());
              if (info.type == Angular2AttributeType.REFERENCE) {
                JSLocalImplicitElementImpl refVar = new JSLocalImplicitElementImpl(info.name, "*", attribute);
                if (!processor.test(info.name, refVar)) {
                  stopWalking();
                }
              }
            }
          });
        }
      }
    }

    private @Nullable PsiFile getTemplate() {
      final TypeScriptClass cls = PsiTreeUtil.getContextOfType(getElement(), TypeScriptClass.class);
      if (cls != null) {
        Angular2Component component = Angular2EntitiesProvider.getComponent(cls);
        if (component != null) {
          return component.getTemplateFile();
        }
      }
      return null;
    }
  }
}
