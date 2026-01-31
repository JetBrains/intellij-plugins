// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSField;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression;
import com.intellij.lang.javascript.psi.impl.JSUseScopeProvider;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSSinkResolveProcessor;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.psi.resolve.ResolveResultSink;
import com.intellij.lang.javascript.psi.resolve.ResultSink;
import com.intellij.lang.javascript.psi.resolve.accessibility.JSPropertyAccessorChecker;
import com.intellij.lang.javascript.psi.resolve.processors.JSCandidateResultProcessor;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.lang.javascript.DialectOptionHolder.OTHER;

public class ActionScriptSinkResolveProcessor<T extends ResultSink> extends ActionScriptResolveProcessor
  implements JSSinkResolveProcessor, JSCandidateResultProcessor {

  private final boolean myToStopOnAssignment;
  private final @NotNull T myResultSink;
  private PsiElement placeTopParent;

  //ActionScript only
  private String myPackageName;
  private boolean myForcedPackage;
  private boolean encounteredFunctionExpression;

  private boolean encounteredXmlLiteral;

  public ActionScriptSinkResolveProcessor(@NotNull T sink) {
    this(sink.getName(), sink.place, sink);
  }

  public ActionScriptSinkResolveProcessor(String name, T sink) {
    this(name, null, sink);
  }

  public ActionScriptSinkResolveProcessor(String name, boolean toStopOnAssignment, @NotNull T sink) {
    this(name, null, toStopOnAssignment, sink);
  }

  public ActionScriptSinkResolveProcessor(String name, PsiElement _place, @NotNull T sink) {
    this(name, _place, false, sink);
  }

  public ActionScriptSinkResolveProcessor(String name, PsiElement _place, boolean stopOnAssignment, @NotNull T sink) {
    super(name, _place);
    myResultSink = sink;
    myToStopOnAssignment = stopOnAssignment;
  }

  public PsiElement getResult() {
    return myResultSink.getResult();
  }

  @Override
  public @Nullable List<PsiElement> getResults() {
    return myResultSink.getResults();
  }

  public PsiElement getPlaceTopParent() {
    return placeTopParent;
  }

  public void setForcedPackageName(String forcedPackageName) {
    myPackageName = forcedPackageName;
    myForcedPackage = forcedPackageName != null;
  }

  @Override
  public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
    if (!myResultSink.accepts(element)) return true;
    if (!accessibilityProcessingHandler.accepts(element, this)) return true;

    return executeAcceptedElement(element, state);
  }

  protected boolean executeAcceptedElement(@NotNull PsiElement element, @NotNull ResolveState state) {
    JSResolveResult.ProblemKind propertyAccessError = JSPropertyAccessorChecker.getPropertyAccessError(place, element, myName == null);
    if (propertyAccessError != null) {
      myResultSink.addCandidateResult(element, false, propertyAccessError);
      return true;
    }

    if (isPlaceConstructorCall()) {
      if (element instanceof JSClass && myName != null) {
        PsiElement constructor = getConstructor(element);
        if (constructor == null) return true;


        if (myResultSink.isActionScript()) {
          element = constructor;
        }
      }
      else if (JSResolveUtil.isConstructorFunction(element) && myName == null) {
        return true;
      }
    }
    else if (isElementClassConstructor(element) && !(place instanceof JSClass)) {
      return true;
    }

    if (placeTopParent == null && place != null) {
      placeTopParent = JSResolveUtil.getTopReferenceExpression(place);
    }

    if (element instanceof JSFunctionExpression) {
      PsiElement ownNameIdentifier = ((JSFunctionExpression)element).getOwnNameIdentifier();
      if (ownNameIdentifier == null) {
        return true;
      }
    }

    boolean addCandidateUnconditionally = false;
    if (element instanceof JSDefinitionExpression && isWalkingUpTree) {
      boolean toProcess = false;
      final JSExpression expression = ((JSDefinitionExpression)element).getExpression();

      // TODO: fix this hack
      if (!accessibilityProcessingHandler.isProcessStatics() && expression instanceof JSReferenceExpression &&
          ((JSReferenceExpression)expression).getQualifier() == null &&
          !myResultSink.isActionScript() &&
          (myToStopOnAssignment || !(JSResolveUtil.isEcmaScript5(element) && DialectDetector.dialectOfElement(element) != OTHER))
      ) {
        toProcess = true;
      }

      if (!toProcess) return true;
      addCandidateUnconditionally = !myToStopOnAssignment;
    }

    if (isField(element) && !isToProcessMembers()) {
      return true;
    }

    if (resolvedVariableShadowsParameter(element)) {
      return true;
    }

    if (element instanceof PsiNamedElement) {
      element = getNameElementIfElementIsXmlTag(element);

      if (addCandidateUnconditionally) {
        addPossibleCandidateResult(element, null);
      }
      else {
        if (!checkAccessibleInActionScript(element)) {
          return true;
        }

        return myResultSink.addResult(element, state, getPlaceTopParent());
      }
    }
    else if (element instanceof ES6ExportDefaultAssignment) {
      return myResultSink.addResult(element, state, getPlaceTopParent());
    }

    return true;
  }

  protected boolean checkAccessibleInActionScript(@NotNull PsiElement element) {
    if (!myResultSink.isActionScript()) return true;
    if (myPackageName == null) {
      myPackageName = place != null ? JSResolveUtil.getPackageNameFromPlace(place) : "";
    }
    if (myForcedPackage ? !ActionScriptResolveUtil.isAccessibleFromActionScriptPackage(element, myPackageName) :
        !ActionScriptResolveUtil.isAccessibleFromCurrentActionScriptPackage(element, myPackageName, place)) {
      elementIsNotAccessible(element);
      return false;
    }
    return true;
  }

  private boolean resolvedVariableShadowsParameter(@NotNull PsiElement element) {
    // for JSResolveTest.testResolveInFunc, WEB-2580
    if (!(element instanceof JSVariable) || !(place instanceof JSReferenceExpression)) return false;
    JSVarStatement varStatement = ((JSVariable)element).getStatement();
    if (varStatement == null || varStatement.getVarKeyword() != JSVarStatement.VarKeyword.VAR) return false;
    if (((JSReferenceExpression)place).getQualifier() != null) return false;
    if (!PsiTreeUtil.isAncestor(element, place, true)) return false;
    PsiElement lexicalScope = JSUseScopeProvider.getLexicalScopeOrFile(varStatement);
    if (!(lexicalScope instanceof JSFunction)) return false;
    JSParameter[] parameters = ((JSFunction)lexicalScope).getParameterVariables();
    return myName != null && ContainerUtil.find(parameters, p -> myName.equals(p.getName())) != null;
  }

  private static boolean isField(@Nullable PsiElement element) {
    return element instanceof JSField &&
           (!DialectDetector.isActionScript(element) || JSUtils.getMemberContainingClass(element) != null);
  }

  protected @Nullable PsiElement getConstructor(@NotNull PsiElement element) {
    PsiElement constructor  = ((JSClass)element).getConstructor();
    return constructor == null ? element : constructor;
  }

  protected boolean isElementClassConstructor(@NotNull PsiElement element) {
    return element instanceof JSFunction &&
           ((JSFunction)element).isConstructor() &&
           JSUtils.getMemberContainingClass(element) != null;
  }

  protected boolean isPlaceConstructorCall() {
    return place != null &&
           (completeConstructorName(place) || place instanceof JSSuperExpression);
  }

  public void addPossibleCandidateResult(PsiElement element, JSResolveResult.ProblemKind problemKind) {
    myResultSink.addCandidateResult(element, false, problemKind);
  }

  @Override
  public void executeCandidateResult(PsiElement element, JSResolveResult.ProblemKind problemKind) {
    if (myResultSink.accepts(element)) {
      myResultSink.addCandidateResult(element, false, problemKind);
    }
  }

  protected void elementIsNotAccessible(PsiElement element) {
    addPossibleCandidateResult(element, JSResolveResult.ProblemKind.ELEMENT_IS_NOT_ACCESSIBLE);
  }

  @Override
  protected void startingParent(PsiElement associated) {
    myResultSink.startingParent(associated, isToProcessMembers());
    super.startingParent(associated);
  }

  private static PsiElement getNameElementIfElementIsXmlTag(final PsiElement element) {
    if (element instanceof XmlTag) {
      return ((XmlTag)element).getAttribute("name").getValueElement().getChildren()[1];
    }
    return element;
  }

  public ResolveResult @NotNull [] getResultsAsResolveResults() {
    return ((ResolveResultSink)myResultSink).getResultsAsResolveResults();
  }

  public void addResult(@NotNull PsiElement element) {
    myResultSink.addResult(element, ResolveState.initial(), placeTopParent);
  }

  @Override
  public final @NotNull T getResultSink() {
    return myResultSink;
  }

  @Override
  public boolean skipTopLevelItems() {
    return myResultSink.skipTopLevelItems();
  }

  /**
   * ActionScript only
   */
  @Override
  public boolean processingEncounteredAnyTypeAccess() {
    return encounteredFunctionExpression || encounteredXmlLiteral;
  }

  public boolean isEncounteredXmlLiteral() {
    return encounteredXmlLiteral;
  }

  public void setEncounteredXmlLiteral(boolean b) {
    encounteredXmlLiteral = b;
  }

  @Override
  public void handleEvent(@NotNull Event event, Object associated) {
    super.handleEvent(event, associated);

    handleActionScriptFunctionExpressionEvents(event, associated);
  }

  protected void handleActionScriptFunctionExpressionEvents(@NotNull Event event, Object associated) {
    if (!getResultSink().isActionScript()) {
      return;
    }

    if (event == Event.SET_DECLARATION_HOLDER) {
      if (associated instanceof JSFunctionExpression) {
        if (!ActionScriptResolveUtil.isAnonymousEventHandler((JSFunctionExpression)associated)) {
          encounteredFunctionExpression = true; // we create anonymous fun exprs for event handlers
        }
      }
    }
    else if (event == ResolveProcessor.SCOPE_CHANGE) {
      if (associated instanceof JSFunction &&
          place != null &&
          !(JSResolveUtil.findParent((PsiElement)associated) instanceof JSClass) &&
          PsiTreeUtil.isAncestor((JSFunction)associated, place, true)) {
        encounteredFunctionExpression = true;
      }
    }
  }

  @Override
  public boolean needTopLevelClassName(String name) {
    return myResultSink.needTopLevelClassName(name);
  }
}
