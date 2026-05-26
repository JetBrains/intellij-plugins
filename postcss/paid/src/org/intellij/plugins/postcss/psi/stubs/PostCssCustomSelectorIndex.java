package org.intellij.plugins.postcss.psi.stubs;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PostCssCustomSelectorIndex extends StringStubIndexExtension<PostCssCustomSelector> {
  public static final StubIndexKey<String, PostCssCustomSelector> KEY = StubIndexKey.createIndexKey("postcss.custom.selector");

  @Override
  public @NotNull StubIndexKey<String, PostCssCustomSelector> getKey() {
    return KEY;
  }

  public static Collection<PostCssCustomSelector> getCustomSelectors(final @NotNull String name, final @NotNull PsiElement context) {
    GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(context);
    return StubIndex.getElements(KEY, name, context.getProject(), scope, PostCssCustomSelector.class);
  }
}