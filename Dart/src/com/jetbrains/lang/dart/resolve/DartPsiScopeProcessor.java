package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.containers.Stack;
import com.jetbrains.lang.dart.ide.index.DartShowHideInfo;
import com.jetbrains.lang.dart.psi.DartComponentName;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public abstract class DartPsiScopeProcessor implements PsiScopeProcessor {
  private static final Logger LOG = Logger.getInstance(DartResolveProcessor.class.getName());

  private final Stack<Pair<VirtualFile, DartShowHideInfo>> myShowHideFilters = new Stack<>();
  private final Map<VirtualFile, Collection<PsiElement>> myFilteredOutElements = new THashMap<>();

  public void importedFileProcessingStarted(final @NotNull VirtualFile importedFile, final @NotNull DartShowHideInfo showHideInfo) {
    myShowHideFilters.push(Pair.create(importedFile, showHideInfo));
  }

  public void importedFileProcessingFinished(final @NotNull VirtualFile importedFile) {
    LOG.assertTrue(!myShowHideFilters.isEmpty(), importedFile.getPath());
    final Pair<VirtualFile, DartShowHideInfo> removed = myShowHideFilters.pop();
    LOG.assertTrue(importedFile.equals(removed.first), "expected: " + removed.first.getPath() + ", actual: " + importedFile.getPath());
  }

  public void processFilteredOutElementsForImportedFile(final @NotNull VirtualFile importedFile) {
    // removed now, but may be added again in execute();
    final Collection<PsiElement> elements = myFilteredOutElements.remove(importedFile);
    if (elements != null) {
      for (PsiElement element : elements) {
        execute(element, ResolveState.initial());
      }
    }
  }

  @Override
  public final boolean execute(final @NotNull PsiElement element, final @NotNull ResolveState state) {
    if (!(element instanceof DartComponentName)) return true;

    final String name = ((DartComponentName)element).getName();
    if (!myShowHideFilters.isEmpty() && StringUtil.startsWithChar(name, '_')) {
      return true;
    }

    if (isFilteredOut(name)) {
      final VirtualFile importedFile = myShowHideFilters.peek().first;
      Collection<PsiElement> elements = myFilteredOutElements.get(importedFile);
      if (elements == null) {
        elements = new ArrayList<>();
        myFilteredOutElements.put(importedFile, elements);
      }
      elements.add(element);

      return true;
    }

    return doExecute((DartComponentName)element);
  }

  protected abstract boolean doExecute(final @NotNull DartComponentName dartComponentName);

  @Override
  public <T> T getHint(@NotNull Key<T> hintKey) {
    return null;
  }

  @Override
  public void handleEvent(@NotNull Event event, @Nullable Object associated) {
  }

  protected boolean isFilteredOut(final String name) {
    for (Pair<VirtualFile, DartShowHideInfo> filter : myShowHideFilters) {
      if (isFilteredOut(name, filter.second)) return true;
    }
    return false;
  }

  private static boolean isFilteredOut(final @Nullable String name, final @NotNull DartShowHideInfo showHideInfo) {
    if (showHideInfo.getHideComponents().contains(name)) return true;
    if (!showHideInfo.getShowComponents().isEmpty() && !showHideInfo.getShowComponents().contains(name)) return true;
    return false;
  }
}
