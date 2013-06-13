package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.JSConditionalCompilationDefinitionsProvider;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.e4x.JSE4XNamespaceReference;
import com.intellij.lang.javascript.psi.ecmal4.JSConditionalCompileVariableReference;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.*;
import com.intellij.lang.javascript.psi.types.JSTypeParser;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptReferenceExpressionResolver extends JSReferenceExpressionResolver implements JSResolveUtil.Resolver<JSReferenceExpressionImpl> {
  public static final ActionScriptReferenceExpressionResolver INSTANCE = new ActionScriptReferenceExpressionResolver();

  private ActionScriptReferenceExpressionResolver() {
    super();
  }

  public ResolveResult[] doResolve(@NotNull final JSReferenceExpressionImpl ref, PsiFile containingFile) {
    String referencedName = ref.getReferencedName();
    if (referencedName == null) return ResolveResult.EMPTY_ARRAY;

    final PsiElement parent = ref.getParent();
    final JSExpression qualifier = ref.getResolveQualifier();
    final boolean localResolve = JSReferenceExpressionImpl.isLocalResolveQualifier(qualifier);

    PsiElement currentParent = JSResolveUtil.getTopReferenceParent(parent);
    if (JSResolveUtil.isSelfReference(currentParent, ref)) {
      if (!(currentParent instanceof JSPackageStatement) || parent == currentParent) {
        return new ResolveResult[] { new JSResolveResult( currentParent) };
      }
    }

    if (isConditionalVariableReference(currentParent, ref)) {
      if (ModuleUtilCore.findModuleForPsiElement(ref) == null) {
        // do not red highlight conditional compiler definitions in 3rd party library/SDK source code
        return new ResolveResult[]{new JSResolveResult(ref)};
      }
      else {
        return resolveConditionalCompilationVariable(ref);
      }
    }

    if (ref.isAttributeReference()) {
      return dummyResult(ref);
    }

    if(JSCommonTypeNames.ANY_TYPE.equals(referencedName)) {
      if (currentParent instanceof JSImportStatement && qualifier instanceof JSReferenceExpression)
        return ((JSReferenceExpression)qualifier).multiResolve(false);
      if (parent instanceof JSE4XNamespaceReference) return dummyResult(ref);
    }

    // nonqualified items in implements list in mxml
    if (parent instanceof JSReferenceList &&
        parent.getNode().getElementType() == JSElementTypes.IMPLEMENTS_LIST &&
        ref.getQualifier() == null &&
        containingFile instanceof JSFile &&
        XmlBackedJSClassImpl.isImplementsAttribute((JSFile)containingFile)) {
      PsiElement byQName = JSResolveUtil.findClassByQName(ref.getText(), ref);
      // for some reason Flex compiler allows to implement non-public interface in default package, so let's not check access type here
      if (byQName != null) return new ResolveResult[] {new JSResolveResult(byQName)};
      return ResolveResult.EMPTY_ARRAY;
    }

    ResolveProcessor localProcessor;
    final Ref<JSType> qualifierType = Ref.create(null);
    if (localResolve) {
      final PsiElement topParent = JSResolveUtil.getTopReferenceParent(parent);
      localProcessor = new ResolveProcessor(referencedName, ref) {
        @Override
        public boolean needPackages() {
          if (parent instanceof JSReferenceExpression && topParent instanceof JSImportStatement) {
            return true;
          }
          return super.needPackages();
        }
      };

      localProcessor.setToProcessHierarchy(true);
      JSReferenceExpressionImpl.doProcessLocalDeclarations(ref, qualifier, localProcessor, true, false, null);

      PsiElement jsElement = localProcessor.getResult();
      if (qualifier instanceof JSThisExpression &&
          localProcessor.processingEncounteredAnyTypeAccess() &&
          jsElement != null  // this is from ecma script closure, proceed it in JavaScript way
        ) {
        localProcessor.getResults().clear();
        jsElement = null;
      }

      if (qualifier == null) {
        final JSReferenceExpression namespaceReference = JSReferenceExpressionImpl.getNamespaceReference(ref);
        ResolveResult[] resolveResultsAsConditionalCompilationVariable = null;

        if (namespaceReference != null && (namespaceReference == ref || namespaceReference.resolve() == namespaceReference)) {
          if (jsElement == null && ModuleUtilCore.findModuleForPsiElement(ref) == null) {
            // do not red highlight conditional compiler definitions in 3rd party library/SDK source code
            return new ResolveResult[]{new JSResolveResult(ref)};
          }

          resolveResultsAsConditionalCompilationVariable = resolveConditionalCompilationVariable(ref);
        }

        if (resolveResultsAsConditionalCompilationVariable != null &&
            resolveResultsAsConditionalCompilationVariable.length > 0 &&
            (jsElement == null || resolveResultsAsConditionalCompilationVariable[0].isValidResult())) {
          return resolveResultsAsConditionalCompilationVariable;
        }
      }

      if (jsElement != null ||
          localProcessor.isEncounteredDynamicClasses() && qualifier == null ||
          !localProcessor.processingEncounteredAnyTypeAccess() && !localProcessor.isEncounteredDynamicClasses()
        ) {
        return localProcessor.getResultsAsResolveResults();
      }
    } else {
      final JSReferenceExpressionImpl.QualifiedItemProcessor processor = new JSReferenceExpressionImpl.QualifiedItemProcessor(referencedName, containingFile, ref) {
        @Override
        public void process(@NotNull JSType type, @NotNull BaseJSSymbolProcessor.EvaluateContext evaluateContext, PsiElement source) {
          qualifierType.set(JSTypeParser.addUnionOption(qualifierType.get(), type));
          super.process(type, evaluateContext, source);
        }
      };
      processor.setTypeContext(JSResolveUtil.isExprInTypeContext(ref));
      JSTypeEvaluator.evaluateTypes(qualifier, containingFile, processor);

      if (processor.resolved == JSReferenceExpressionImpl.QualifiedItemProcessor.TypeResolveState.PrefixUnknown) {
        return dummyResult(ref);
      }

      if (processor.resolved == JSReferenceExpressionImpl.QualifiedItemProcessor.TypeResolveState.Resolved ||
          processor.resolved == JSReferenceExpressionImpl.QualifiedItemProcessor.TypeResolveState.Undefined ||
          processor.getResult() != null
        ) {
        return processor.getResultsAsResolveResults();
      } else {
        localProcessor = processor;
      }
    }

    ResolveResult[] results =
      doOldResolve(ref, containingFile, referencedName, parent, qualifier, localResolve, localProcessor, qualifierType.get());

    if (results.length == 0 && localProcessor.isEncounteredXmlLiteral()) {
      return dummyResult(ref);
    }

    return results;
  }

  protected ResolveResult[] doOldResolve(final JSReferenceExpression ref,
                                         final PsiFile containingFile,
                                         final String referencedName,
                                         final PsiElement parent,
                                         final JSExpression qualifier,
                                         final boolean localResolve,
                                         final ResolveProcessor localProcessor,
                                         final JSType qualifierType) {
    final WalkUpResolveProcessor processor = new WalkUpResolveProcessor(
      referencedName, null,
      containingFile,
      ref,
      BaseJSSymbolProcessor.MatchMode.Strict
    );

    boolean inDefinition = false;
    boolean allowOnlyCompleteMatches = localResolve && localProcessor.isEncounteredDynamicClasses();

    if (parent instanceof JSDefinitionExpression) {
      inDefinition = true;
      if (localResolve && localProcessor.processingEncounteredAnyTypeAccess()) allowOnlyCompleteMatches = false;
      else allowOnlyCompleteMatches = true;
    } else if (qualifier instanceof JSThisExpression && localProcessor.processingEncounteredAnyTypeAccess()) {
      processor.allowPartialResults();
    }

    if (inDefinition || allowOnlyCompleteMatches) {
      processor.setAddOnlyCompleteMatches(allowOnlyCompleteMatches);
    }
    processor.setSkipDefinitions(inDefinition);
    if (localProcessor != null) processor.addLocalResults(localProcessor.getResultsAsResolveResults());

    JavaScriptIndex instance = JavaScriptIndex.getInstance(containingFile.getProject());
    instance.processAllSymbols(processor);

    ResolveResult[] results = processor.getResults();
    if (results.length == 0) {
      if (inDefinition) {
        return new ResolveResult[] { new JSResolveResult(parent) };
      } else {
        if (processor.getLimitingScope() != null) {
          processor.resetLimitingScope();
          instance.processAllSymbols(processor);
          results = processor.getResults();
        }
      }
    }

    return results;
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
        return new ResolveResult[]{new JSResolveResult(jsReferenceExpression, false)};
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
        return new ResolveResult[]{new JSResolveResult(jsReferenceExpression, false)};
      }
    }

    for (JSConditionalCompilationDefinitionsProvider provider : JSConditionalCompilationDefinitionsProvider.EP_NAME.getExtensions()) {
      if (provider.containsConstant(ModuleUtilCore.findModuleForPsiElement(jsReferenceExpression), namespace,
                                    constantName)) {
        return new ResolveResult[]{new JSResolveResult(jsReferenceExpression)};
      }
    }

    return new ResolveResult[]{new JSResolveResult(jsReferenceExpression, false)};
  }
}
