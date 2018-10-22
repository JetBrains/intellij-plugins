// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.javascript.JSInjectionController;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2DecoratorUtil {

  public static final String DIRECTIVE_DEC = "Directive";
  public static final String COMPONENT_DEC = "Component";
  public static final String PIPE_DEC = "Pipe";
  public static final String INPUT_DEC = "Input";
  public static final String OUTPUT_DEC = "Output";

  public static final String NAME_PROP = "name";
  public static final String SELECTOR_PROP = "selector";
  public static final String EXPORT_AS_PROP = "exportAs";
  public static final String INPUTS_PROP = "inputs";
  public static final String OUTPUTS_PROP = "outputs";

  public static boolean isLiteralInNgDecorator(PsiElement element, String propertyName, String decoratorName) {
    if (element instanceof JSLiteralExpression) {
      final JSLiteralExpression literal = (JSLiteralExpression)element;
      final PsiElement parent;
      return literal.isQuotedLiteral()
             && (parent = literal.getParent()) instanceof JSProperty
             && propertyName.equals(((JSProperty)parent).getName())
             && decoratorName.equals(ObjectUtils.doIfNotNull(PsiTreeUtil.getParentOfType(parent, ES6Decorator.class),
                                                             ES6Decorator::getDecoratorName));
    }
    return false;
  }

  @Nullable
  public static ES6Decorator findDecorator(@NotNull JSClass cls, @NotNull String... names) {
    JSAttributeList list = cls.getAttributeList();
    if (list == null || names.length == 0) {
      return null;
    }
    for (ES6Decorator decorator : PsiTreeUtil.getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      String decoratorName = decorator.getDecoratorName();
      for (String n : names) {
        if (n.equals(decoratorName)) {
          return decorator;
        }
      }
    }
    return null;
  }

  @Nullable
  public static ES6Decorator findDecorator(@NotNull JSClass cls, @NotNull String name) {
    JSAttributeList list = cls.getAttributeList();
    if (list == null) {
      return null;
    }
    for (ES6Decorator decorator : PsiTreeUtil.getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      if (name.equals(decorator.getDecoratorName())) {
        return decorator;
      }
    }
    return null;
  }

  @Nullable
  public static JSCallExpression getDecoratorCall(JSClass cls, String name) {
    if (cls.getAttributeList() == null) {
      return null;
    }
    JSAttributeList list = cls.getAttributeList();
    for (ES6Decorator decorator : PsiTreeUtil.getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      String decoratorName = decorator.getDecoratorName();
      if (name.equals(decoratorName)) {
        JSCallExpression call = ObjectUtils.tryCast(decorator.getExpression(), JSCallExpression.class);
        if (call != null
            && call.getArguments().length == 1
            && call.getArguments()[0] instanceof JSObjectLiteralExpression) {
          return call;
        }
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
    if (decorator instanceof ES6Decorator) {
      decorator = ((ES6Decorator)decorator).getExpression();
    }
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
