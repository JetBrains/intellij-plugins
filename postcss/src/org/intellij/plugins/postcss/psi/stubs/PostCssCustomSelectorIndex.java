package org.intellij.plugins.postcss.psi.stubs;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorIndex extends StringStubIndexExtension<PostCssCustomSelector> {
  public static final StubIndexKey<String, PostCssCustomSelector> KEY = StubIndexKey.createIndexKey("postcss.custom.selector");

  @NotNull
  @Override
  public StubIndexKey<String, PostCssCustomSelector> getKey() {
    return KEY;
  }
}