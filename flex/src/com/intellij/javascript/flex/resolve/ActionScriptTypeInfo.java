// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;


import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSNamespace;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.JSQualifiedNameImpl;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory;
import com.intellij.lang.javascript.psi.types.JSTopLevelNamespace;
import com.intellij.lang.javascript.psi.types.JSTypeContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.evaluable.JSNamespaceProviderStubBasedExpressionType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ActionScriptTypeInfo {

  static final ActionScriptContextLevel GLOBAL_CONTEXT_LEVEL =
    new ActionScriptContextLevel(new JSTopLevelNamespace(JSTypeSource.EXPLICITLY_DECLARED, false, JSTypeContext.STATIC), 0);

  public final @NotNull List<ActionScriptContextLevel> myContextLevels;

  private final @NotNull GlobalStatusHint myContextGlobalStatusHint;
  private boolean myForcedUnknownContext;

  public ActionScriptTypeInfo(@NotNull GlobalStatusHint contextGlobalStatusHint) {
    myContextLevels = new ArrayList<>();
    myContextGlobalStatusHint = contextGlobalStatusHint;
    if (contextGlobalStatusHint == GlobalStatusHint.GLOBAL) {
      addGlobalType();
    }
  }

  public void addType(@NotNull JSNamespace namespace, boolean isTopClassInHierarchy) {
    JSQualifiedName qualifiedName = namespace.getQualifiedName(); // no need to resolve, we only check for global and predefined namespaces

    if (qualifiedName == null && !(namespace instanceof JSNamespaceProviderStubBasedExpressionType)) {
      if (!namespace.isSourceStrict()) return;
      if (!namespace.isLocal() && !(namespace instanceof JSAnyType)) {
        addGlobalType();
      }
      return;
    }

    if (qualifiedName != null && BaseJSSymbolProcessor.canBeResolvedToUnqualified(qualifiedName, namespace.getJSContext())) {
      addGlobalType();
    }

    final int relativeLevel = getRelativeLevel(isTopClassInHierarchy, qualifiedName);

    ProcessingContext context = new ProcessingContext();
    for (int i = 0; i < myContextLevels.size(); i++) {
      ActionScriptContextLevel contextLevel = myContextLevels.get(i);
      if (contextLevel.myNamespace.isEquivalentTo(namespace, context, false)) {
        if (relativeLevel < contextLevel.myRelativeLevel) {
          myContextLevels.set(i, new ActionScriptContextLevel(namespace, relativeLevel));
        }
        return;
      }
    }

    myContextLevels.add(new ActionScriptContextLevel(namespace, relativeLevel));
  }

  private int getRelativeLevel(boolean isTopClassInHierarchy, @Nullable JSQualifiedName qualifiedName) {
    if (qualifiedName != null && qualifiedName.getParent() == null &&
        JSCommonTypeNames.OBJECT_FUNCTION_CLASS_NAMES.contains(qualifiedName.getName())) {
      return JSLookupPriority.OBJECT_AND_FUNCTION_MEMBERS_NESTING_LEVEL;
    }

    return isTopClassInHierarchy || myContextLevels.isEmpty() ? 0 :
           myContextLevels.getLast().myRelativeLevel + 1;
  }

  public void addGlobalType() {
    for (ActionScriptContextLevel level: myContextLevels) {
      if (level == GLOBAL_CONTEXT_LEVEL) return;
    }
    myContextLevels.add(GLOBAL_CONTEXT_LEVEL);
  }

  public void addBaseObjectType() {
    final JSQualifiedNameImpl name = JSQualifiedNameImpl.create(JSCommonTypeNames.OBJECT_CLASS_NAME, null);
    final JSNamespace namespace = JSNamedTypeFactory.createNamespace(name, JSContext.INSTANCE, null, false);
    myContextLevels.add(new ActionScriptContextLevel(namespace, JSLookupPriority.OBJECT_AND_FUNCTION_MEMBERS_NESTING_LEVEL));
  }

  public boolean isEmpty() {
    return myContextLevels.isEmpty();
  }

  public boolean typeWasProcessed() {
    return !myContextLevels.isEmpty();
  }

  public void addNamespace(@NotNull JSNamespace namespace, boolean isTopClassInHierarchy) {
    addType(namespace, isTopClassInHierarchy);
  }

  public void addNamespace(@NotNull String type,
                           boolean isTopClassInHierarchy,
                           JSContext staticOrInstance,
                           boolean isExplicitlyDeclared) {
    final JSQualifiedNameImpl qualifiedName =
      StringUtil.isQuotedString(type) ? JSQualifiedNameImpl.create(type, null) : JSQualifiedNameImpl.fromNamepath(type);
    final JSNamespace namespace = JSNamedTypeFactory.createNamespace(qualifiedName, staticOrInstance, null, isExplicitlyDeclared);
    addNamespace(namespace, isTopClassInHierarchy);
  }

  public void buildIndexListFromQNameAndCorrectQName(@NotNull String type, boolean isTopClassInHierarchy, JSContext staticOrInstance) {
    addNamespace(type, isTopClassInHierarchy, staticOrInstance, true);
  }

  public void buildIndexListFromQNameAndCorrectQName(@NotNull String type) {
    buildIndexListFromQNameAndCorrectQName(type, false, JSContext.UNKNOWN);
  }

  /**
   * If true, only symbols from JSGlobalSymbolIndex should be taken
   */
  public boolean isGlobalContext() {
    return getContextGlobalStatusHint() == GlobalStatusHint.GLOBAL;
  }

  /**
   * If true, only symbols from JSNonGlobalSymbolIndex should be taken
   */
  public boolean isNonGlobalContext() {
    return getGlobalStatusHint() == GlobalStatusHint.NONGLOBAL;
  }

  private @NotNull GlobalStatusHint getGlobalStatusHint() {
    if (myForcedUnknownContext) return GlobalStatusHint.UNKNOWN;

    boolean onlyGlobal = true;
    boolean onlyNonGlobal = true;
    boolean hasGlobal = false;
    boolean hasNonGlobal = false;
    for (ActionScriptContextLevel level : myContextLevels) {
      final GlobalStatusHint contextLevelHint = getContextLevelGlobalStatusHint(level);
      onlyGlobal &= contextLevelHint == GlobalStatusHint.GLOBAL;
      onlyNonGlobal &= contextLevelHint == GlobalStatusHint.NONGLOBAL;
      hasGlobal |= contextLevelHint == GlobalStatusHint.GLOBAL;
      hasNonGlobal |= contextLevelHint == GlobalStatusHint.NONGLOBAL;
    }

    if (onlyGlobal && (hasGlobal || getContextGlobalStatusHint() == GlobalStatusHint.GLOBAL)) return GlobalStatusHint.GLOBAL;

    if (hasNonGlobal && onlyNonGlobal ||
        getContextGlobalStatusHint() == GlobalStatusHint.NONGLOBAL && !hasGlobal) {
      return GlobalStatusHint.NONGLOBAL;
    }

    return GlobalStatusHint.UNKNOWN;
  }

  public @NotNull GlobalStatusHint getContextGlobalStatusHint() {
    return myContextGlobalStatusHint;
  }

  private static GlobalStatusHint getContextLevelGlobalStatusHint(@NotNull ActionScriptContextLevel level) {
    if (level == GLOBAL_CONTEXT_LEVEL) return GlobalStatusHint.GLOBAL;
    if (level.myNamespace.hasQualifiedName()) {
      return GlobalStatusHint.NONGLOBAL;
    }

    return GlobalStatusHint.UNKNOWN;
  }

  public void setForcedUnknownContext() {
    myForcedUnknownContext = true;
  }

  public enum GlobalStatusHint {
    GLOBAL, NONGLOBAL, UNKNOWN
  }
}
