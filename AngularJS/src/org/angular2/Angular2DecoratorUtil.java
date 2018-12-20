// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.javascript.JSInjectionController;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ArrayUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static com.intellij.psi.util.PsiTreeUtil.getStubChildrenOfTypeAsList;
import static com.intellij.util.ArrayUtil.contains;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2DecoratorUtil {

  public static final String DIRECTIVE_DEC = "Directive";
  public static final String COMPONENT_DEC = "Component";
  public static final String PIPE_DEC = "Pipe";
  public static final String INPUT_DEC = "Input";
  public static final String OUTPUT_DEC = "Output";
  public static final String VIEW_CHILD_DEC = "ViewChild";

  public static final String NAME_PROP = "name";
  public static final String SELECTOR_PROP = "selector";
  public static final String EXPORT_AS_PROP = "exportAs";
  public static final String INPUTS_PROP = "inputs";
  public static final String OUTPUTS_PROP = "outputs";
  public static final String STYLE_URLS_PROP = "styleUrls";

  public static boolean isLiteralInNgDecorator(@Nullable PsiElement element, @NotNull String propertyName, String... decoratorNames) {
    if (element instanceof JSLiteralExpression) {
      final JSLiteralExpression literal = (JSLiteralExpression)element;
      final PsiElement parent;
      return literal.isQuotedLiteral()
             && (parent = literal.getParent()) instanceof JSProperty
             && propertyName.equals(((JSProperty)parent).getName())
             && contains(doIfNotNull(getParentOfType(parent, ES6Decorator.class), ES6Decorator::getDecoratorName),
                         decoratorNames);
    }
    return false;
  }

  @StubSafe
  @Nullable
  public static ES6Decorator findDecorator(@NotNull JSClass cls, @NotNull String... names) {
    JSAttributeList list = cls.getAttributeList();
    if (list == null || names.length == 0) {
      return null;
    }
    for (ES6Decorator decorator : getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      String decoratorName = decorator.getDecoratorName();
      for (String n : names) {
        if (n.equals(decoratorName)) {
          return decorator;
        }
      }
    }
    return null;
  }

  @StubSafe
  @Nullable
  public static ES6Decorator findDecorator(@NotNull JSClass cls, @NotNull String name) {
    JSAttributeList list = cls.getAttributeList();
    if (list == null) {
      return null;
    }
    for (ES6Decorator decorator : getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      if (name.equals(decorator.getDecoratorName())) {
        return decorator;
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

  @StubUnsafe
  @Nullable
  public static String getPropertyValue(@Nullable ES6Decorator decorator, @NotNull String name) {
    return getExpressionStringValue(doIfNotNull(getProperty(decorator, name), JSProperty::getValue));
  }

  @StubUnsafe
  @Nullable
  public static String getExpressionStringValue(@Nullable JSExpression value) {
    if (value instanceof JSBinaryExpression) {
      return JSInjectionController.getConcatenationText(value);
    }
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      return AngularJSIndexingHandler.unquote(value);
    }
    return null;
  }

  @StubSafe
  @Nullable
  public static JSProperty getProperty(@Nullable ES6Decorator decorator, @NotNull String name) {
    JSObjectLiteralExpression objectLiteralExpression = null;
    for (PsiElement child: getStubChildrenOfTypeAsList(decorator, PsiElement.class)) {
      if (child instanceof JSCallExpression) {
        StubElement<?> callStub = child instanceof StubBasedPsiElement ? ((StubBasedPsiElement)child).getStub() : null;
        if (callStub != null) {
          for (StubElement callChildStub: callStub.getChildrenStubs()) {
            PsiElement callChild = callChildStub.getPsi();
            if (callChild instanceof JSObjectLiteralExpression) {
              objectLiteralExpression = (JSObjectLiteralExpression)callChild;
              break;
            }
          }
        } else {
          objectLiteralExpression = tryCast(ArrayUtil.getFirstElement(((JSCallExpression)child).getArguments()),
                                            JSObjectLiteralExpression.class);
        }
        break;
      } else if (child instanceof JSObjectLiteralExpression) {
        objectLiteralExpression = (JSObjectLiteralExpression)child;
        break;
      }
    }
    return doIfNotNull(objectLiteralExpression, expr -> expr.findProperty(name));
  }

}
