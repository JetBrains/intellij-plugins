package org.intellij.plugins.postcss.psi.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.stubs.base.CssNamedStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.impl.PostCssCustomSelectorImpl;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.intellij.plugins.postcss.psi.stubs.impl.PostCssCustomSelectorStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PostCssCustomSelectorStubElementType extends CssNamedStubElementType<PostCssCustomSelectorStub, PostCssCustomSelector> {

  public PostCssCustomSelectorStubElementType() {
    super("POST_CSS_CUSTOM_SELECTOR", PostCssLanguage.INSTANCE);
  }

  @Override
  public PsiElement createElement(ASTNode node) {
    return new PostCssCustomSelectorImpl(node);
  }

  @Override
  public PostCssCustomSelector createPsi(@NotNull PostCssCustomSelectorStub stub) {
    return new PostCssCustomSelectorImpl(stub, this);
  }

  @NotNull
  @Override
  public PostCssCustomSelectorStub createStub(@NotNull PostCssCustomSelector psi, StubElement parentStub) {
    //noinspection ConstantConditions
    return new PostCssCustomSelectorStub(parentStub, this, psi.getName(), psi.getTextOffset());
  }

  public void serialize(@NotNull final PostCssCustomSelectorStub stub, @NotNull final StubOutputStream stream) throws IOException {
    serializeNameAndTextOffset(stub, stream);
  }

  @NotNull
  public PostCssCustomSelectorStub deserialize(@NotNull final StubInputStream stream, final StubElement parentStub) throws IOException {
    Pair<StringRef, Integer> nameAndTextOffset = deserializeNameAndTextOffset(stream);
    return new PostCssCustomSelectorStub(parentStub, this, nameAndTextOffset.first, nameAndTextOffset.second);
  }

  public void indexStub(@NotNull final PostCssCustomSelectorStub stub, @NotNull final IndexSink sink) {
    sink.occurrence(PostCssCustomSelectorIndex.KEY, stub.getName());
  }
}