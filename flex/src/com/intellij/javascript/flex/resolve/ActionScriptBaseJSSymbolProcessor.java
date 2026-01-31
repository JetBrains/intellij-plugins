// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.javascript.flex.resolve;

import com.intellij.codeInsight.completion.CompletionUtilCoreImpl;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSNamespace;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.JSQualifiedNameImpl;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.StubSafe;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.jsdoc.JSDocTagType;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSCompleteTypeEvaluationProcessor;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSGenericParameterImpl;
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory;
import com.intellij.lang.javascript.psi.types.JSTypeContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.primitives.JSObjectType;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IntRef;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

import static com.intellij.lang.javascript.index.JSTypeEvaluateManager.iterateTypeHierarchy;
import static com.intellij.lang.javascript.psi.resolve.JSResolveUtil.getOriginalFile;

/**
 * @author Maxim.Mossienko
 */
public abstract class ActionScriptBaseJSSymbolProcessor {

  protected final @NotNull ActionScriptTypeInfo myTypeInfo;
  protected final @NotNull PsiElement myContext;
  protected final @NotNull PsiFile myTargetFile;
  protected final @Nullable VirtualFile myTargetVirtualFile;

  protected @Nullable VirtualFile myCurrentFile;

  public enum MatchType {
    COMPLETE_WITH_CONTEXT, COMPLETE, PARTIAL, NOMATCH
  }

  private enum CompleteMatchSign {
    COMPLETE_MATCH, INCOMPLETE_MATCH, FORCED_COMPLETE_MATCH
  }

  private final EnumSet<CompleteMatchSign> myCompleteMatchSigns = EnumSet.noneOf(CompleteMatchSign.class);

  protected ActionScriptBaseJSSymbolProcessor(final @NotNull PsiFile targetFile, final @NotNull PsiElement context) {
    myTargetFile = targetFile;
    myTargetVirtualFile = targetFile.getVirtualFile();
    myContext = context;
    myTypeInfo = createTypeInfo(initGlobalStatusHint(context));
  }

  protected ActionScriptTypeInfo createTypeInfo(@NotNull ActionScriptTypeInfo.GlobalStatusHint globalStatusHint) {
    return new ActionScriptTypeInfo(globalStatusHint);
  }

  @StubSafe
  protected @NotNull ActionScriptTypeInfo.GlobalStatusHint initGlobalStatusHint(@Nullable PsiElement context) {
    if (!(context instanceof JSReferenceExpression)) return ActionScriptTypeInfo.GlobalStatusHint.UNKNOWN;

    if (((JSReferenceExpression)context).getQualifier() != null) {
      return ActionScriptTypeInfo.GlobalStatusHint.NONGLOBAL;
    }

    boolean withinWithStatement = !JSPsiImplUtils.getWithStatementContexts((JSReferenceExpression)context).isEmpty();
    if (!withinWithStatement &&
        ActionScriptResolveUtil.getRealRefExprQualifier((JSReferenceExpression)context) == null) {
      return ActionScriptTypeInfo.GlobalStatusHint.GLOBAL;
    }

    return ActionScriptTypeInfo.GlobalStatusHint.UNKNOWN;
  }

  protected void updateTypeInfoFromThis(@Nullable JSNamespace ns) {
    if (ns != null && JSTypeUtils.isStrictType(ns)) {
      setAddOnlyCompleteMatches();
    }
    else {
      myTypeInfo.setForcedUnknownContext();
    }
  }

  public void addTypeHierarchy(@NotNull JSNamespace namespace) {
    myTypeInfo.addNamespace(namespace, true);
    addSupers(namespace);
  }

  public void addSupers(@NotNull JSNamespace namespace) {
    final JSQualifiedName nsName = namespace.getQualifiedName();
    if (nsName == null) return;

    final JSContext staticOrInstance = namespace.getJSContext();

    final List<JSNamespace> parentClasses = new SmartList<>();
    Processor<JSType> baseTypeProcessor = type -> {
      JSNamespace baseNs = JSTypeUtils.getNamespaceMatchingType(type, false);
      if (baseNs == null || baseNs instanceof JSObjectType) return true;
      if (baseNs.getQualifiedName() != null) {
        parentClasses.add(baseNs);
      }
      return true;
    };

    Processor<JSClass> baseClassProcessor = clazz -> {
      final String name = clazz.getQualifiedName();
      if (JSCommonTypeNames.OBJECT_CLASS_NAME.equals(name)) return true;
      if (name != null) {
        JSNamespace classNs = JSNamedTypeFactory.buildProvidedNamespace(clazz, true);
        if (classNs != null && classNs.getQualifiedName() != null) {
          classNs = JSNamedTypeFactory.copyWithJSContext(classNs, staticOrInstance);
          parentClasses.add(classNs);
        }
      }
      return true;
    };

    iterateTypeHierarchy(namespace, baseClassProcessor, baseTypeProcessor, myContext, false);

    for (JSNamespace parentClass : parentClasses) {
      myTypeInfo.addNamespace(parentClass, false);
    }
  }

