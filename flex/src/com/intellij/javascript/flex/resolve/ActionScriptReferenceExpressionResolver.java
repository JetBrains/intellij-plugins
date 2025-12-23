// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.build.JSConditionalCompilationDefinitionsProviderImpl;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.e4x.JSE4XNamespaceReference;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptReferenceExpressionResolver
  implements ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {

  protected final @NotNull JSReferenceExpressionImpl myRef;
  protected final PsiElement myParent;
  protected final @NotNull PsiFile myContainingFile;
  protected final @Nullable String myReferencedName;
  protected final JSExpression myQualifier;
  protected final boolean myIgnorePerformanceLimits;
  private final boolean myUnqualifiedOrLocalResolve;

  public ActionScriptReferenceExpressionResolver(JSReferenceExpressionImpl expression, boolean ignorePerformanceLimits) {
    myRef = expression;
    myContainingFile = expression.getContainingFile();
    myReferencedName = adjustReferencedName(this.myRef);
    myParent = this.myRef.getParent();
    myQualifier = this.myRef.getQualifier();
    myIgnorePerformanceLimits = ignorePerformanceLimits;
    myUnqualifiedOrLocalResolve =
      myQualifier == null || myQualifier instanceof JSThisExpression || myQualifier instanceof JSSuperExpression;
  }

  @Override
  public ResolveResult @NotNull [] resolve(@NotNull JSReferenceExpressionImpl expression, boolean incompleteCode) {
    if (myReferencedName == null) return ResolveResult.EMPTY_ARRAY;

    PsiElement currentParent = JSResolveUtil.getTopReferenceParent(myParent);
    if (JSResolveUtil.isSelfReference(currentParent, myRef)) {
      if (!(currentParent instanceof JSPackageStatement) || myParent == currentParent) {
        return new ResolveResult[] { new JSResolveResult( currentParent) };
      }
    }

    if (isConditionalVariableReference(currentParent, myRef)) {
      if (ModuleUtilCore.findModuleForPsiElement(myRef) == null) {
        // do not red highlight conditional compiler definitions in 3rd party library/SDK source code
        return new ResolveResult[]{new JSResolveResult(myRef)};
      }
      else {
        return resolveConditionalCompilationVariable(myRef);
      }
    }

    if (myRef.isAttributeReference()) {
      return dummyResult(myRef);
    }

    if(JSCommonTypeNames.ANY_TYPE.equals(myReferencedName)) {
      if (currentParent instanceof JSImportStatement && myQualifier instanceof JSReferenceExpression)
        return ((JSReferenceExpression)myQualifier).multiResolve(false);
      if (myParent instanceof JSE4XNamespaceReference) return dummyResult(myRef);
    }

    // nonqualified items in implements list in mxml
    if (myParent instanceof JSReferenceListMember &&
        myParent.getParent().getNode().getElementType() == JSElementTypes.IMPLEMENTS_LIST &&
        myRef.getQualifier() == null &&
        myContainingFile instanceof JSFile &&
        XmlBackedJSClassImpl.isImplementsAttribute((JSFile)myContainingFile)) {
      PsiElement byQName = ActionScriptClassResolver.findClassByQNameStatic(myRef.getText(), myRef);
      // for some reason Flex compiler allows to implement non-public interface in default package, so let's not check access type here
      if (byQName != null) return new ResolveResult[] {new JSResolveResult(byQName)};
      return ResolveResult.EMPTY_ARRAY;
    }

    ResolveResultSink resultSink = new ResolveResultSink(myRef, myReferencedName);
    ActionScriptSinkResolveProcessor<ResolveResultSink> localProcessor;
    if (myUnqualifiedOrLocalResolve) {
      final PsiElement topParent = JSResolveUtil.getTopReferenceParent(myParent);
      localProcessor = new ActionScriptSinkResolveProcessor<>(myReferencedName, myRef, resultSink) {
        @Override
        public boolean needPackages() {
          if (myParent instanceof JSReferenceExpression && topParent instanceof JSImportStatement) {
            return true;
          }
          return super.needPackages();
        }
      };

      localProcessor.setToProcessHierarchy(true);
      JSReferenceExpressionImpl.doProcessLocalDeclarations(myRef, myQualifier, localProcessor, true, false, null);

      PsiElement jsElement = localProcessor.getResult();
      if (myQualifier instanceof JSThisExpression &&
          localProcessor.processingEncounteredAnyTypeAccess() &&
          jsElement != null  // this is from ecma script closure, proceed it in JavaScript way
        ) {
        localProcessor.getResults().clear();
        jsElement = null;
      }

      if (myQualifier == null) {
        final JSReferenceExpression namespaceReference = JSReferenceExpressionImpl.getNamespaceReference(myRef);
        ResolveResult[] resolveResultsAsConditionalCompilationVariable = null;

        if (namespaceReference != null && (namespaceReference == myRef || namespaceReference.resolve() == namespaceReference)) {
          if (jsElement == null && ModuleUtilCore.findModuleForPsiElement(myRef) == null) {
            // do not red highlight conditional compiler definitions in 3rd party library/SDK source code
            return new ResolveResult[]{new JSResolveResult(myRef)};
          }

          resolveResultsAsConditionalCompilationVariable = resolveConditionalCompilationVariable(myRef);
        }

        if (resolveResultsAsConditionalCompilationVariable != null &&
            resolveResultsAsConditionalCompilationVariable.length > 0 &&
            (jsElement == null || resolveResultsAsConditionalCompilationVariable[0].isValidResult())) {
          return resolveResultsAsConditionalCompilationVariable;
        }
      }

      if (jsElement != null ||
          localProcessor.isEncounteredDynamicClasses() && myQualifier == null ||
          !localProcessor.processingEncounteredAnyTypeAccess() && !localProcessor.isEncounteredDynamicClasses()
        ) {
        return localProcessor.getResultsAsResolveResults();
      }
    }
    else {
      ActionScriptQualifiedItemProcessor<ResolveResultSink> processor =
        new ActionScriptQualifiedItemProcessor<>(resultSink);
      processor.setTypeContext(JSResolveUtil.isExprInTypeContext(myRef));
      JSResolveUtil.evaluateQualifierType(myQualifier, myContainingFile, processor);

      if (processor.resolved == QualifiedItemProcessor.TypeResolveState.PrefixUnknown) {
        return dummyResult(myRef);
      }

      if (processor.resolved.isSuitableForReferenceResolve() ||
          processor.getResult() != null
        ) {
        return processor.getResultsAsResolveResults();
      } else {
        localProcessor = processor;
      }
    }

    ResolveResult[] results = resolveFromIndices(localProcessor);

    if (results.length == 0 && localProcessor.isEncounteredXmlLiteral()) {
      return dummyResult(myRef);
    }

    return results;
  }

  protected boolean prepareProcessor(ActionScriptWalkUpResolveProcessor processor, @NotNull JSSinkResolveProcessor localProcessor) {
    boolean allowOnlyCompleteMatches = false;

    PsiElement context = processor.getContext();
    if (context instanceof JSReferenceExpression refExpr) {
      boolean haveEncounteredDynamics = false;
      final JSExpression originalQualifier = refExpr.getQualifier();
      final JSExpression qualifier = ActionScriptResolveUtil.getRealRefExprQualifier(refExpr);
      if (originalQualifier == null && qualifier != null && refExpr.isAttributeReference()) {
        haveEncounteredDynamics = true;
      }
      else if (qualifier instanceof JSThisExpression) {
        final JSNamespace ns = JSContextResolver.resolveContext(qualifier);
        final String contextQualifierText = JSNamespace.getQualifiedName(ns);
        final PsiElement clazz = contextQualifierText == null ? null :
                                 JSClassResolver.findClassFromNamespace(contextQualifierText, context);
        if (clazz instanceof JSClass && JSPsiImplUtils.hasModifier((JSClass)clazz, JSAttributeList.ModifierType.DYNAMIC)) {
          haveEncounteredDynamics = true;
        }
      }

      allowOnlyCompleteMatches = processor.getTypeInfo().isEmpty() || !haveEncounteredDynamics;
    }

    boolean inDefinition = false;
    allowOnlyCompleteMatches |= myUnqualifiedOrLocalResolve && localProcessor.isEncounteredDynamicClasses();

    if (myParent instanceof JSDefinitionExpression) {
      inDefinition = true;
      allowOnlyCompleteMatches = !(myUnqualifiedOrLocalResolve && localProcessor.processingEncounteredAnyTypeAccess());
    }
    else if (myQualifier instanceof JSThisExpression && localProcessor.processingEncounteredAnyTypeAccess()) {
      processor.allowPartialResults();
    }

    processor.setAddOnlyCompleteMatches();
    if (!allowOnlyCompleteMatches) {
      processor.allowPartialResults();
    }
    processor.setSkipDefinitions(inDefinition);
    return true;
  }

  protected ResolveResult[] getResultsForDefinition() {
    return new ResolveResult[] { new JSResolveResult(myParent) };
  }

  protected ResolveResult[] resolveFromIndices(@NotNull JSSinkResolveProcessor localProcessor) {
    assert myReferencedName != null;
    final ActionScriptWalkUpResolveProcessor processor = new ActionScriptWalkUpResolveProcessor(myReferencedName, myContainingFile, myRef);

    if (prepareProcessor(processor, localProcessor)) {
      JSResolveUtil.tryProcessAllElementsInInjectedContext(myContainingFile, element -> {
        if (myReferencedName.equals(element.getName())) {
          processor.doQualifiedCheck(element);
        }
        return true;
      });

      processAllSymbols(processor, myIgnorePerformanceLimits);
    }

    ResolveResult[] results = getResultsFromProcessor(processor);
    if (results.length == 0 && myParent instanceof JSDefinitionExpression) {
      return getResultsForDefinition();
    }

    return myIgnorePerformanceLimits || results.length <= JSReferenceExpressionResolver.MAX_RESULTS_COUNT_TO_KEEP ? results : JSResolveResult.tooManyCandidatesResult();
  }

  private static void processAllSymbols(@NotNull ActionScriptWalkUpResolveProcessor processor, boolean ignorePerformanceLimit) {
    final String name = processor.getRequiredName();
    GlobalSearchScope scope = JSResolveUtil.getResolveScope(processor.getContext());
    CommonProcessors.CollectProcessor<JSPsiElementBase> collector = new CommonProcessors.CollectProcessor<>();
    for (ActionScriptContextLevel level : processor.getTypeInfo().myContextLevels) {
      JSNamespace namespace = level.myNamespace;
      String qName = JSIndexBasedResolveUtil.getQualifiedNameToIndex(name, namespace, true);
      JSClassResolver.IncludeLocalMembersOptions includeLocalMembers =
        JSClassResolver.IncludeLocalMembersOptions.NONE;
      JSClassResolver.getInstance().processElementsByQNameIncludingImplicit(qName, scope, includeLocalMembers, collector);
    }

    List<ActionScriptTaggedResolveResult> resultsWithCompleteMatches = processor.getTaggedResolveResults();
    boolean hasValidResult = false;
    for (ActionScriptTaggedResolveResult completeMatchResult : resultsWithCompleteMatches) {
      if (completeMatchResult.result.isValidResult() && !completeMatchResult.hasTag(ActionScriptTaggedResolveResult.ResolveResultTag.PARTIAL)) {
        hasValidResult = true;
        break;
      }
    }

    if (!hasValidResult && !processor.addOnlyCompleteMatches()) {
      final StubIndexKey<String, JSElement> indexKey = JSIndexKeys.JS_SYMBOL_INDEX_2_KEY;
      JSClassResolver.processElementsByNameIncludingImplicit(processor.getRequiredName(), scope, false, indexKey, collector);
    }
    for (JSPsiElementBase result : collector.getResults()) {
      processor.doQualifiedCheck(result);
    }
  }

  protected ResolveResult[] getResultsFromProcessor(ActionScriptWalkUpResolveProcessor processor) {
    return processor.getResults();
  }

  protected @Nullable String adjustReferencedName(@NotNull JSReferenceExpression ref) {
    return ref.getReferenceName();
  }

  protected ResolveResult[] dummyResult(JSReferenceExpression expression) {
    return new ResolveResult[]{new JSResolveResult(expression)};
  }

  @Override
  public String toString() {
    String simpleName = this.getClass().getSimpleName();
    return simpleName + "{" +
           "myQualifier=" + myQualifier +
           ", myRef=" + myRef +
           '}';
  }

  private static boolean isConditionalVariableReference(PsiElement currentParent, JSReferenceExpressionImpl thisElement) {
    if(currentParent instanceof JSConditionalCompileVariableReference) {
      return JSReferenceExpressionImpl.getNamespaceReference(thisElement) != null;
    }
    return false;
  }


  private static ResolveResult[] resolveConditionalCompilationVariable(final JSReferenceExpression jsReferenceExpression) {
    final String namespace;
    final String constantName;

    final PsiElement parent = jsReferenceExpression.getParent();
    if (parent instanceof JSE4XNamespaceReference) {
      final PsiElement namespaceReference = ((JSE4XNamespaceReference)parent).getNamespaceReference();
      final PsiElement parentParent = parent.getParent();
      PsiElement sibling = parent.getNextSibling();
      while (sibling instanceof PsiWhiteSpace) {
        sibling = sibling.getNextSibling();
      }
      if (namespaceReference != null &&
          parentParent instanceof JSReferenceExpression &&
          sibling != null &&
          sibling.getNextSibling() == null &&
          sibling.getNode() != null &&
          sibling.getNode().getElementType() == JSTokenTypes.IDENTIFIER) {
        namespace = namespaceReference.getText();
        constantName = sibling.getText();
      }
      else {
        return new ResolveResult[]{new JSResolveResult(jsReferenceExpression, null, JSResolveResult.ProblemKind.UNRESOLVED_SYMBOL)};
      }
    }
    else {
      final JSE4XNamespaceReference namespaceElement = PsiTreeUtil.getChildOfType(jsReferenceExpression, JSE4XNamespaceReference.class);
      final PsiElement namespaceReference = namespaceElement == null ? null : namespaceElement.getNamespaceReference();
      PsiElement sibling = namespaceElement == null ? null : namespaceElement.getNextSibling();
      while (sibling instanceof PsiWhiteSpace) {
        sibling = sibling.getNextSibling();
      }

      if (namespaceElement != null &&
          sibling != null &&
          sibling.getNextSibling() == null &&
          sibling.getNode() != null &&
          sibling.getNode().getElementType() == JSTokenTypes.IDENTIFIER) {
        namespace = namespaceReference.getText();
        constantName = sibling.getText();
      }
      else {
        return new ResolveResult[]{new JSResolveResult(jsReferenceExpression, null, JSResolveResult.ProblemKind.UNRESOLVED_SYMBOL)};
      }
    }

    final Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(jsReferenceExpression);
    JSConditionalCompilationDefinitionsProviderImpl provider =
      ApplicationManager.getApplication().getService(JSConditionalCompilationDefinitionsProviderImpl.class);
    if (provider.containsConstant(moduleForPsiElement, namespace, constantName)) {
      return new ResolveResult[]{new JSResolveResult(jsReferenceExpression)};
    }

    return new ResolveResult[]{new JSResolveResult(jsReferenceExpression, null, JSResolveResult.ProblemKind.UNRESOLVED_SYMBOL)};
  }
}
