package org.intellij.plugins.postcss.psi.impl;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.CssNamedItemPresentation;
import com.intellij.psi.css.impl.stubs.base.CssNamedStub;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElementType;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.PostCssIcons;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PostCssCustomMediaImpl extends CssNamedStubElement<CssNamedStub<PostCssCustomMedia>> implements PostCssCustomMedia {
  public PostCssCustomMediaImpl(@NotNull CssNamedStub<PostCssCustomMedia> stub, @NotNull CssNamedStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PostCssCustomMediaImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull PsiElement getNameIdentifier() {
    return getFirstChild();
  }

  @Override
  public @NotNull String getName() {
    CssNamedStub<PostCssCustomMedia> stub = getStub();
    if (stub != null) return stub.getName();
    String text = getNameIdentifier().getText();
    return StringUtil.startsWith(text, "--") ? text.substring(2) : "";
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return CssPsiUtil.replaceToken(getNameIdentifier(), StringUtil.startsWith(name, "--") ? name : "--" + name);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new CssNamedItemPresentation(this);
  }

  @Override
  public @Nullable Icon getIcon(int flags) {
    return PostCssIcons.Custom_media;
  }
}