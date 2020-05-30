// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration;
import com.intellij.lang.javascript.injections.JSInjectionUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClassExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.AstLoadingFilter;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.intellij.psi.util.PsiTreeUtil.getContextOfType;
import static com.intellij.psi.util.PsiTreeUtil.getStubChildrenOfTypeAsList;
import static com.intellij.util.ArrayUtil.contains;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.index.Angular2IndexingHandler.TS_CLASS_TOKENS;
import static org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE;

public class Angular2DecoratorUtil {

  @NonNls public static final String DIRECTIVE_DEC = "Directive";
  @NonNls public static final String COMPONENT_DEC = "Component";
  @NonNls public static final String PIPE_DEC = "Pipe";
  @NonNls public static final String MODULE_DEC = "NgModule";
  @NonNls public static final String INPUT_DEC = "Input";
  @NonNls public static final String OUTPUT_DEC = "Output";
  @NonNls public static final String ATTRIBUTE_DEC = "Attribute";
  @NonNls public static final String VIEW_CHILD_DEC = "ViewChild";
  @NonNls public static final String VIEW_CHILDREN_DEC = "ViewChildren";
  @NonNls public static final String VIEW_DEC = "View";

  @NonNls public static final String NAME_PROP = "name";
  @NonNls public static final String SELECTOR_PROP = "selector";
  @NonNls public static final String EXPORT_AS_PROP = "exportAs";
  @NonNls public static final String INPUTS_PROP = "inputs";
  @NonNls public static final String OUTPUTS_PROP = "outputs";
  @NonNls public static final String IMPORTS_PROP = "imports";
  @NonNls public static final String EXPORTS_PROP = "exports";
  @NonNls public static final String DECLARATIONS_PROP = "declarations";
  @NonNls public static final String ENTRY_COMPONENTS_PROP = "entryComponents";
  @NonNls public static final String BOOTSTRAP_PROP = "bootstrap";

  @NonNls public static final String TEMPLATE_URL_PROP = "templateUrl";
  @NonNls public static final String TEMPLATE_PROP = "template";
  @NonNls public static final String STYLE_URLS_PROP = "styleUrls";
  @NonNls public static final String STYLES_PROP = "styles";

  public static boolean isLiteralInNgDecorator(@Nullable PsiElement element, @NotNull String propertyName, String... decoratorNames) {
    if (element instanceof JSLiteralExpression) {
      final JSLiteralExpression literal = (JSLiteralExpression)element;
      final PsiElement parent;
      return literal.isQuotedLiteral()
             && (parent = literal.getParent()) instanceof JSProperty
             && propertyName.equals(((JSProperty)parent).getName())
             && doIfNotNull(getContextOfType(parent, ES6Decorator.class),
                            decorator -> isAngularEntityDecorator(decorator, decoratorNames)) == Boolean.TRUE;
    }
    return false;
  }

  public static ES6Decorator findDecorator(@NotNull JSAttributeListOwner attributeListOwner, @NotNull String name) {
    return findDecorator(attributeListOwner, new String[]{name});
  }