  public abstract String getRequiredName();

  public void forceSetAddOnlyCompleteMatches() {
    addCompleteMatchSign(CompleteMatchSign.FORCED_COMPLETE_MATCH);
  }

  public void setAddOnlyCompleteMatches() {
    addCompleteMatchSign(CompleteMatchSign.COMPLETE_MATCH);
  }

  public void allowPartialResults() {
    addCompleteMatchSign(CompleteMatchSign.INCOMPLETE_MATCH);
  }

  private void addCompleteMatchSign(@NotNull CompleteMatchSign sign) {
    myCompleteMatchSigns.add(sign);
  }

  public boolean addOnlyCompleteMatches() {
    if (myCompleteMatchSigns.contains(CompleteMatchSign.FORCED_COMPLETE_MATCH)) return true;
    return addOnlyCompleteMatchesEvaluated();
  }

  protected boolean addOnlyCompleteMatchesEvaluated() {
    if (myCompleteMatchSigns.contains(CompleteMatchSign.INCOMPLETE_MATCH)) return false;
    if (myCompleteMatchSigns.contains(CompleteMatchSign.COMPLETE_MATCH)) return true;
    return false;
  }

  public static boolean isGlobalNS(@NotNull JSNamespace jsNamespace, boolean includeGlobalObjects) {
    if (jsNamespace.isLocal()) return false;
    if (jsNamespace instanceof JSAnyType) return false;
    JSQualifiedName namespace = jsNamespace.getQualifiedName();
    if (namespace == null) return true;

    final String name = namespace.getName();
    return includeGlobalObjects &&
           (JSSymbolUtil.GLOBAL_OBJECT_NAMES.contains(name) && namespace.getParent() == null ||
            JSSymbolUtil.GLOBAL_TYPES.contains(namespace)
           );
  }

  public boolean acceptsFile(PsiFile file) {
    myCurrentFile = file.getViewProvider().getVirtualFile();
    boolean currentFileEcma = file.getLanguage() == FlexSupportLoader.ECMA_SCRIPT_L4;
    return currentFileEcma;
  }

  protected boolean isFromRelevantFileOrDirectory() {
    return Comparing.equal(myTargetVirtualFile, myCurrentFile);
  }

  protected boolean isStrictTypingPossible(@NotNull JSType type) {
    return type.isSourceStrict() &&
           !(type instanceof JSObjectType) &&
           !(type instanceof JSAnyType) &&
           !(type instanceof JSRecordType && !((JSRecordType)type).hasMembers()) &&
           !(type instanceof JSGenericParameterImpl);
  }

  static boolean isValidType(String parameterType) {
    return parameterType != null && !parameterType.isEmpty();
  }

  protected void addPackageScope(final @Nullable JSClass jsClass, final @Nullable PsiElement expression) {
    final String packageQualifier = JSResolveUtil.findPackageStatementQualifier(expression);

    if (packageQualifier != null) {
      myTypeInfo.buildIndexListFromQNameAndCorrectQName(packageQualifier);
    }
    else if (jsClass != null) {
      String qName = jsClass.getQualifiedName();
      if (qName != null && !qName.equals(jsClass.getName())) {
        final int index = qName.lastIndexOf('.');
        if (index > 0) myTypeInfo.buildIndexListFromQNameAndCorrectQName(qName.substring(0, index));
      }
    }
    else if (expression != null) {
      String s = ActionScriptResolveUtil.findPackageForMxml(expression);
      if (isValidType(s)) myTypeInfo.buildIndexListFromQNameAndCorrectQName(s);
    }
  }

  public @NotNull PsiElement getContext() {
    return myContext;
  }

  public @NotNull ActionScriptTypeInfo getTypeInfo() {
    return myTypeInfo;
  }

