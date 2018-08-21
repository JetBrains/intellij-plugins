package org.angularjs.codeInsight.refs;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.angular2.index.Angular2IndexingHandler.isDirective;

public class Angular2ViewChildReferencesProvider extends PsiReferenceProvider {

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new Angular2ViewChildReference((JSLiteralExpression)element)};
  }

  @Nullable
  public static PsiElement findAngularTagReference(@NotNull XmlFile html, @NotNull String ref) {
    Ref<PsiElement> result = new Ref<>();
    String attrName = "#" + ref;
    html.accept(new XmlRecursiveElementWalkingVisitor() {
      @Override
      public void visitXmlAttribute(XmlAttribute attribute) {
        if (attribute.getName().equals(attrName)) {
          result.set(attribute.getNameElement());
          stopWalking();
        }
      }
    });
    return result.get();
  }

  @Nullable
  public static HtmlFileImpl findAngularComponentTemplate(@NotNull TypeScriptClass cls) {
    if (cls.getAttributeList() == null) {
      return null;
    }
    JSAttributeList list = cls.getAttributeList();
    for (ES6Decorator decorator : PsiTreeUtil.getChildrenOfTypeAsList(list, ES6Decorator.class)) {
      JSCallExpression call = ObjectUtils.tryCast(decorator.getExpression(), JSCallExpression.class);
      if (call != null
          && call.getMethodExpression() instanceof JSReferenceExpression
          && isDirective(((JSReferenceExpression)call.getMethodExpression()).getReferenceName())
          && call.getArguments().length == 1
          && call.getArguments()[0] instanceof JSObjectLiteralExpression) {

        JSObjectLiteralExpression props = (JSObjectLiteralExpression)call.getArguments()[0];
        for (JSProperty property : props.getProperties()) {
          if ("templateUrl".equals(property.getName())
              && property.getValue() != null) {
            for (PsiReference ref : property.getValue().getReferences()) {
              PsiElement el = ref.resolve();
              if (el instanceof HtmlFileImpl) {
                return (HtmlFileImpl)el;
              }
            }
            break;
          }
          if ("template".equals(property.getName())
              && property.getValue() != null) {
            List<Pair<PsiElement, TextRange>> injections =
              InjectedLanguageManager.getInstance(cls.getProject()).getInjectedPsiFiles(property.getValue());
            if (injections != null) {
              for (Pair<PsiElement, TextRange> injection : injections) {
                if (injection.getFirst() instanceof HtmlFileImpl) {
                  return (HtmlFileImpl)injection.getFirst();
                }
              }
            }
            break;
          }
        }
        break;
      }
    }
    return null;
  }

  public static class Angular2ViewChildReference extends AngularJSReferenceBase<JSLiteralExpression> {

    public Angular2ViewChildReference(@NotNull JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
      final TypeScriptClass cls = PsiTreeUtil.getParentOfType(getElement(), TypeScriptClass.class);
      if (cls != null) {
        final HtmlFileImpl template = findAngularComponentTemplate(cls);
        final String refName = myElement.getStringValue();
        if (template != null && refName != null) {
          return findAngularTagReference(template, refName);
        }
      }
      return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      final TypeScriptClass cls = PsiTreeUtil.getParentOfType(getElement(), TypeScriptClass.class);
      if (cls != null) {
        final List<String> result = new ArrayList<>();
        final HtmlFileImpl template = findAngularComponentTemplate(cls);
        if (template != null) {
          template.accept(new XmlRecursiveElementWalkingVisitor() {
            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
              final String name = attribute.getName();
              if (name.startsWith("#")) {
                result.add(name.substring(1));
              }
            }
          });
        }
        return result.toArray();
      }
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
