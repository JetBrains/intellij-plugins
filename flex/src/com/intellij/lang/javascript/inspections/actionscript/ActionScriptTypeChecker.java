package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.completion.ActionScriptSmartCompletionContributor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType;
import com.intellij.lang.javascript.validation.JSProblemReporter;
import com.intellij.lang.javascript.validation.JSTypeChecker;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import com.intellij.lang.javascript.validation.fixes.ChangeSignatureFix;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.Map;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.*;
import static com.intellij.lang.javascript.validation.ValidateTypesUtil.getHighlightTypeForTypeOrSignatureProblem;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptTypeChecker extends JSTypeChecker {

  public ActionScriptTypeChecker(JSProblemReporter<?> reporter) {
    super(reporter);
  }

  @Override
  public void checkExpressionIsAssignableToVariable(JSVariable p,
                                                    final JSExpression expr,
                                                    PsiFile containingFile,
                                                    @PropertyKey(resourceBundle = JSBundle.BUNDLE) String problemKey,
                                                    boolean allowChangeVariableTypeFix) {
    final JSType type = p.getType();
    boolean isAssignable = checkExpressionIsAssignableToType(expr, type, p, problemKey, allowChangeVariableTypeFix ? p : null, null, true);

    PsiElement _fun;
    if (isAssignable &&
        type != null && FUNCTION_CLASS_NAME.equals(type.getResolvedTypeText()) &&
        p instanceof JSParameter &&
        isAddEventListenerMethod((JSFunction)p.getParent().getParent()) &&
        (( expr instanceof JSReferenceExpression &&
           (_fun = ((JSReferenceExpression)expr).resolve()) instanceof JSFunction
         ) ||
         (
           expr instanceof JSFunctionExpression &&
           (_fun = expr) != null
         )
        )) {
      JSFunction fun = (JSFunction)_fun;
      JSParameterList parameterList = fun.getParameterList();

      if (parameterList != null) {
        JSParameter[] parameters = parameterList.getParameterVariables();
        boolean invalidArgs = parameters.length == 0;

        if (!invalidArgs && parameters.length > 1) {
          for(int i = parameters.length - 1; i > 0; --i) {
            if (!parameters[i].isRest() && parameters[i].getInitializer() == null) {
              invalidArgs = true;
              break;
            }
          }
        }

        Computable.NotNullCachedComputable<JSParameterList> expectedParameterListForEventListener =
          new Computable.NotNullCachedComputable<JSParameterList>() {
            @NotNull
            @Override
            protected JSParameterList internalCompute() {
              JSClass jsClass = calcNontrivialExpectedEventType(expr);
              ASTNode treeFromText =
                JSChangeUtil.createJSTreeFromText(
                  expr.getProject(),
                  "function f(event:" + (jsClass != null ? jsClass.getQualifiedName() : FlexCommonTypeNames.FLASH_EVENT_FQN) + ") {}",
                  JavaScriptSupportLoader.ECMA_SCRIPT_L4
                );
              return ((JSFunction)treeFromText.getPsi()).getParameterList();
            }
          };

        if (invalidArgs) {
          PsiElement expr_;
          if (expr instanceof JSFunctionExpression) {
            expr_ = ((JSFunctionExpression)expr).getParameterList();
          }
          else {
            expr_ = expr;
          }
          registerProblem(
            expr_,
            JSBundle.message("javascript.callback.signature.mismatch"),
            ProblemHighlightType.WEAK_WARNING,
            new ChangeSignatureFix(fun, expectedParameterListForEventListener)
          );
        } else {
          final JSClass expectedEventClass = calcNontrivialExpectedEventType(expr);
          JSType paramType = parameters[0].getType();
          final String actualParameterType = paramType != null ? paramType.getResolvedTypeText() : null;

          if (expectedEventClass == null) {
            if (!JSResolveUtil.isAssignableType(FlexCommonTypeNames.FLASH_EVENT_FQN, actualParameterType, parameters[0]) &&
                !JSResolveUtil.isAssignableType(FlexCommonTypeNames.STARLING_EVENT_FQN, actualParameterType, parameters[0])) {
              registerProblem(
                expr instanceof JSFunctionExpression ? parameters[0] : expr,
                JSBundle.message("javascript.callback.signature.mismatch"),
                ProblemHighlightType.WEAK_WARNING,
                new ChangeSignatureFix(fun, expectedParameterListForEventListener)
              );
            }
          }
          else {
            if (!JSResolveUtil.isAssignableType(actualParameterType, expectedEventClass.getQualifiedName(), parameters[0])) {
              registerProblem(
                expr instanceof JSFunctionExpression ? parameters[0] : expr,
                JSBundle.message("javascript.callback.signature.mismatch.event.class", expectedEventClass.getQualifiedName()),
                ProblemHighlightType.WEAK_WARNING,
                new ChangeSignatureFix(fun, expectedParameterListForEventListener)
              );
            }
          }
        }
      }
    }
  }

  @Override
  protected void registerExpressionNotAssignableToType(PsiElement expr,
                                                       PsiElement typeOwner,
                                                       String message,
                                                       ProblemHighlightType problemHighlightType,
                                                       LocalQuickFix... fixes) {
    if (typeOwner != null && expr instanceof JSExpression &&
        typeOwner.getParent() instanceof JSParameterList &&
        expr.getParent() instanceof JSArgumentList) {
      JSFunction method = (JSFunction)typeOwner.getParent().getParent();
      if (!(JSResolveUtil.getExpressionJSType((JSExpression)expr) instanceof JSVoidType)) {
        JSFunction topMethod = JSInheritanceUtil.findTopMethods(method).iterator().next();
        fixes = ArrayUtil.append(fixes, new ChangeSignatureFix(topMethod, (JSArgumentList)expr.getParent()));
      }
    }
    myReporter.registerProblem(expr, message, problemHighlightType, fixes);
  }

  private static boolean isAddEventListenerMethod(final JSFunction method) {
    if (ActionScriptResolveUtil.ADD_EVENT_LISTENER_METHOD.equals(method.getName())) {
      PsiElement methodParent = method.getParent();
      if (methodParent instanceof JSClass) {
        JSClass declaringClass = (JSClass)methodParent;
        if (JSResolveUtil.isAssignableType(FlexCommonTypeNames.FLASH_IEVENT_DISPATCHER_FQN, declaringClass.getQualifiedName(), method)
          || ActionScriptClassResolver.isParentClass(declaringClass, FlexCommonTypeNames.STARLING_EVENT_DISPATCHER_FQN, false)) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  private static JSClass calcNontrivialExpectedEventType(JSExpression expr) {
    JSExpression prevExpr = PsiTreeUtil.findChildOfAnyType(expr.getParent(), JSExpression.class);

    String type = null;
    JSExpression adHocQualifierExpr = null;

    if (prevExpr instanceof JSReferenceExpression && prevExpr != expr) {
      PsiElement constantRef = ((JSReferenceExpression)prevExpr).resolve();

      if (constantRef instanceof JSVariable) {
        final String initializerText = ((JSVariable)constantRef).getLiteralOrReferenceInitializerText();
        if (initializerText != null &&
            (StringUtil.startsWith(initializerText, "\'") ||
             StringUtil.startsWith(initializerText, "\"")
            )) {
          type = StringUtil.stripQuotesAroundValue(initializerText);
        }
      }

      adHocQualifierExpr = ((JSReferenceExpression)prevExpr).getQualifier();
    } else if (prevExpr instanceof JSLiteralExpression) {
      type = StringUtil.stripQuotesAroundValue(prevExpr.getText());
    }

    if (type != null) {
      JSExpression methodExpression = ((JSCallExpression)expr.getParent().getParent()).getMethodExpression();
      if (methodExpression instanceof JSReferenceExpression) {
        JSClass clazz = ActionScriptSmartCompletionContributor.findClassOfQualifier((JSReferenceExpression)methodExpression);

        if (clazz != null) {
          Map<String,String> eventsMap = ActionScriptSmartCompletionContributor.getEventsMap(clazz);
          String qName = eventsMap.get(type);
          if (qName != null) {
            PsiElement classFromNamespace = JSClassResolver.findClassFromNamespace(qName, clazz);
            if (classFromNamespace instanceof JSClass) return (JSClass)classFromNamespace;
            // if uncomment next 2 lines then the following event listener parameter won't be highlighted with warning
            // new Sprite().addEventListener(ErrorEvent.ERROR, function(e:AccelerometerEvent):void{})
            //} else if (JSInheritanceUtil.isParentClass(clazz, "flash.events.EventDispatcher", true)) {
            //  adHocQualifierExpr = null;
          }
        }
      }
    }

    if (adHocQualifierExpr instanceof JSReferenceExpression) {
      PsiElement resolve = ((JSReferenceExpression)adHocQualifierExpr).resolve();
      if (resolve instanceof JSClass) {
        JSClass clazz = (JSClass)resolve;
        if (ActionScriptClassResolver.isParentClass((JSClass)resolve, FlexCommonTypeNames.FLASH_EVENT_FQN, false) ||
            ActionScriptClassResolver.isParentClass((JSClass)resolve, FlexCommonTypeNames.STARLING_EVENT_FQN, false)) {
          return clazz;
        }
      }
    }

    return null;
  }

  @Override
  public void checkTypesInForIn(@NotNull JSForInStatement node) {
    if (!node.isForEach()) {
      final JSVarStatement statement = node.getDeclarationStatement();

      if (statement != null) {
        final PsiFile containingFile = node.getContainingFile();
        final JSExpression collectionExpression = node.getCollectionExpression();
        final String expressionType = JSResolveUtil.getQualifiedExpressionType(collectionExpression, containingFile);

        if (JSResolveUtil.isAssignableType(ValidateTypesUtil.FLASH_UTILS_DICTIONARY, expressionType, containingFile)) {
          return;
        }

        for (JSVariable var : statement.getVariables()) {
          final PsiElement typeElement = var.getTypeElement();
          final String typeElementText = typeElement == null ? null : typeElement.getText();

          if (typeElementText != null &&
              isValidArrayIndexType(typeElementText) &&
              JSResolveUtil.isAssignableType("Array", expressionType, containingFile)) {
            continue;
          }

          if (typeElement != null &&
              (OBJECT_CLASS_NAME.equals(typeElementText) ||
               ANY_TYPE.equals(typeElementText) ||
               OBJECT_CLASS_NAME.equals(expressionType) && !STRING_CLASS_NAME.equals(typeElementText))) {
            myReporter.registerProblem(typeElement, JSBundle.message("javascript.incorrect.array.type.in.for-in"),
                                     ProblemHighlightType.WEAK_WARNING);
            continue;
          }

          checkTypeIs(typeElement, typeElement, "XMLList".equals(expressionType) ? "XML" : "String",
                      "javascript.incorrect.variable.type.mismatch");
        }
      }
    }
  }

  private static boolean isValidArrayIndexType(final String type) {
    return "String".equals(type) ||
           "int".equals(type) ||
           "uint".equals(type) ||
           "Number".equals(type);
  }

  private void checkTypeIs(PsiElement type, PsiElement node, String typeName, String key) {
    if (type instanceof JSReferenceExpression) {
      checkTypeIs((JSExpression)type, node, typeName, key);
    }
    else if (type != null) {
      myReporter.registerProblem(node, JSBundle.message(key, typeName, type.getText()),
                               getHighlightTypeForTypeOrSignatureProblem(node));
    }
  }

  private void checkTypeIs(JSExpression rOperand, PsiElement node, String typeName, String key) {
    String expressionType = JSResolveUtil.getQualifiedExpressionType(rOperand, rOperand.getContainingFile());
    if (!typeName.equals(expressionType) && !ANY_TYPE.equals(expressionType)) {
      myReporter.registerProblem(node, JSBundle.message(key, typeName, expressionType),
                                 getHighlightTypeForTypeOrSignatureProblem(node));
    }
  }

  @Override
  public void checkIfProperTypeReference(JSExpression rOperand) {
    checkTypeIs(
      rOperand,
      rOperand,
      "Class",
      "javascript.binary.operand.type.mismatch"
    );
  }
}
