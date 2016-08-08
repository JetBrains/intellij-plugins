package org.intellij.plugins.postcss.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStub;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssCustomMediaImpl extends CssNamedStubElement<CssNamedStub<PostCssCustomMedia>> implements PostCssCustomMedia {
  public PostCssCustomMediaImpl(@NotNull CssNamedStub<PostCssCustomMedia> stub, @NotNull CssNamedStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PostCssCustomMediaImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiElement getNameIdentifier() {
    return getFirstChild();
  }

  @NotNull
  @Override
  public String getName() {
    CssNamedStub<PostCssCustomMedia> stub = getStub();
    if (stub != null) return stub.getName();
    PsiElement nameIdentifier = getNameIdentifier();
    if (nameIdentifier == null) return "";
    String text = nameIdentifier.getText();
    return StringUtil.startsWith(text, "--") ? text.substring(2) : "";
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null;
  }
}