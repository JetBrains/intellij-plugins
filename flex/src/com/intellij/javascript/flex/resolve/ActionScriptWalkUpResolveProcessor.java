// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.resolve.accessibility.JSPropertyAccessorChecker;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.openapi.util.IntRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.intellij.lang.javascript.index.JSTypeEvaluateManager.iterateTypeHierarchy;
import static com.intellij.lang.javascript.psi.resolve.JSResolveUtil.getOriginalFile;

/**
 * @author Maxim.Mossienko
 */
public class ActionScriptWalkUpResolveProcessor extends ActionScriptBaseJSSymbolProcessor {

  private final ActionScriptResolveMatchData matchData = new ActionScriptResolveMatchData();

  private boolean mySkipDefs;
  protected final @NotNull String myName;

  public ActionScriptWalkUpResolveProcessor(@NotNull String name,
                                             @NotNull PsiFile targetFile,
                                             final @NotNull PsiElement context) {
    super(targetFile, context);

    myName = name;

    if (context instanceof JSReferenceExpression refExpr) {
      final JSExpression originalQualifier = refExpr.getQualifier();
      final JSExpression qualifier = JSResolveUtil.getRealRefExprQualifier(refExpr);

      if (qualifier == null || originalQualifier == null) {
        if (qualifier == null) {
          final JSImportedElementResolveResult expression = JSImportHandlingUtil.resolveTypeNameUsingImports(refExpr);

          if (expression != null) {
            final JSQualifiedName qName =
              JSResolveUtil.buildNamespaceForQualifier(JSResolveUtil.getRealRefExprQualifierFromResult(refExpr, expression))
                .getQualifiedName();
            myTypeInfo.buildIndexListFromQNameAndCorrectQName(qName != null ? qName.getQualifiedName() : "");
          }
          else {
            final JSClass jsClass = PsiTreeUtil.getParentOfType(context, JSClass.class); // get rid of it!
            addPackageScope(jsClass, refExpr);
          }
        }
      }
      else { // if qualifier != null
        if (qualifier instanceof JSThisExpression || qualifier instanceof JSSuperExpression) {
          if (qualifier instanceof JSThisExpression) {
            final JSNamespace ns = JSContextResolver.resolveContext(qualifier);
            updateTypeInfoFromThis(ns);

            final String contextQualifierText = JSNamespace.getQualifiedName(ns);
            final PsiElement clazz = contextQualifierText == null ? null :
                                     JSClassResolver.findClassFromNamespace(contextQualifierText, context);

            if (clazz instanceof JSClass) {
              myTypeInfo.buildIndexListFromQNameAndCorrectQName(contextQualifierText, false, ns.getJSContext());
            }
            else if (contextQualifierText != null) {
              myTypeInfo.buildIndexListFromQNameAndCorrectQName(contextQualifierText, true, ns.getJSContext());
            }
          }
        }

        if (!(qualifier instanceof JSThisExpression) && !(qualifier instanceof JSSuperExpression)) {
          ActionScriptResolveTypeProcessor processor = new ActionScriptResolveTypeProcessor();
          processor.evaluateQualifier(qualifier, originalQualifier);
        }
      }
    }

    if (myTypeInfo.typeWasProcessed()) {
      myTypeInfo.addBaseObjectType();
    }
  }

  @Override
  protected @NotNull ActionScriptTypeInfo.GlobalStatusHint initGlobalStatusHint(@Nullable PsiElement context) {
    ActionScriptTypeInfo.GlobalStatusHint statusHint = super.initGlobalStatusHint(context);

    if (context instanceof JSReferenceExpression) {
      final JSElement container = PsiTreeUtil.getParentOfType(context, JSEmbeddedContent.class, JSFile.class);
      if (container != null &&
          (container.getContainingFile().getContext() != null || container.getContext() instanceof XmlAttributeValue) &&
          statusHint == ActionScriptTypeInfo.GlobalStatusHint.GLOBAL) {
        statusHint = ActionScriptTypeInfo.GlobalStatusHint.UNKNOWN;
      }
    }

    return statusHint;
  }

  public void doQualifiedCheck(final @NotNull JSPsiElementBase element) {
    IntRef matchLevel = new IntRef(-1);
    MatchType matchType = isAcceptableQualifiedItem(element, matchLevel);

    if (isCompleteOrWithContextMatchType(matchType)) {
      JSResolveResult.ProblemKind propertyAccessError = JSPropertyAccessorChecker.getPropertyAccessError(myContext, element, false);
      if (propertyAccessError != null) {
        addCompleteResultWithProblem(element, propertyAccessError);
        return;
      }
    }

    if (matchType == MatchType.PARTIAL) {
      addPartialResult(element);
    }
    else if (isCompleteOrWithContextMatchType(matchType)) {
      EnumSet<ActionScriptTaggedResolveResult.ResolveResultTag> tags =
        matchType == MatchType.COMPLETE_WITH_CONTEXT
        ? EnumSet.of(ActionScriptTaggedResolveResult.ResolveResultTag.CONTEXT_MATCHES)
        : EnumSet.noneOf(ActionScriptTaggedResolveResult.ResolveResultTag.class);
      matchData.addResult(new JSResolveResult(element), tags, matchLevel.get());
    }
  }

  private void addCompleteResultWithProblem(PsiElement element, JSResolveResult.ProblemKind problemKey) {
    matchData.addResult(new JSResolveResult(element, null, problemKey), EnumSet.noneOf(ActionScriptTaggedResolveResult.ResolveResultTag.class));
  }

