package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartImportInfo {
  private final @NotNull String myImportText;
  private final @Nullable String myPrefix;
  private final @NotNull Set<String> myShowComponents;
  private final @NotNull Set<String> myHideComponents;

  public DartImportInfo(final @NotNull String importText,
                        final @Nullable String prefix,
                        final @NotNull Set<String> showComponents,
                        final @NotNull Set<String> hideComponents) {
    myImportText = importText;
    myPrefix = prefix;
    myShowComponents = showComponents;
    myHideComponents = hideComponents;
  }

  @NotNull
  public String getImportText() {
    return myImportText;
  }

  @Nullable
  public String getPrefix() {
    return myPrefix;
  }

  @NotNull
  public Set<String> getShowComponents() {
    return myShowComponents;
  }

  @NotNull
  public Set<String> getHideComponents() {
    return myHideComponents;
  }

  public PsiScopeProcessor wrapElementProcessor(final PsiScopeProcessor processor) {
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
