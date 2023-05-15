package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.completion.ActionScriptSmartCompletionContributor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.lang.javascript.validation.JSProblemReporter;
import com.intellij.lang.javascript.validation.JSTypeChecker;
import com.intellij.lang.javascript.validation.ValidateTypesUtil;
import com.intellij.lang.javascript.validation.fixes.JSChangeSignatureFix;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.List;
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
  public boolean checkExpressionIsAssignableToVariable(JSVariable p,
                                                       final JSExpression expr,
                                                       @PropertyKey(resourceBundle = JavaScriptBundle.BUNDLE) String problemKey) {
    final JSType type = p.getJSType();
    boolean isAssignable =
      checkExpressionIsAssignableToTypeAndReportError(expr, type, p, problemKey, null, true);

    if (isAssignable &&
        type != null && FUNCTION_CLASS_NAMES.contains(type.getResolvedTypeText()) &&
        p instanceof JSParameter &&
        isAddEventListenerMethod((JSFunction)p.getParent().getParent())) {
      JSFunction fun = 
        expr instanceof JSReferenceExpression ref && ref.resolve() instanceof JSFunction fn ? fn :
        expr instanceof JSFunctionExpression fn ? fn :
        null;
      if (fun != null) {
        JSParameterList parameterList = fun.getParameterList();

        if (parameterList != null) {
          JSParameter[] parameters = parameterList.getParameterVariables();
          boolean invalidArgs = parameters.length == 0;

          if (!invalidArgs && parameters.length > 1) {
            for (int i = parameters.length - 1; i > 0; --i) {
              if (!parameters[i].isRest() && parameters[i].getInitializer() == null) {
                invalidArgs = true;
                break;
              }
            }
          }


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
              FlexBundle.message("javascript.callback.signature.mismatch"),
              ProblemHighlightType.WEAK_WARNING,
              getChangeSignatureFixForEventListener(fun, expr)
            );
            return false;
          }
          else {
            final JSClass expectedEventClass = calcNontrivialExpectedEventType(expr);
            JSType paramType = parameters[0].getJSType();
            final String actualParameterType = paramType != null ? paramType.getResolvedTypeText() : null;

            if (expectedEventClass == null) {
              if (!ActionScriptResolveUtil.isAssignableType(FlexCommonTypeNames.FLASH_EVENT_FQN, actualParameterType, parameters[0]) &&
                  !ActionScriptResolveUtil.isAssignableType(FlexCommonTypeNames.STARLING_EVENT_FQN, actualParameterType, parameters[0])) {
                registerProblem(
                  expr instanceof JSFunctionExpression ? parameters[0] : expr,
                  FlexBundle.message("javascript.callback.signature.mismatch"),
                  ProblemHighlightType.WEAK_WARNING,
                  getChangeSignatureFixForEventListener(fun, expr)
                );
                return false;
              }
            }
            else {
              if (!ActionScriptResolveUtil.isAssignableType(actualParameterType, expectedEventClass.getQualifiedName(), parameters[0])) {
                registerProblem(
                  expr instanceof JSFunctionExpression ? parameters[0] : expr,
                  FlexBundle.message("javascript.callback.signature.mismatch.event.class", expectedEventClass.getQualifiedName()),
                  ProblemHighlightType.WEAK_WARNING,
                  getChangeSignatureFixForEventListener(fun, expr)
                );
                return false;
              }
            }
          }
        }
      }
    }
    return isAssignable;
  }

  @NotNull
  private static JSChangeSignatureFix getChangeSignatureFixForEventListener(@NotNull JSFunction fun,
                                                                            @NotNull JSExpression expr) {
    JSClass jsClass = calcNontrivialExpectedEventType(expr);
    String typeText = jsClass != null ? jsClass.getQualifiedName() : FlexCommonTypeNames.FLASH_EVENT_FQN;
    
    return new JSChangeSignatureFix(fun){
      @NotNull
      @Override
      protected Pair<List<JSParameterInfo>, Boolean> buildParameterInfos(@NotNull JSFunction function) {
        ASTNode treeFromText =
          JSChangeUtil.createStatementFromTextWithContext(
            "function f(event:" + typeText + ") {}",
            function
          );
        JSParameterList expectedParameterList = ((JSFunction)treeFromText.getPsi()).getParameterList();

        return Pair.create(buildParameterInfosForExpected(function, expectedParameterList.getParameters()), false);
      }
    };
  }

  private static boolean isAddEventListenerMethod(final JSFunction method) {
    if (ActionScriptResolveUtil.ADD_EVENT_LISTENER_METHOD.equals(method.getName())) {
      PsiElement methodParent = method.getParent();
      if (methodParent instanceof JSClass declaringClass) {
        if (ActionScriptResolveUtil
              .isAssignableType(FlexCommonTypeNames.FLASH_IEVENT_DISPATCHER_FQN, declaringClass.getQualifiedName(), method)
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
            (StringUtil.startsWith(initializerText, "'") ||
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
      if (resolve instanceof JSClass clazz) {
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
      final JSVarStatement statement = node.getVarDeclaration();

      if (statement != null) {
        final PsiFile containingFile = node.getContainingFile();
        final JSExpression collectionExpression = node.getCollectionExpression();
        final String expressionType = ActionScriptResolveUtil.getQualifiedExpressionType(collectionExpression, containingFile);

        if (ActionScriptResolveUtil.isAssignableType(ValidateTypesUtil.FLASH_UTILS_DICTIONARY, expressionType, containingFile)) {
          return;
        }

        for (JSVariable var : statement.getVariables()) {
          final PsiElement typeElement = var.getTypeElement();
          final String typeElementText = typeElement == null ? null : typeElement.getText();

          if (typeElementText != null &&
              isValidArrayIndexType(typeElementText) &&
              ActionScriptResolveUtil.isAssignableType("Array", expressionType, containingFile)) {
            continue;
          }

          if (typeElement != null &&
              (OBJECT_CLASS_NAME.equals(typeElementText) ||
               ANY_TYPE.equals(typeElementText) ||
               OBJECT_CLASS_NAME.equals(expressionType) && !STRING_CLASS_NAME.equals(typeElementText))) {
            myReporter.registerProblem(typeElement, null, JavaScriptBundle.message("javascript.incorrect.array.type.in.for-in"),
                                       ProblemHighlightType.WEAK_WARNING);
            continue;
          }

          checkTypeIs(typeElement, typeElement, "XMLList".equals(expressionType) ? "XML" : "String");
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

  private void checkTypeIs(PsiElement type, PsiElement node, String typeName) {
    if (type instanceof JSReferenceExpression) {
      checkTypeIs((JSExpression)type, node, typeName);
    }
    else if (type != null) {
      myReporter.registerProblem(node, null, JavaScriptBundle.message("javascript.incorrect.variable.type.mismatch", typeName, type.getText()),
                                 getHighlightTypeForTypeOrSignatureProblem(node));
    }
  }

  private void checkTypeIs(JSExpression rOperand, PsiElement node, String typeName) {
    String expressionType = ActionScriptResolveUtil.getQualifiedExpressionType(rOperand, rOperand.getContainingFile());
    if (!typeName.equals(expressionType) && !ANY_TYPE.equals(expressionType)) {
      myReporter.registerProblem(node, null, JavaScriptBundle.message("javascript.incorrect.variable.type.mismatch", typeName, expressionType),
                                 getHighlightTypeForTypeOrSignatureProblem(node));
    }
  }

  @Override
  public void checkIfProperTypeReference(JSExpression rOperand) {
    String expressionType = ActionScriptResolveUtil.getQualifiedExpressionType(rOperand, rOperand.getContainingFile());
    if (!"Class".equals(expressionType) && !ANY_TYPE.equals(expressionType)) {
      myReporter.registerProblem(rOperand, null, FlexBundle.message("actionscript.binary.operand.type.mismatch", "Class", expressionType),
                                 getHighlightTypeForTypeOrSignatureProblem(rOperand));
    }
  }
}
