// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JSInjectionController;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.angular2.index.Angular2IndexingHandler;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2DecoratorUtil {

  public static final String COMPONENT_DEC = "Component";

  @Nullable
  public static String getDecoratorName(@Nullable JSCallExpression decorator) {
    return decorator != null && decorator.getMethodExpression() instanceof JSReferenceExpression
           ? ((JSReferenceExpression)decorator.getMethodExpression()).getReferenceName()
           : null;
  }

  public static boolean isLiteralInNgDecorator(PsiElement element, String propertyName, String decoratorName) {
    if (element instanceof JSLiteralExpression) {
      final JSLiteralExpression literal = (JSLiteralExpression)element;
      final PsiElement parent;
      return literal.isQuotedLiteral()
             && (parent = literal.getParent()) instanceof JSProperty
             && propertyName.equals(((JSProperty)parent).getName())
             && decoratorName.equals(getDecoratorName(PsiTreeUtil.getParentOfType(parent, JSCallExpression.class)));
    }
    return false;
  }

  @Nullable
  public static JSCallExpression getDecorator(JSClass cls, String name) {
    if (cls.getAttributeList() == null) {
      return null;
    }
    JSAttributeList list = cls.getAttributeList();
    for (ES6Decorator decorator : PsiTreeUtil.getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      JSCallExpression call = ObjectUtils.tryCast(decorator.getExpression(), JSCallExpression.class);
      if (call != null
          && name.equals(getDecoratorName(call))
          && call.getArguments().length == 1
          && call.getArguments()[0] instanceof JSObjectLiteralExpression) {
        return call;
      }
    }
    return null;
  }

  @Nullable
  public static HtmlFileImpl findAngularComponentTemplate(@NotNull TypeScriptClass cls) {
    if (cls.getAttributeList() == null) {
      return null;
    }
    JSCallExpression componentDecorator = getDecorator(cls, "Component");
    if (componentDecorator == null) {
      return null;
    }
    JSObjectLiteralExpression props = (JSObjectLiteralExpression)componentDecorator.getArguments()[0];
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
    return null;
  }

  public static boolean isPrivateMember(JSPsiElementBase element) {
    if (element instanceof JSAttributeListOwner) {
      JSAttributeListOwner attributeListOwner = (JSAttributeListOwner)element;
      return attributeListOwner.getAttributeList() != null
             && attributeListOwner.getAttributeList().getAccessType() == JSAttributeList.AccessType.PRIVATE;
    }
    return false;
  }

  public static boolean isTemplate(PsiElement decorator) {
    final JSClass clazz = PsiTreeUtil.getParentOfType(decorator, JSClass.class);
    if (clazz != null) {
      final JSFunction constructor = clazz.getConstructor();
      final JSParameterList params = constructor != null ? constructor.getParameterList() : null;
      return params != null && params.getText().contains(Angular2IndexingHandler.TEMPLATE_REF);
    }

    JsonProperty property = PsiTreeUtil.getParentOfType(decorator, JsonProperty.class);
    if (property == null || !"selector".equals(property.getName())) return false;
    property = PsiTreeUtil.getParentOfType(property, JsonProperty.class);
    if (property == null || !"arguments".equals(property.getName())) return false;
    property = PsiTreeUtil.getParentOfType(property, JsonProperty.class);
    if (property == null || !"decorators".equals(property.getName())) return false;
    PsiElement parent = property.getParent();
    if (parent instanceof JsonObject) {
      JsonProperty members = ((JsonObject)parent).findProperty("members");
      return members != null && members.getText().contains(Angular2IndexingHandler.TEMPLATE_REF);
    }
    return false;
  }

  @Nullable
  public static String getPropertyName(PsiElement decorator, String name) {
    final JSProperty selector = getProperty(decorator, name);
    final JSExpression value = selector != null ? selector.getValue() : null;
    if (value instanceof JSBinaryExpression) {
      return JSInjectionController.getConcatenationText(value);
    }
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      return AngularJSIndexingHandler.unquote(value);
    }
    return null;
  }

  @Nullable
  public static JSProperty getProperty(@Nullable PsiElement decorator, @NotNull String name) {
    final JSArgumentList argumentList = PsiTreeUtil.getChildOfType(decorator, JSArgumentList.class);
    JSExpression[] arguments = argumentList != null ? argumentList.getArguments() : null;
    if (arguments == null) {
      final JSArrayLiteralExpression array = PsiTreeUtil.getChildOfType(decorator, JSArrayLiteralExpression.class);
      arguments = array != null ? array.getExpressions() : null;
    }
    final JSObjectLiteralExpression descriptor = ObjectUtils.tryCast(arguments != null && arguments.length > 0 ? arguments[0] : null,
                                                                     JSObjectLiteralExpression.class);
    return descriptor != null ? descriptor.findProperty(name) : null;
  }

  public static boolean isDirective(@Nullable String name) {
    return "Directive".equals(name) || "DirectiveAnnotation".equals(name) ||
           "Component".equals(name) || "ComponentAnnotation".equals(name);
  }

  public static boolean isModule(@Nullable String name) {
    return "NgModule".equals(name);
  }

  public static boolean isPipe(@Nullable String name) {
    return "Pipe".equals(name);
  }
}
