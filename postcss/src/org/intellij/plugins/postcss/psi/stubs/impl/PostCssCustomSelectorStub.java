package org.intellij.plugins.postcss.psi.stubs.impl;

import com.intellij.psi.css.impl.stubs.base.CssNamedStub;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorStub extends CssNamedStub<PostCssCustomSelector> {

  public PostCssCustomSelectorStub(StubElement parent, @NotNull IStubElementType elementType, @NotNull StringRef name, int textOffset) {
    super(parent, elementType, name, textOffset);
  }

  public PostCssCustomSelectorStub(StubElement parent, @NotNull IStubElementType elementType, @NotNull String name, int textOffset) {
    super(parent, elementType, name, textOffset);
  }
}