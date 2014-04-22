package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartImportOrExportInfo {
  public enum Kind {Import, Export}

  private final @NotNull Kind myKind;
  private final @NotNull String myUri;
  private final @Nullable String myImportPrefix;
  private final @NotNull Set<String> myShowComponents;
  private final @NotNull Set<String> myHideComponents;

  public DartImportOrExportInfo(final @NotNull Kind kind,
                                final @NotNull String uri,
                                final @Nullable String importPrefix,
                                final @NotNull Set<String> showComponents,
                                final @NotNull Set<String> hideComponents) {
    myKind = kind;
    myUri = uri;
    myImportPrefix = kind == Kind.Export ? null : importPrefix;
    myShowComponents = showComponents;
    myHideComponents = hideComponents;
  }

  @NotNull
  public String getUri() {
    return myUri;
  }

  @NotNull
  public Kind getKind() {
    return myKind;
  }

  @Nullable
  public String getImportPrefix() {
    return myImportPrefix;
  }

  @NotNull
  public Set<String> getShowComponents() {
    return myShowComponents;
  }

  @NotNull
  public Set<String> getHideComponents() {
    return myHideComponents;
  }

  public PsiScopeProcessor createShowHideAwareProcessor(final PsiScopeProcessor processor) {
    if (myShowComponents.isEmpty() && myHideComponents.isEmpty()) {
      return processor;
    }
    return new PsiScopeProcessor() {
      @Override
      public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        if (element instanceof DartComponentName && isComponentExcluded(((DartComponentName)element).getName())) {
          return true;
        }
        return processor.execute(element, state);
      }

      @Nullable
      @Override
      public <T> T getHint(@NotNull Key<T> hintKey) {
        return processor.getHint(hintKey);
      }

      @Override
      public void handleEvent(@NotNull Event event, @Nullable Object associated) {
        processor.handleEvent(event, associated);
      }
    };
  }

  private boolean isComponentExcluded(@Nullable String elementName) {
    // nothing
    if (myShowComponents.isEmpty() && myHideComponents.isEmpty()) {
      return false;
    }
    // hide
    if (myHideComponents.contains(elementName)) {
      return true;
    }
    // show isn't empty and doesn't contain name
    if (!myShowComponents.isEmpty() && !myShowComponents.contains(elementName)) {
      return true;
    }
    return false;
  }
}
