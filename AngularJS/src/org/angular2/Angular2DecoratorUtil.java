// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.ecmascript6.psi.ES6FromClause;
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration;
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration;
import com.intellij.lang.javascript.injections.JSInjectionUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
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
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

import static com.intellij.psi.util.PsiTreeUtil.getContextOfType;
import static com.intellij.psi.util.PsiTreeUtil.getStubChildrenOfTypeAsList;
import static com.intellij.util.ArrayUtil.contains;
import static com.intellij.util.ObjectUtils.*;
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

  private static final Set<String> ANGULAR_RESOLVED_DECS = ContainerUtil.newHashSet(DIRECTIVE_DEC, COMPONENT_DEC, PIPE_DEC, MODULE_DEC,
                                                                                    VIEW_CHILD_DEC, VIEW_DEC);

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

  @StubSafe
  @Nullable
  public static ES6Decorator findDecorator(@NotNull JSAttributeListOwner attributeListOwner, @NotNull String... names) {
    JSAttributeList list = attributeListOwner.getAttributeList();
    if (list == null || names.length == 0) {
      return null;
    }
    for (ES6Decorator decorator : getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      if (isAngularEntityDecorator(decorator, names)) {
        return decorator;
      }
    }
    return null;
  }

  @StubSafe
  @Nullable
  public static ES6Decorator findDecorator(@NotNull JSAttributeListOwner attributeListOwner, @NotNull String name) {
    JSAttributeList list = attributeListOwner.getAttributeList();
    if (list == null) {
      return null;
    }
    for (ES6Decorator decorator : getStubChildrenOfTypeAsList(list, ES6Decorator.class)) {
      if (isAngularEntityDecorator(decorator, name)) {
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
      return JSInjectionUtil.getConcatenationText(value);
    }
    if (value instanceof JSLiteralExpression && ((JSLiteralExpression)value).isQuotedLiteral()) {
      return AngularJSIndexingHandler.unquote(value);
    }
    return null;
  }

  @StubSafe
  @Nullable
  public static JSObjectLiteralExpression getObjectLiteralInitializer(@Nullable ES6Decorator decorator) {
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
  @Nullable
  public static JSObjectLiteralExpression getReferencedObjectLiteralInitializer(@NotNull ES6Decorator decorator) {
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
  @Nullable
  public static JSProperty getProperty(@Nullable ES6Decorator decorator, @NotNull String name) {
    return doIfNotNull(getObjectLiteralInitializer(decorator),
                       expr -> expr.findProperty(name));
  }

  public static boolean isAngularEntityDecorator(@NotNull ES6Decorator decorator, @NotNull String... names) {
    String decoratorName = decorator.getDecoratorName();
    return decoratorName != null
           && contains(decoratorName, names)
           && doIfCast(decorator.getContext(), JSAttributeList.class,
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
}