  @StubSafe
  public static @Nullable ES6Decorator findDecorator(@NotNull JSAttributeListOwner attributeListOwner, String @NotNull ... names) {
    JSAttributeList list = attributeListOwner.getAttributeList();
    if (list == null || names.length == 0) {
      return null;
    }
    for (ES6Decorator decorator : getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      if (isAngularEntityDecorator(decorator, names)) {
        return decorator;
      }
    }
    if (attributeListOwner instanceof TypeScriptClassExpression) {
      JSAttributeListOwner context = tryCast(attributeListOwner.getContext(), JSAttributeListOwner.class);
      if (context != null) {
        return findDecorator(context, names);
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
  public static @Nullable String getPropertyValue(@Nullable ES6Decorator decorator, @NotNull String name) {
    return getExpressionStringValue(doIfNotNull(getProperty(decorator, name), JSProperty::getValue));
  }

  @StubUnsafe
  public static @Nullable String getExpressionStringValue(@Nullable JSExpression value) {
    if (value instanceof JSBinaryExpression) {
      return JSInjectionUtil.getConcatenationText(value);
    }
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      return AngularJSIndexingHandler.unquote(value);
    }
    return null;
  }

  @StubSafe
  public static @Nullable JSObjectLiteralExpression getObjectLiteralInitializer(@Nullable ES6Decorator decorator) {
    for (PsiElement child : getStubChildrenOfTypeAsList(decorator, PsiElement.class)) {
      if (child instanceof JSCallExpression) {
        StubElement<?> callStub = child instanceof StubBasedPsiElement ? ((StubBasedPsiElement)child).getStub() : null;
        if (callStub != null) {
          for (StubElement callChildStub : callStub.getChildrenStubs()) {
            PsiElement callChild = callChildStub.getPsi();
            if (callChild instanceof JSObjectLiteralExpression) {
              return (JSObjectLiteralExpression)callChild;
            }
          }
        }
        else {
          return tryCast(ArrayUtil.getFirstElement(((JSCallExpression)child).getArguments()),
                         JSObjectLiteralExpression.class);
        }
        break;
      }
      else if (child instanceof JSObjectLiteralExpression) {
        return (JSObjectLiteralExpression)child;
      }
    }
    return null;
  }

  @StubUnsafe
  public static @Nullable JSObjectLiteralExpression getReferencedObjectLiteralInitializer(@NotNull ES6Decorator decorator) {
    return AstLoadingFilter.forceAllowTreeLoading(decorator.getContainingFile(), () ->
      Optional.ofNullable(decorator.getExpression())
        .map(expr -> tryCast(expr, JSCallExpression.class))
        .map(expr -> ArrayUtil.getFirstElement(expr.getArguments()))
        .map(expr -> tryCast(expr, JSReferenceExpression.class))
        .map(expr -> expr.resolve())
        .map(expr -> tryCast(expr, JSVariable.class))
        .map(var -> var.getInitializerOrStub())
        .map(expr -> tryCast(expr, JSObjectLiteralExpression.class))
        .orElse(null));
  }

  @StubSafe
  public static @Nullable JSProperty getProperty(@Nullable ES6Decorator decorator, @NotNull String name) {
    return doIfNotNull(getObjectLiteralInitializer(decorator),
                       expr -> expr.findProperty(name));
  }

  public static boolean isAngularEntityDecorator(@NotNull ES6Decorator decorator, String @NotNull ... names) {
    String decoratorName = decorator.getDecoratorName();
    return decoratorName != null
           && contains(decoratorName, names)
           && (!decoratorName.equals(DIRECTIVE_DEC) || getObjectLiteralInitializer(decorator) != null)
           && doIfNotNull(doIfNotNull(getClassForDecoratorElement(decorator), JSAttributeListOwner::getAttributeList),
                          attrList -> attrList.hasModifier(JSAttributeList.ModifierType.ABSTRACT)) != Boolean.TRUE
           && Angular2LangUtil.isAngular2Context(decorator)
           && hasAngularImport(decoratorName, decorator.getContainingFile());
  }

  private static boolean hasAngularImport(@NotNull String name, @NotNull PsiFile file) {
    return Optional.ofNullable(JSStubBasedPsiTreeUtil.resolveLocally(name, file))
      .map(element -> getContextOfType(element, ES6ImportDeclaration.class))
      .map(ES6ImportExportDeclaration::getFromClause)
      .map(ES6FromClause::getReferenceText)
      .map(StringUtil::unquoteString)
      .map(from -> ANGULAR_CORE_PACKAGE.equals(from))
      .orElse(false);
  }

  public static @Nullable TypeScriptClass getClassForDecoratorElement(@Nullable PsiElement element) {
    ES6Decorator decorator = element instanceof ES6Decorator ? (ES6Decorator)element
                                                             : getContextOfType(element, ES6Decorator.class, false);
    if (decorator == null) {
      return null;
    }
    JSAttributeListOwner context = getContextOfType(decorator, JSAttributeListOwner.class);
    if (context == null) {
      return null;
    }
    if (context instanceof TypeScriptClass) {
      return (TypeScriptClass)context;
    }
    return (TypeScriptClass)ArrayUtil.getFirstElement(JSStubBasedPsiTreeUtil.getChildrenByType(context, TS_CLASS_TOKENS));
  }
}
