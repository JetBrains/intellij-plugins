// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.javascript.completion.JSCompletionUtil;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSVariantsProcessorMerger;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.resolve.accessibility.JSPropertyAccessorChecker;
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor;
import com.intellij.lang.javascript.refactoring.JSNamesValidation;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.IntRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ActionScriptVariantsProcessor extends ActionScriptBaseJSSymbolProcessor implements JSCompletionProcessor {

  private final Set<String> myPopulatedResultsNames = new HashSet<>();

  private final boolean myProcessOnlyTypes;
  private final JSCompletionPlaceFilter myPlaceFilter;

  private final @Nullable String myReferencedParameterName;
  private final @Nullable PsiElement myOriginalElement;

  private final PrefixMatcher myPrefixMatcher;

  private final @NotNull JSVariantsProcessorMerger myVariantsProcessorMerger;

  public ActionScriptVariantsProcessor(@NotNull PsiFile targetFile,
                                       final @NotNull PsiElement context,
                                       @NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet) {
    super(targetFile.getOriginalFile(), context);
    myPrefixMatcher = resultSet.getPrefixMatcher();
    int limit = JSCompletionUtil.getCompletionLimit();
    boolean isExtendedCompletion = parameters.isExtendedCompletion();
    myVariantsProcessorMerger = new JSVariantsProcessorMerger(context, (element, priority) -> {
      return true;
    }, limit, !isExtendedCompletion, parameters);
    boolean processOnlyTypes = false;
    myCurrentFile = targetFile.getOriginalFile() == targetFile ? null : targetFile.getOriginalFile().getVirtualFile();

    final JSClass jsClass = PsiTreeUtil.getParentOfType(context, JSClass.class);

    String referencedParameterName = null;
    if (context instanceof JSReferenceExpression refExpr) {
      final PsiElement parent = refExpr.getParent();
      if (parent instanceof JSArgumentList) {
        JSParameterItem parameter = JSResolveUtil.findParameterForUsedArgument(refExpr, (JSArgumentList)parent);
        if (parameter != null) referencedParameterName = parameter.getName();
      }
      JSExpression qualifier = refExpr.getQualifier();

      if (qualifier != null) {
        JSExpression originalQualifier = getOriginalQualifier(qualifier);
        final CompletionTypeProcessor processor = new CompletionTypeProcessor();

        processor.evaluateQualifier(qualifier, originalQualifier);
      }
      else if (JSResolveUtil.isExprInTypeContext(refExpr) ||
               JSResolveUtil.isInPlaceWhereTypeCanBeDuringCompletion(refExpr)) {
        forceSetAddOnlyCompleteMatches();
        processOnlyTypes = true;
        addPackageScope(jsClass, refExpr);
      }
      else if (JSPsiImplUtils.getWithStatementContexts(refExpr).isEmpty()) {
        forceSetAddOnlyCompleteMatches();
      }
    }

    this.myProcessOnlyTypes = processOnlyTypes;
    myPlaceFilter = JSCompletionPlaceFilter.forPlace(context);

    if (myTypeInfo.typeWasProcessed() &&
        !myTypeInfo.isGlobalContext()) {
      myTypeInfo.addBaseObjectType();
    }

    myOriginalElement = CompletionUtil.getOriginalElement(context);
    myReferencedParameterName = referencedParameterName;
  }

  public void addPushedVariants(@NotNull Set<String> pushedVariants) {
    myPopulatedResultsNames.addAll(pushedVariants);
  }

  public void populateCompletionList(Collection<? extends LookupElement> resultsAsObjects,
                                     @NotNull CompletionResultSet resultSet) {
    if (resultsAsObjects == null) return;

    Collection<String> visited = new HashSet<>();
    ArrayList<LookupElement> batch = new ArrayList<>(resultsAsObjects.size());
    for (LookupElement o : resultsAsObjects) {
      String text = o.getLookupString();
      if (myPopulatedResultsNames.contains(text)) continue;
      visited.add(text);
      batch.add(o);
    }

    resultSet.addAllElements(batch);

    myPopulatedResultsNames.addAll(visited);
  }

  private class CompletionTypeProcessor extends JSTypeProcessorBase {

    @Override
    public void process(@NotNull JSType type, @NotNull JSEvaluateContext evaluateContext) {
      processCandidate(type);
    }

    @Override
    protected boolean isMakeAddedNamespaceStrict() {
      return true;
    }
  }

  public @NotNull Collection<LookupElement> getCurrentResults() {
    return myVariantsProcessorMerger.values();
  }

  public @NotNull Collection<LookupElement> getFinalResults() {
    return myVariantsProcessorMerger.values();
  }

  @Override
  public String getRequiredName() {
    return null;
  }

  @Override
  public boolean doAdd(final @NotNull JSPsiElementBase element) {
    return doAdd(element, null);
  }

  public boolean doAdd(final @NotNull JSPsiElementBase element, @Nullable JSTypeSubstitutor typeSubstitutor) {
    ProgressManager.checkCanceled();
    final String name = element.getName();
    if (name == null ||
        !JSNamesValidation.isUnqualifiedType(name) ||
        !myPrefixMatcher.prefixMatches(name) ||
        !myPlaceFilter.isAcceptable(element)) {
      return true;
    }

    JSAttributeList.AccessType accessType = element.getAccessType();

    if (isOriginalElement(element)) return true;
    if (myPopulatedResultsNames.contains(name)) return true;

    if (JSPropertyAccessorChecker.getPropertyAccessError(myOriginalElement, element, true) != null) {
      return true;
    }

    final boolean isClassMember = element.getContext() instanceof JSClass;
    if (isClassMember && JSResolveUtil.isConstructorFunction(element)) {
      return true;
    }

    if (isClassMember && accessType == JSAttributeList.AccessType.PRIVATE) {
      return true;
    }

    final IntRef typeHierarchyLevel = new IntRef(-1);
    final MatchType matchType = calcMatchType(element, typeHierarchyLevel);

    if (matchType == MatchType.PARTIAL) {
      return addPartialMatch(element, name);
    }
    else if (isCompleteOrWithContextMatchType(matchType)) {
      return addCompleteMatch(element, name, matchType, typeHierarchyLevel.get(), typeSubstitutor);
    }
    return true;
  }

  protected boolean isOriginalElement(@NotNull JSPsiElementBase element) {
    if (element.isEquivalentTo(myOriginalElement)) return true;
    if (myOriginalElement instanceof JSReferenceExpression) {
      PsiElement parent = myOriginalElement.getParent();
      if (parent instanceof JSDefinitionExpression && element.isEquivalentTo(parent)) {
        return true;
      }
    }
    return false;
  }

  private @NotNull MatchType calcMatchType(final @NotNull JSPsiElementBase element,
                                           @NotNull IntRef typeHierarchyLevel) {
    MatchType matchType = isAcceptableQualifiedItem(element, typeHierarchyLevel);

    return matchType;
  }

  private boolean addCompleteMatch(final @NotNull PsiElement element,
                                   final @Nullable String name,
                                   @NotNull MatchType matchType,
                                   int nestingLevel,
                                   @Nullable JSTypeSubstitutor typeSubstitutor) {
    if (name == null) return true;

    final JSLookupPriority priority = getPriority(name, matchType, nestingLevel);

    return myVariantsProcessorMerger.addResult(element, name, priority, toBaseMatchType(matchType), typeSubstitutor);
  }

  private JSLookupPriority getPriority(@NotNull String name, @NotNull MatchType matchType, int nestingLevel) {
    if (myTypeInfo.isGlobalContext()) {
      nestingLevel += 5;// prefer local results
    }
    return myTypeInfo.isGlobalContext() && !myProcessOnlyTypes
           ? JSLookupPriority.getLookupPriority(BaseJSSymbolProcessor.MatchType.PARTIAL, false, false)
           : JSLookupPriority.getSameTypeValue(name.equals(myReferencedParameterName), toBaseMatchType(matchType), nestingLevel);
  }

  private static BaseJSSymbolProcessor.MatchType toBaseMatchType(MatchType matchType) {
    return switch (matchType) {
      case COMPLETE_WITH_CONTEXT -> BaseJSSymbolProcessor.MatchType.COMPLETE_WITH_CONTEXT;
      case COMPLETE -> BaseJSSymbolProcessor.MatchType.COMPLETE;
      case PARTIAL -> BaseJSSymbolProcessor.MatchType.PARTIAL;
      case NOMATCH -> BaseJSSymbolProcessor.MatchType.NOMATCH;
    };
  }

  private boolean addPartialMatch(final @NotNull PsiElement element, @Nullable String name) {
    if (name == null || addOnlyCompleteMatches()) return true;


    final boolean fromRelevantFileOrDirectory = isFromRelevantFileOrDirectory();
    JSLookupPriority priority = JSLookupPriority.getLookupPriority(null, fromRelevantFileOrDirectory, false);

    return myVariantsProcessorMerger.addResult(element, name, priority, BaseJSSymbolProcessor.MatchType.PARTIAL, null);
  }

  @Override
  public @NotNull PsiFile getTargetFile() {
    return myTargetFile;
  }

  @Override
  public @NotNull PrefixMatcher getPrefixMatcher() {
    return myPrefixMatcher;
  }
}
