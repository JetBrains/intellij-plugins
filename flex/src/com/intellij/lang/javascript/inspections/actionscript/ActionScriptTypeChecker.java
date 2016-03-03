package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ActionScriptSmartCompletionContributor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.JSProblemReporter;
import com.intellij.lang.javascript.validation.JSTypeChecker;
import com.intellij.lang.javascript.validation.fixes.ChangeSignatureFix;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.Map;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.FUNCTION_CLASS_NAME;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptTypeChecker extends JSTypeChecker<Annotation> {
  private final JSProblemReporter<Annotation> myReporter;

  public ActionScriptTypeChecker(JSProblemReporter<Annotation> reporter) {
    myReporter = reporter;
  }

  @Override
  public Annotation registerProblem(PsiElement place,
                                    String message,
                                    @Nullable ProblemHighlightType highlightType,
                                    LocalQuickFix... fixes) {
    return myReporter
      .registerProblem(place, message, highlightType, getValidateTypesInspectionId(), fixes);
  }

  @Override
  public void checkExpressionIsAssignableToVariable(JSVariable p,
                                                    final JSExpression expr,
                                                    PsiFile containingFile,
                                                    @PropertyKey(resourceBundle = JSBundle.BUNDLE) String problemKey,
                                                    boolean allowChangeVariableTypeFix) {
    final JSType type = p.getType();
    Pair<Annotation, String> annotationAndExprType =
      checkExpressionIsAssignableToType(expr, type, problemKey,
                                        allowChangeVariableTypeFix ? p : null);

    if (annotationAndExprType != null &&
        p.getParent() instanceof JSParameterList &&
        expr.getParent() instanceof JSArgumentList &&
        !JSCommonTypeNames.VOID_TYPE_NAME.equals(annotationAndExprType.second)) {
      JSFunction method = (JSFunction)p.getParent().getParent();
      JSFunction topMethod = JSInheritanceUtil.findTopMethods(method).iterator().next();
      annotationAndExprType.first.registerFix(new ChangeSignatureFix(topMethod, ((JSArgumentList)expr.getParent()).getArguments()));
    }

    PsiElement _fun;
    if (annotationAndExprType == null &&
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
        JSParameter[] parameters = parameterList.getParameters();
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

  private static boolean isAddEventListenerMethod(final JSFunction method) {
    if ("addEventListener".equals(method.getName())) {
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

}
