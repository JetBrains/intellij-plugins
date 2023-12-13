package com.jetbrains.lang.dart.ide;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.ide.index.DartSymbolIndex.DART_SYMBOL_INDEX;

public final class DartSymbolContributor implements ChooseByNameContributorEx {
  @Override
  public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    FileBasedIndex.getInstance().processAllKeys(DART_SYMBOL_INDEX, processor, scope, null);
  }

  @Override
  public void processElementsWithName(@NotNull String name,
                                      @NotNull Processor<? super NavigationItem> processor,
                                      @NotNull FindSymbolParameters parameters) {
    DartClassContributor.doProcessElements(DART_SYMBOL_INDEX, DartSymbolContributor::getComponents,
                                           name, processor, parameters);
  }

  private static JBIterable<DartComponent> getComponents(PsiElement context) {
    return SyntaxTraverser.psiTraverser(context).traverse().filter(DartComponent.class);
  }
}