  @StubSafe
  protected @NotNull MatchType isAcceptableQualifiedItem(final @NotNull JSPsiElementBase element,
                                                         @NotNull IntRef typeHierarchyLevel) {
    PsiElement parent = element.getContext();
    JSNamespace elementJSNamespace =
      parent instanceof JSClass ? JSNamedTypeFactory.buildProvidedNamespace((JSClass)parent, true) : element.getJSNamespace();
    if (elementJSNamespace == null) elementJSNamespace = JSAnyType.get(element);
    JSQualifiedName namespace = elementJSNamespace.getQualifiedName();

    boolean isElementLocal = elementJSNamespace.isLocal();
    boolean isNamespaceExplicitlyDeclared = element.isNamespaceExplicitlyDeclared();

    boolean isGlobal = isGlobalNS(elementJSNamespace, false);

    if (myTypeInfo.isNonGlobalContext() &&
        isNamespaceExplicitlyDeclared &&
        isGlobal) {
      // Qualified reference cannot be resolved into global symbol.
      // See JSQualifiedObjectStubBase.doIndexNonGlobalSymbol
      return MatchType.NOMATCH;
    }

    Ref<ActionScriptContextLevel> contextLevelRef = Ref.create(null);
    final MatchType result = iterateContextLevels(element, contextLevelRef, namespace, elementJSNamespace);
    ActionScriptContextLevel contextLevel = contextLevelRef.get();
    if (contextLevel != null) {
      typeHierarchyLevel.set(contextLevel.myRelativeLevel);
    }
    if (result != null) return result;

    if (myTypeInfo.isGlobalContext() && !isGlobalNS(elementJSNamespace, true)) {
      return MatchType.NOMATCH;
    }

    if (isNamespaceExplicitlyDeclared && isElementLocal && namespace == null &&
        !(myContext instanceof JSDocTagType && element instanceof JSClass)) {
      return MatchType.NOMATCH;
    }

    return MatchType.PARTIAL;
  }

  @StubSafe
  private @Nullable MatchType iterateContextLevels(@NotNull JSPsiElementBase element,
                                                   @NotNull Ref<ActionScriptContextLevel> contextLevelRef,
                                                   @Nullable JSQualifiedName namespace,
                                                   @NotNull JSNamespace elementJSNamespace) {
    if (myTypeInfo.myContextLevels.isEmpty()) return null;

    JSContext elementStaticOrInstance = element.getJSContext();
    boolean jsContextMismatched = false;
    for (ActionScriptContextLevel contextLevel : myTypeInfo.myContextLevels) {
      if (!contextLevel.isElementInScope(element, elementJSNamespace)) continue;

      if (isElementFromNamespaceIgnoringJSContext(contextLevel.myNamespace, namespace)) {
        if (!contextLevel.myNamespace.getJSContext().isCompatibleWith(elementStaticOrInstance)) {
          jsContextMismatched = true;
          continue;
        }
        if (jsContextMismatched && contextLevel.myNamespace.getJSContext() == JSContext.UNKNOWN) {
          continue; // current context is probably mismatched too
        }

        contextLevelRef.set(contextLevel);

        return contextLevel.myRelativeLevel == 0 &&
               (elementStaticOrInstance != JSContext.UNKNOWN && contextLevel.myNamespace.getJSContext() != JSContext.UNKNOWN ||
                contextLevel == ActionScriptTypeInfo.GLOBAL_CONTEXT_LEVEL)
               ? MatchType.COMPLETE_WITH_CONTEXT : MatchType.COMPLETE;
      }
    }

    return jsContextMismatched ? MatchType.NOMATCH : null;
  }

  public static boolean isElementFromNamespaceIgnoringJSContext(@NotNull JSNamespace contextLevelNamespace,
                                                                @Nullable JSQualifiedName elementNamespace) {
    JSQualifiedName currentContextNamespace = JSQualifiedNameImpl.getResolvedQualifiedName(contextLevelNamespace);
    JSQualifiedName currentNs = elementNamespace;

    while (currentContextNamespace != null) {
      if (currentNs == null || !currentContextNamespace.getName().equals(currentNs.getName())) {
        break;
      }
      currentNs = currentNs.getParent();
      currentContextNamespace = currentContextNamespace.getParent();
    }

    return currentContextNamespace == null && currentNs == null;
  }

