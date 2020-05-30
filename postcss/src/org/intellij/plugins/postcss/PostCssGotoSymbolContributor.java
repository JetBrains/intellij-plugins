package org.intellij.plugins.postcss;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssGotoSymbolContributor implements ChooseByNameContributorEx {
  @Override
  public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    StubIndex.getInstance().processAllKeys(PostCssCustomSelectorIndex.KEY,
                                           s -> processor.process(s) && processor.process("--" + s) && processor.process(":--" + s),
                                           scope, filter);
    StubIndex.getInstance()
      .processAllKeys(PostCssCustomMediaIndex.KEY, s -> processor.process(s) && processor.process("--" + s), scope, filter);
  }

  @Override
  public void processElementsWithName(@NotNull String name,
                                      @NotNull Processor<? super NavigationItem> processor,
                                      @NotNull FindSymbolParameters parameters) {
    if (StringUtil.startsWith(name, ":--")) {
      StubIndex.getInstance().processElements(PostCssCustomSelectorIndex.KEY, name.substring(3), parameters.getProject(),
                                              parameters.getSearchScope(), PostCssCustomSelector.class, processor);
    }
    else if (StringUtil.startsWith(name, "--")) {
      StubIndex.getInstance().processElements(PostCssCustomSelectorIndex.KEY, name.substring(2), parameters.getProject(),
                                              parameters.getSearchScope(), PostCssCustomSelector.class, processor);
      StubIndex.getInstance().processElements(PostCssCustomMediaIndex.KEY, name.substring(2), parameters.getProject(),
                                              parameters.getSearchScope(), PostCssCustomMedia.class, processor);
    }
    else {
      StubIndex.getInstance().processElements(PostCssCustomSelectorIndex.KEY, name, parameters.getProject(),
                                              parameters.getSearchScope(), PostCssCustomSelector.class, processor);
      StubIndex.getInstance().processElements(PostCssCustomMediaIndex.KEY, name, parameters.getProject(),
                                              parameters.getSearchScope(), PostCssCustomMedia.class, processor);
    }
  }
}