  private void addPartialResult(PsiElement element) {
    final JSResolveResult o = new JSResolveResult(element, null, null);
    matchData.addResult(o, EnumSet.of(ActionScriptTaggedResolveResult.ResolveResultTag.PARTIAL));
  }

  @Override
  public @NotNull String getRequiredName() {
    return myName;
  }

  public ResolveResult[] getResults() {
    final List<ActionScriptTaggedResolveResult> taggedResolveResults = getTaggedResolveResults();

    if (taggedResolveResults.isEmpty()) return ResolveResult.EMPTY_ARRAY;

    final int resultCount = taggedResolveResults.size();
    final ResolveResult[] result = new ResolveResult[resultCount];
    for (int i = 0; i < resultCount; i++) {
      ActionScriptTaggedResolveResult taggedResolveResult = taggedResolveResults.get(i);
      ResolveResult resolveResult = taggedResolveResult.result;
      if (resolveResult instanceof JSResolveResult jsResolveResult) {
        if (taggedResolveResult.tags.contains(ActionScriptTaggedResolveResult.ResolveResultTag.PARTIAL)) {
          var status = jsResolveResult.getStatus();
          var partialStatus = status.copy(
            status.usedImportOrExport,
            status.myResolveProblem,
            status.getSubstitutor(),
            true
          );
          resolveResult = new JSResolveResult(jsResolveResult.getElement(), partialStatus);
        }
      }

      result[i] = resolveResult;
    }

    return result;
  }

  /**
   * Modifies state, throws out lower priority results, but can be safely invoked several times in a row.
   */
  public @NotNull List<ActionScriptTaggedResolveResult> getTaggedResolveResults() {
    return matchData.getTaggedResolveResults();
  }

  public void setSkipDefinitions(boolean b) {
    mySkipDefs = b;
  }

  private final class ActionScriptResolveMatchData {
    private @Nullable List<ActionScriptTaggedResolveResult> matchData = null;

    void addResult(ResolveResult o, @NotNull EnumSet<ActionScriptTaggedResolveResult.ResolveResultTag> tags) {
      addResult(o, tags, 0);
    }

    void addResult(ResolveResult o, @NotNull EnumSet<ActionScriptTaggedResolveResult.ResolveResultTag> tags, int completeMatchLevel) {
      final PsiElement element = o.getElement();

      if (element instanceof JSDefinitionExpression && mySkipDefs &&
          JSPsiImplUtils.isUnqualifiedAssignment((JSDefinitionExpression)element)) {
        return;
      }

      if (matchData == null) matchData = new SmartList<>();
      if (isFromRelevantFileOrDirectory()) {
        tags.add(ActionScriptTaggedResolveResult.ResolveResultTag.CURRENT_FILE);
      }

      addResolveResultTags(tags, element, myContext);

      matchData.add(new ActionScriptTaggedResolveResult(o, tags, completeMatchLevel));
    }


    List<ActionScriptTaggedResolveResult> getTaggedResolveResults() {
      if (matchData == null) return Collections.emptyList();

      Collections.sort(matchData);
      final ActionScriptTaggedResolveResult topResult = matchData.getFirst();
      final boolean topResultIsPartial = topResult.tags.contains(ActionScriptTaggedResolveResult.ResolveResultTag.PARTIAL);
      if (addOnlyCompleteMatches() && topResultIsPartial) {
        matchData = null;
        return Collections.emptyList();
      }

      int resultCount = 0;
      for (ActionScriptTaggedResolveResult taggedResolveResult : matchData) {
        if (!topResultIsPartial) {
          int compare = taggedResolveResult.comparePriorityTo(topResult);
          if (compare != 0) {
            assert compare > 0;
            break;
          }
        }
        resultCount++;
      }
      matchData = matchData.subList(0, resultCount);
      return matchData;
    }
  }

  private final class ActionScriptResolveTypeProcessor implements JSTypeProcessor {

    @Override
    public void processAdditionalType(@NotNull JSType type, @NotNull JSEvaluateContext evaluateContext) {
      process(type, evaluateContext);
    }

    @Override
    public void process(@NotNull JSType type, final @NotNull JSEvaluateContext context) {
      if (type instanceof JSAnyType) return; // match anything by default

      if (!(type instanceof JSNamespace)) return;

      boolean result = iterateTypeHierarchy(
        (JSNamespace)type,
        clazz -> {
          return !"flash.utils.Proxy".equals(clazz.getQualifiedName());
        },
        baseType -> {
          return !"Proxy".equals(JSTypeUtils.getQualifiedNameMatchingType(baseType, false));
        },
        myContext, false);

      if (!result) return; // allow Proxy descendant for any reference
      myTypeInfo.buildIndexListFromQNameAndCorrectQName(type.getTypeText());
    }

    public void evaluateQualifier(@NotNull JSExpression qualifier, @NotNull JSExpression originalQualifier) {
      JSCompleteTypeEvaluationProcessor.evaluateTypes(originalQualifier, getOriginalFile(originalQualifier), this, true);

      if (!addOnlyCompleteMatchesEvaluated() || myTypeInfo.isEmpty()) {
        final JSNamespace namespace = JSSymbolUtil.evaluateNamespaceLocally(qualifier);
        if (namespace != null) {
          myTypeInfo.addNamespace(namespace, true);
        }
      }
    }
  }
}