  @Contract("null -> null")
  public static JSExpression getOriginalQualifier(@Nullable JSExpression rawqualifier) {
    if (rawqualifier == null) return null;
    JSExpression element = CompletionUtilCoreImpl.getOriginalElement(rawqualifier);
    return element != null ? element : rawqualifier;
  }

  public static boolean isCompleteOrWithContextMatchType(@Nullable MatchType matchType) {
    return matchType == MatchType.COMPLETE_WITH_CONTEXT || matchType == MatchType.COMPLETE;
  }

  public static void addResolveResultTags(@NotNull EnumSet<ActionScriptTaggedResolveResult.ResolveResultTag> tags,
                                          @Nullable PsiElement element,
                                          @NotNull PsiElement context) {
    if (element instanceof JSDefinitionExpression) {
      addTagsForDefinitionInClass((JSDefinitionExpression)element, tags);
      if (context instanceof JSReferenceExpression && JSResolveUtil.isSameReference((JSReferenceExpression)context, element)) {
        tags.add(ActionScriptTaggedResolveResult.ResolveResultTag.SELF_DEFINITION);
      }
      if (!((JSDefinitionExpression)element).isDeclaration()) {
        tags.add(ActionScriptTaggedResolveResult.ResolveResultTag.IS_ASSIGNMENT);
      }
    }
  }

  @StubSafe
  private static void addTagsForDefinitionInClass(@NotNull JSDefinitionExpression element,
                                                  @NotNull EnumSet<ActionScriptTaggedResolveResult.ResolveResultTag> tags) {
    if (element.getJSNamespace().getTypeContext() != JSTypeContext.PROTOTYPE) {
      // need to check that element is 'this.foo=42' assignment, but it is not stored in stubs, so we do a less strict check
      return;
    }
    PsiElement scopeNode = JSPsiImplUtils.getParentFunctionOrClassThroughLambdas(element);
    if (scopeNode == null ||
        !(scopeNode instanceof JSClass || JSClassUtils.isClassMember(scopeNode))) {
      return;
    }
    tags.add(ActionScriptTaggedResolveResult.ResolveResultTag.DEFINITION_IN_CLASS_NOT_CONSTRUCTOR);
  }

  public abstract class JSTypeProcessorBase implements JSTypeProcessor {

    public void evaluateQualifier(@NotNull JSExpression qualifier, @NotNull JSExpression originalQualifier) {
      JSCompleteTypeEvaluationProcessor.evaluateTypes(
        originalQualifier, getOriginalFile(originalQualifier), this, true);

      if (!addOnlyCompleteMatchesEvaluated() || myTypeInfo.isEmpty()) {
        final JSNamespace namespace = JSSymbolUtil.evaluateNamespaceLocally(qualifier);
        if (namespace != null) {
          myTypeInfo.addNamespace(namespace, true);
        }
      }
    }

    @Override
    public void processAdditionalType(@NotNull JSType type, @NotNull JSEvaluateContext context) {
      if (!JSTypeUtils.processExpandedType(t -> {
        doProcessAdditionalType(t);
        return true;
      }, type)) {
        return;
      }
      doProcessAdditionalType(type);
    }

    private void doProcessAdditionalType(@NotNull JSType type) {
      if (!(type instanceof JSNamespace namespace)) return;
      if (namespace.getQualifiedName() == null) return;

      namespace = (JSNamespace)namespace.copyWithStrict(true);

      addTypeHierarchy(namespace);
    }

    protected final void processCandidate(@NotNull JSType type) {
      updateResolveStrictness(type);

      if (type instanceof JSAnyType) {
        return;
      }

      JSNamespace namespace = JSTypeUtils.getNamespaceMatchingType(type, isMakeAddedNamespaceStrict());
      if (namespace == null) return;

      addTypeHierarchy(namespace);
    }

    protected boolean isMakeAddedNamespaceStrict() {
      return false;
    }

    private void updateResolveStrictness(@NotNull JSType type) {
      if (type instanceof JSAnyType) {
        allowPartialResults();
      }
      JSTypeSource typeSource = type.getSource();
      if (isStrictTypingPossible(type)) {
        setAddOnlyCompleteMatches();
      }
      else if (!typeSource.isStrict()) {
        allowPartialResults();
      }
    }

    protected void allowPartialResults() {
      ActionScriptBaseJSSymbolProcessor.this.allowPartialResults();
    }
  }
}
