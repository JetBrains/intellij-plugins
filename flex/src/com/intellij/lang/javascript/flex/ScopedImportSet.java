// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.resolve.JSImportedElementResolveResult;
import com.intellij.lang.javascript.psi.resolve.JSResolveProcessorEx;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Maxim.Mossienko
 */
public abstract class ScopedImportSet {
  private static final ImportProcessor<JSImportedElementResolveResult> ourFindClassProcessor = new ImportProcessor<>() {
    @Override
    public JSImportedElementResolveResult process(@NotNull String referenceName, @NotNull ImportInfo info, @NotNull PsiNamedElement scope) {
      return resolveImportedClass(referenceName, scope, info);
    }
  };

  protected static void appendToMap(final Map<String, Object> map, final JSImportStatement importStatement) {
    doAppendToMap(map, importStatement);
  }

  protected static void doAppendToMap(final Map<String, Object> map, final Object stringOrStatement) {
    String s =
      stringOrStatement instanceof String ? (String)stringOrStatement : ((JSImportStatement)stringOrStatement).getImportText();

    final int index = s.lastIndexOf('.');
    if (index == -1) return; // nothing to import, global symbol
    final String key = s.substring(index + 1);
    final Object o = map.get(key);

    if (o == null) {
      map.put(key, stringOrStatement);
    } else if (o instanceof Object[]) {
      map.put(key, ArrayUtil.append((Object[])o, stringOrStatement));
    } else {
      map.put(key, new Object[]{stringOrStatement, o});
    }
  }

  public static JSImportedElementResolveResult resolveImportedClass(@NotNull String referenceName, final PsiNamedElement file,
                                                                    final ImportProcessor.ImportInfo info) {
    String nameToTry = info.getQNameToSearch(referenceName);
    PsiElement element = JSDialectSpecificHandlersFactory.forElement(file).getClassResolver().findClassByQName(nameToTry, file);

    if (element != null) {
      return new JSImportedElementResolveResult(
        nameToTry,
        element,
        info.source instanceof JSImportStatement ? (JSImportStatement)info.source : null
      );
    }

    return null;
  }

  public boolean tryResolveImportedClass(PsiNamedElement scope, JSResolveProcessorEx resolveProcessor) {
    final String qname = resolveProcessor.getQualifiedNameToImport();
    final Object result = process(resolveProcessor.getName(), qname, scope, new ImportProcessor<>() {
      @Override
      public Object process(@NotNull String referenceName, @NotNull ImportInfo info, @NotNull PsiNamedElement scope) {
        if (qname != null && !qname.equals(info.getQNameToSearch(referenceName))) return null;
        final JSImportedElementResolveResult resolveResult = ourFindClassProcessor.process(referenceName, info, scope);
        return dispatchResult(resolveResult, resolveProcessor) ? resolveResult : null;
      }
    });
    return result == null;
  }

  private static boolean dispatchResult(JSImportedElementResolveResult expression, PsiScopeProcessor processor) {
    if (expression != null) {
      final PsiElement element = expression.resolvedElement;

      if (element != null) {
        ResolveState state = ResolveState.initial();
        if (expression.importStatement != null) state = state.put(JSResolveResult.IMPORT_KEY, expression.importStatement);
        return !processor.execute(element, state);
      }
    }

    return false;
  }

  public <T> T process(final @NotNull String referenceName,
                       final @Nullable String qualifiedName,
                       final @NotNull PsiNamedElement scope,
                       final @NotNull ImportProcessor<T> processor) {
    Map<String, Object> map = getUpToDateMap(scope);
    T o = tryEntry(map.get(referenceName), referenceName, scope, processor, false);
    if (o == null) o = tryEntry(map.get("*"), referenceName, scope, processor, true);
    if (o == null && qualifiedName != null) {
      for (final Object entry : map.values()) {
        ImportProcessor<T> filteringProcessor = new ImportProcessor<>() {
          @Override
          public T process(@NotNull String referenceName,
                           @NotNull ImportInfo info,
                           @NotNull PsiNamedElement scope) {
            if (!StringUtil.getPackageName(qualifiedName).equals(StringUtil.getPackageName(info.importString))) {
              return null;
            }
            return processor.process(referenceName, info, scope);
          }
        };
        o = tryEntry(entry, referenceName, scope, filteringProcessor, true);
        if (o != null) break;
      }
    }
    return o;
  }

  private static <T> T tryEntry(Object entry, String referenceName, PsiNamedElement scope, ImportProcessor<T> processor, boolean starImport) {
    if (entry == null) {
      return null;
    }
    else if (entry instanceof Object[]) {
      for (Object entryItem : (Object[])entry) {
        T result = dispatch(referenceName, entryItem, starImport, scope, processor);
        if (result != null) return result;
      }
    }
    else {
      return dispatch(referenceName, entry, starImport, scope, processor);
    }

    return null;
  }

  private static <T> T dispatch(String referenceName, Object entry, boolean starImport, PsiNamedElement scope, ImportProcessor<T> processor) {
    String importString = entry instanceof String ? (String)entry : ((JSImportStatement)entry).getImportText();
    ImportProcessor.ImportInfo importInfo = new ImportProcessor.ImportInfo(importString, starImport, entry);
    return processor.process(referenceName, importInfo, scope);
  }

  protected abstract @NotNull Map<String, Object> getUpToDateMap(@NotNull PsiElement scope);

  public interface ImportProcessor<T> {
    final class ImportInfo {
      public final String importString;
      public final boolean starImport;
      public final Object source;

      public ImportInfo(String _importString, boolean _starImport, Object _source) {
        importString = _importString;
        starImport = _starImport;
        source = _source;
      }

      public String getQNameToSearch(@NotNull String referenceName) {
        return starImport ? StringUtil.getQualifiedName(StringUtil.getPackageName(importString), referenceName) : importString;
      }
    }

    T process(@NotNull String referenceName, @NotNull ImportInfo info, @NotNull PsiNamedElement scope);
  }
}
