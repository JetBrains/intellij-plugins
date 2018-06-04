package org.intellij.plugins.postcss.psi.impl;

import com.intellij.css.util.CssPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.css.descriptor.CssElementDescriptor;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.CssNamedItemPresentation;
import com.intellij.psi.css.impl.stubs.base.CssNamedStub;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElementType;
import com.intellij.util.IncorrectOperationException;
import icons.PostcssIcons;
import org.intellij.plugins.postcss.descriptors.PostCssCustomSelectorDescriptor;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;

public class PostCssCustomSelectorImpl extends CssNamedStubElement<CssNamedStub<PostCssCustomSelector>> implements PostCssCustomSelector {

  public PostCssCustomSelectorImpl(@NotNull CssNamedStub<PostCssCustomSelector> stub, @NotNull CssNamedStubElementType nodeType) {
    super(stub, nodeType);
  }

  public PostCssCustomSelectorImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiElement getNameIdentifier() {
    PsiElement token = getLastChild();
    if (token == null || token.getNode().getElementType() != CssElementTypes.CSS_IDENT) return null;
    return token;
  }

  @NotNull
  @Override
  public String getName() {
    CssNamedStub<PostCssCustomSelector> stub = getStub();
    if (stub != null) return stub.getName();
    PsiElement nameIdentifier = getNameIdentifier();
    if (nameIdentifier == null) return "";
    String text = nameIdentifier.getText();
    return StringUtil.startsWith(text, "--") ? text.substring(2) : "";
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PsiElement nameIdentifier = getNameIdentifier();
    return nameIdentifier != null ? CssPsiUtil.replaceToken(nameIdentifier, StringUtil.startsWith(name, "--") ? name : "--" + name) : this;
  }

  @Override
  public ItemPresentation getPresentation() {
    return new CssNamedItemPresentation(this);
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return PostcssIcons.Custom_selectors;
  }

  @Override
  public int getTextOffset() {
    PsiElement identifier = getNameIdentifier();
    return identifier != null ? identifier.getTextRange().getStartOffset() : super.getTextOffset();
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PostCssElementVisitor) {
      ((PostCssElementVisitor)visitor).visitPostCssCustomSelector(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @NotNull
  @Override
  public Collection<? extends CssElementDescriptor> getDescriptors() {
    return Collections.singletonList(new PostCssCustomSelectorDescriptor(this));
  }

  @NotNull
  @Override
  public Collection<? extends CssElementDescriptor> getDescriptors(@NotNull PsiElement context) {
    return getDescriptors();
  }
}