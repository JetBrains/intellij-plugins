package org.intellij.plugins.postcss.psi.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStub;
import com.intellij.psi.css.impl.stubs.base.CssSimpleNamedStubElementType;
import com.intellij.psi.stubs.IndexSink;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorImpl;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PostCssCustomSelectorStubElementType extends CssSimpleNamedStubElementType<PostCssCustomSelector> {

  public PostCssCustomSelectorStubElementType(@NonNls @NotNull String debugName) {
    super(debugName, PostCssLanguage.INSTANCE);
  }

  @Override
  public PsiElement createElement(ASTNode node) {
    return new PostCssCustomSelectorImpl(node);
  }

  @Override
  public PostCssCustomSelector createPsi(@NotNull CssNamedStub<PostCssCustomSelector> stub) {
    return new PostCssCustomSelectorImpl(stub, this);
  }

  @Override
  public void indexStub(@NotNull final CssNamedStub<PostCssCustomSelector> stub, @NotNull final IndexSink sink) {
    sink.occurrence(PostCssCustomSelectorIndex.KEY, stub.getName());
  }
}