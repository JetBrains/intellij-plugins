package org.intellij.plugins.postcss.psi.stubs;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PostCssCustomMediaIndex extends StringStubIndexExtension<PostCssCustomMedia> {
  public static final StubIndexKey<String, PostCssCustomMedia> KEY = StubIndexKey.createIndexKey("postcss.custom.media");

  @Override
  public @NotNull StubIndexKey<String, PostCssCustomMedia> getKey() {
    return KEY;
  }

  public static Collection<PostCssCustomMedia> getCustomMediaFeatures(final @NotNull String name, final @NotNull PsiElement context) {
    GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(context);
    return StubIndex.getElements(KEY, name, context.getProject(), scope, PostCssCustomMedia.class);
  }
}