package org.intellij.plugins.postcss.psi.stubs;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomMediaIndex extends StringStubIndexExtension<PostCssCustomMedia> {
  public static final StubIndexKey<String, PostCssCustomMedia> KEY = StubIndexKey.createIndexKey("postcss.custom.media");

  @NotNull
  @Override
  public StubIndexKey<String, PostCssCustomMedia> getKey() {
    return KEY;
  }
}