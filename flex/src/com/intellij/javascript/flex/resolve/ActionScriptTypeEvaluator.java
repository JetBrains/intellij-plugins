package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.e4x.JSE4XNamespaceReference;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageWrapper;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.resolve.context.JSApplyCallElement;
import com.intellij.lang.javascript.psi.resolve.context.JSApplyContextElement;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveArrayType;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.ARRAY_CLASS_NAME;
import static com.intellij.lang.javascript.psi.JSCommonTypeNames.VECTOR_CLASS_NAME;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptTypeEvaluator extends JSTypeEvaluator {
  private static final String REPEATER_CLASS_FQN = "mx.core.Repeater";

  public ActionScriptTypeEvaluator(JSEvaluateContext context, JSTypeProcessor processor) {
    super(context, processor, JSTypeEvaluationHelper.DEFAULT);
  }

  @Override
  protected boolean addTypeFromDialectSpecificElements(PsiElement resolveResult) {
    if (resolveResult instanceof JSPackageWrapper) {
      myTypeProcessor.processResolvedElement(resolveResult, myContext);
      return true;
    }
    return false;
  }

  @Override
  protected void evaluateNewExpressionTypes(JSNewExpression newExpression) {
    JSExpression methodExpr = newExpression.getMethodExpression();
    if (methodExpr != null) {
      if (methodExpr instanceof JSArrayLiteralExpression) {
        JSTypeSource source = JSTypeSourceFactory.createTypeSource(methodExpr);
        JSType type = JSNamedType.createType(VECTOR_CLASS_NAME, source, JSContext.INSTANCE);

        PsiElement arrayInitializingType = newExpression.getArrayInitializingType();
        if (arrayInitializingType != null) {
          JSType argType = JSTypeUtils.createType(JSImportHandlingUtil.resolveTypeName(arrayInitializingType.getText(), newExpression), source);
          type = new JSGenericTypeImpl(source, type, argType);
        }
        addType(type, methodExpr);
      }
      else {
        JSType type = JSAnyType.get(methodExpr, false);
        if (methodExpr instanceof JSReferenceExpression) {
          PsiElement resolve = ((JSReferenceExpression)methodExpr).resolve();
          if (JSResolveUtil.isConstructorFunction(resolve) && resolve.getParent() instanceof JSClass) {
            resolve = resolve.getParent();
          }
          if (resolve instanceof JSClass || resolve == null) {
            JSType typeFromText = JSTypeUtils.createType(methodExpr.getText(), JSTypeSourceFactory.createTypeSource(methodExpr, false));
            if (typeFromText != null) type = typeFromText;
          }
        }
        addType(type, methodExpr);
      }
    }
  }

  @Override
  protected void addTypeFromClassCandidate(@NotNull JSClass resolveResult) {
    final JSReferenceExpression expression = myContext.getProcessedExpression();
    if (expression == null) return;

    PsiElement parent = expression.getParent();
    if (parent instanceof JSExpression) parent = JSUtils.unparenthesize((JSExpression)parent);
    String psiElementType = parent instanceof JSReferenceExpression ||
                            JSResolveUtil.isExprInStrictTypeContext(expression) ||
                            PsiTreeUtil.getChildOfType(expression, JSE4XNamespaceReference.class) != null || // TODO avoid it
                            parent instanceof JSCallExpression ?
                            resolveResult.getQualifiedName() : "Class";
    JSTypeSource source = JSTypeSourceFactory.createTypeSource(expression);
    JSType namedType = JSNamedType.createType(psiElementType, source, JSContext.UNKNOWN);
    JSType type = JSTypeUtils.isActionScriptVectorType(namedType) ?
                  JSTypeUtils.createType(JSImportHandlingUtil.resolveTypeName(expression.getText(), expression), source) :
                  namedType;
    final JSApplyContextElement peek = myContext.peekJSElementToApply();
    if (peek instanceof JSApplyCallElement) {
      // MyClass(anyVar) is cast to MyClass
      myContext.processWithoutTopJSElementToApply(() -> addType(type, resolveResult));
    }
    else {
      addType(type, resolveResult);
    }
  }

  @Override
  protected boolean useVariableType(JSType type) {
    return myContext.isJSElementsToApplyEmpty() && super.useVariableType(type);
  }

  /**
   * @deprecated use {@link #addType(JSType, PsiElement)}
   */

  @Deprecated
  protected void addType(@NotNull String type, @Nullable final PsiElement source) {
    addType(JSNamedType.createType(type, JSTypeSourceFactory.createTypeSource(source, false), JSContext.UNKNOWN), source);
  }
  
  @Override
  protected void addTypeFromElementResolveResult(@Nullable PsiElement resolveResult) {
    if (resolveResult instanceof JSOffsetBasedImplicitElement && JavaScriptSupportLoader.isFlexMxmFile(resolveResult.getContainingFile())) {
      resolveResult = ((JSOffsetBasedImplicitElement)resolveResult).getElementAtOffset();
    }
    if (resolveResult instanceof XmlToken) {
      final XmlToken xmlToken = (XmlToken)resolveResult;
      final XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(xmlToken, XmlAttribute.class);
      final XmlTag xmlTag = PsiTreeUtil.getParentOfType(xmlToken, XmlTag.class);
      if (xmlToken.getTokenType() == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN &&
          xmlAttribute != null &&
          "id".equals(xmlAttribute.getName()) &&
          xmlTag != null &&
          isInsideRepeaterTag(xmlTag)) {
        final PsiElement arrayClass = ActionScriptClassResolver.findClassByQNameStatic(ARRAY_CLASS_NAME, xmlToken);
        if (arrayClass != null) {
          final String arrayType = new JSTagContextBuilder(resolveResult, null).typeName;
          JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
          JSType type;
          if (arrayType != null) {
            JSType baseType = JSNamedType.createType(arrayType, source, JSContext.INSTANCE);
            type = new JSArrayTypeImpl(baseType, source);
          }
          else {
            type = new JSPrimitiveArrayType(source, JSTypeContext.INSTANCE);
          }
          addType(type, arrayClass);
        }
      }
      else {
        final XmlTag tag = PsiTreeUtil.getParentOfType(resolveResult, XmlTag.class, false);
        final JSClass clazz = JSResolveUtil.getClassFromTagNameInMxml(tag);
        if (clazz != null) {
            final String name = clazz.getQualifiedName();
            if (name != null) {
              addType(name, clazz);
            }
        }
      }
      return;
    }

    super.addTypeFromElementResolveResult(resolveResult);
  }

  private static boolean isInsideRepeaterTag(@NotNull final XmlTag xmlTag) {
    PsiElement parent = xmlTag;
    while ((parent = parent.getParent()) instanceof XmlTag) {
      if (REPEATER_CLASS_FQN.equals(new JSTagContextBuilder(parent, "").typeName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void addType(@Nullable final JSType _type, @Nullable PsiElement source) {
    if (_type != null &&
        myContext.isJSElementsToApplyEmpty() &&
        (source == null || source == EXPLICIT_TYPE_MARKER_ELEMENT)
      ) {
      // TODO [ksafonov] enforced scope (and context) should internal part of JSType.resolve()
      JSClass jsClass = JSInheritanceUtil.withEnforcedScope(() -> _type.resolveClass(), JSResolveUtil.getResolveScope(myContext.targetFile));
      if (jsClass != null) {
        source = jsClass;
      }
    }
    super.addType(_type, source);
  }

  @NotNull
  @Override
  protected JSType createTypeForThisExpression(@NotNull JSContext staticOrInstance,
                                               @NotNull JSClass jsClass,
                                               @NotNull JSTypeSource typeSource) {
    String name = jsClass.getQualifiedName();
    if (name == null) {
      Logger.getInstance(ActionScriptTypeEvaluator.class).error(new IllegalArgumentException("name can't be null"));
      return JSAnyType.get(typeSource);
    }
    return JSNamedTypeFactory.createType(name, typeSource, staticOrInstance);
  }
}
