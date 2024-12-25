package org.intellij.plugins.postcss.psi;

import com.intellij.psi.css.CssFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssFileElementType extends IStubFileElementType {
  private static final int MY_STUB_VERSION = 3;

  public PostCssFileElementType() {
    super("POST_CSS_FILE", PostCssLanguage.INSTANCE);
  }

  @Override
  public int getStubVersion() {
    return super.getStubVersion() + CssFileElementType.BASE_VERSION + MY_STUB_VERSION;
  }

  @Override
  public @NotNull String getExternalId() {
    return "postcss.file";
  }
}
