package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.psi.types.evaluable.JSCustomElementType;
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveArrayType;
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

  public ActionScriptTypeEvaluator(@NotNull JSEvaluateContext context) {
    super(context);
  }

  @Override
  protected void evaluateNewExpressionTypes(@NotNull JSNewExpression newExpression) {
    JSExpression methodExpr = newExpression.getMethodExpression();
    if (methodExpr != null) {
      if (methodExpr instanceof JSArrayLiteralExpression) {
        JSTypeSource source = JSTypeSourceFactory.createTypeSource(methodExpr);
        JSType type = JSNamedType.createType(VECTOR_CLASS_NAME, source, JSContext.INSTANCE);

        PsiElement arrayInitializingType = newExpression.getArrayInitializingType();
        if (arrayInitializingType != null) {
          JSType argType = JSTypeParser.createType(newExpression.getProject(),
                                                   JSImportHandlingUtil.resolveTypeName(arrayInitializingType.getText(), newExpression),
                                                   source);
          type = new JSGenericTypeImpl(source, type, argType);
        }
        addType(type);
      }
      else {
        JSType type = JSAnyType.get(methodExpr, false);
        if (methodExpr instanceof JSReferenceExpression) {
          PsiElement resolve = ((JSReferenceExpression)methodExpr).resolve();
          if (JSResolveUtil.isConstructorFunction(resolve) && resolve.getParent() instanceof JSClass) {
            resolve = resolve.getParent();
          }
          if (resolve instanceof JSClass || resolve == null) {
            JSType typeFromText = JSTypeParser.createType(methodExpr.getProject(), methodExpr.getText(),
                                                          JSTypeSourceFactory.createTypeSource(methodExpr, false));
            if (typeFromText != null) type = typeFromText;
          }
        }
        addType(type);
      }
    }
  }

  @Override
  protected void addTypeFromClassCandidate(@NotNull JSClass resolveResult) {
    final JSReferenceExpression expression = myContext.getProcessedExpression();
    if (expression == null) return;

    JSType typeFromClass = ActionScriptResolveUtil.getTypeFromClass(expression, resolveResult);
    if (typeFromClass != null) {
      addType(typeFromClass);
    }
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
          addType(type);
        }
      }
      else {
        final XmlTag tag = PsiTreeUtil.getParentOfType(resolveResult, XmlTag.class, false);
        final JSClass clazz = JSResolveUtil.getClassFromTagNameInMxml(tag);
        if (clazz != null) {
            final String name = clazz.getQualifiedName();
            if (name != null) {
              addType(JSNamedType.createType(name, JSTypeSourceFactory.createTypeSource(clazz, false), JSContext.UNKNOWN));
            }
        }
      }
    }
    else if (resolveResult instanceof JSPackage) {
      addType(new JSCustomElementType(resolveResult));
    }
    else {
      super.addTypeFromElementResolveResult(resolveResult);
    }
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
  public void addType(@Nullable final JSType _type) {
    if (_type instanceof JSPsiBasedTypeOfType) {
      PsiElement element = ((JSPsiBasedTypeOfType)_type).getElement();
      if (element instanceof JSReferenceExpression && ((JSReferenceExpression)element).resolve() == element) {
        return;
      }
    }
    super.addType(_type);
  }

  @NotNull
  @Override
  protected JSType createTypeForThisExpression(@NotNull JSContext staticOrInstance,
                                               @NotNull JSClass jsClass,
                                               @NotNull JSTypeSource typeSource) {
    String name = jsClass.getQualifiedName();
    if (name == null) {
      return JSAnyType.get(typeSource);
    }
    return JSNamedTypeFactory.createType(name, typeSource, staticOrInstance);
  }

  @Override
  protected @Nullable JSType getIndexedPropertyAccessType(@NotNull JSIndexedPropertyAccessExpression rawqualifier) {
    return addComponentTypeFromArrayExpression(rawqualifier, rawqualifier.getQualifier());
  }
}
