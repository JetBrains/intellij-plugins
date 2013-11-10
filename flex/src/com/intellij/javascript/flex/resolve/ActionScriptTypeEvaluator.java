package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.e4x.JSE4XNamespaceReference;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSPackageWrapper;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveArrayType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.ARRAY_CLASS_NAME;
import static com.intellij.lang.javascript.psi.JSCommonTypeNames.VECTOR_CLASS_NAME;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptTypeEvaluator extends JSTypeEvaluator {
  private static final String REPEATER_CLASS_FQN = "mx.core.Repeater";

  public ActionScriptTypeEvaluator(BaseJSSymbolProcessor.EvaluateContext context,
                                      BaseJSSymbolProcessor.TypeProcessor processor, boolean ecma) {
    super(context, processor, ecma);
  }

  @Override
  protected boolean addTypeFromDialectSpecificElements(JSReferenceExpression expression, PsiElement resolveResult, boolean wasPrototype) {
    if (resolveResult instanceof JSPackageWrapper) {
      if (myTypeProcessor instanceof PsiScopeProcessor) {
        if (myTypeProcessor instanceof ResolveProcessor) ((ResolveProcessor)myTypeProcessor).prefixResolved();
        resolveResult.processDeclarations((PsiScopeProcessor)myTypeProcessor, ResolveState.initial(), expression, expression);
      } else {
        addType(((JSQualifiedNamedElement)resolveResult).getQualifiedName(),
                expression);
      }
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
        JSType type = JSNamedType.createType(VECTOR_CLASS_NAME, source, JSNamedType.StaticOrInstance.INSTANCE);

        PsiElement arrayInitializingType = newExpression.getArrayInitializingType();
        if (arrayInitializingType != null) {
          JSType argType = JSNamedType.createType(JSImportHandlingUtil.resolveTypeName(arrayInitializingType.getText(), newExpression),
                                                  source,
                                                  JSNamedType.StaticOrInstance.INSTANCE);
          type = new JSGenericTypeImpl(source, type);
          ((JSGenericTypeImpl)type).addGenericArgument(argType);
        }
        addType(type, methodExpr);
      }
      else {
        String text = methodExpr.getText();
        JSResolveUtil.GenericSignature signature = JSResolveUtil.extractGenericSignature(text);
        if (signature != null) {
          text = signature.elementType;
        }
        if (methodExpr instanceof JSReferenceExpression && ((JSReferenceExpression)methodExpr).resolve() instanceof JSVariable) {
          text = "*";
        } else {
          BaseJSSymbolProcessor.SimpleTypeProcessor subProcessor = new BaseJSSymbolProcessor.SimpleTypeProcessor();
          JSTypeEvaluator.evaluateTypes(methodExpr, myContext.targetFile, subProcessor);

          JSType type = subProcessor.getType();
          if (type != JSType.ANY && JSTypeUtils.hasFunctionType(type)) {
            type = JSType.ANY;
            text = "*";
          }

          if (type != JSType.NO_TYPE && (type != JSType.ANY || isNotValidType(text))) {
            text = "Class".equals(type.getTypeText())? "*": type.getTypeText();
          }

          if(!"*".equals(text)) {
            text = JSImportHandlingUtil.resolveTypeName(text, methodExpr);
          }
        }
        addType(JSTypeUtils.createType(text, JSTypeSourceFactory.createTypeSource(methodExpr, false)), methodExpr);
      }
    }
  }

  private static boolean isNotValidType(String text) {
    return text.indexOf('[') != -1 || text.indexOf('(') != -1 || text.indexOf('{') != -1;
  }

  @Override
  protected void addTypeFromClass(JSReferenceExpression expression, PsiElement parent, PsiElement resolveResult) {
    if (resolveResult instanceof JSFunction) {
      resolveResult = resolveResult.getParent();
    }
    String psiElementType = parent instanceof JSReferenceExpression ||
                            JSResolveUtil.isExprInStrictTypeContext(expression) ||
                            PsiTreeUtil.getChildOfType(expression, JSE4XNamespaceReference.class) != null || // TODO avoid it
                            parent instanceof JSCallExpression ?
                            ((JSClass)resolveResult).getQualifiedName():"Class";
    JSTypeSource source = JSTypeSourceFactory.createTypeSource(expression);
    JSType type = JSNamedType.createType(psiElementType, source, JSNamedType.StaticOrInstance.UNKNOWN);
    if (type instanceof JSGenericTypeImpl) type = ((JSGenericTypeImpl)type).getType();
    if (JSTypeUtils.isActionScriptVectorType(type)) {
      type = JSTypeUtils.createType(JSImportHandlingUtil.resolveTypeName(expression.getText(), expression), source);
    }
    myCallExpressionsToApply.pollLast(); // MyClass(anyVar) is cast to MyClass
    addType(type, resolveResult);
  }

  @Override
  protected boolean useVariableType(JSType type) {
    return myCallExpressionsToApply.isEmpty() && super.useVariableType(type);
  }

  @Override
  protected boolean addTypeFromElementResolveResult(JSReferenceExpression expression,
                                                    PsiElement parent,
                                                    PsiElement resolveResult,
                                                    boolean wasPrototype,
                                                    boolean hasSomeType) {
    if (resolveResult instanceof JSNamedElementProxy && JavaScriptSupportLoader.isFlexMxmFile(resolveResult.getContainingFile())) {
      resolveResult = ((JSNamedElementProxy)resolveResult).getElement();
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
          final String arrayType = new BaseJSSymbolProcessor.TagContextBuilder(resolveResult, null).typeName;
          JSTypeSource source = JSTypeSourceFactory.createTypeSource(resolveResult);
          JSType type;
          if (arrayType != null) {
            JSType baseType = JSNamedType.createType(arrayType, source, JSNamedType.StaticOrInstance.INSTANCE);
            type = new JSArrayTypeImpl(baseType, source);
          }
          else {
            type = new JSPrimitiveArrayType(source, JSNamedType.StaticOrInstance.INSTANCE);
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
      return hasSomeType;
    }
    return super.addTypeFromElementResolveResult(expression, parent, resolveResult, wasPrototype, hasSomeType);
  }

  private static boolean isInsideRepeaterTag(final @NotNull XmlTag xmlTag) {
    PsiElement parent = xmlTag;
    while ((parent = parent.getParent()) instanceof XmlTag) {
      if (REPEATER_CLASS_FQN.equals(new BaseJSSymbolProcessor.TagContextBuilder(parent, "").typeName)) {
        return true;
      }
    }
    return false;
  }
}
