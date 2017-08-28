package org.intellij.plugins.markdown.lang.stubs.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.intellij.plugins.markdown.lang.index.MarkdownHeadersIndex;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl;
import org.intellij.plugins.markdown.lang.stubs.MarkdownStubElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

public class MarkdownHeaderStubElementType extends MarkdownStubElementType<MarkdownHeaderStubElement, MarkdownHeaderImpl> {
  private static final Logger LOG = Logger.getInstance(MarkdownHeaderStubElementType.class);

  public MarkdownHeaderStubElementType(@NotNull String debugName) {
    super(debugName);
  }

  @NotNull
  @Override
  public PsiElement createElement(@NotNull ASTNode node) {
    return new MarkdownHeaderImpl(node);
  }

  @Override
  public MarkdownHeaderImpl createPsi(@NotNull MarkdownHeaderStubElement stub) {
    return new MarkdownHeaderImpl(stub, this);
  }

  @NotNull
  @Override
  public MarkdownHeaderStubElement createStub(@NotNull MarkdownHeaderImpl psi, StubElement parentStub) {
    return new MarkdownHeaderStubElement(parentStub, this, psi.getName(), psi.getPresentation());
  }

  @Override
  public void serialize(@NotNull MarkdownHeaderStubElement stub, @NotNull StubOutputStream dataStream) throws IOException {
    writeUTFFast(dataStream, stub.getLocationString());
    writeUTFFast(dataStream, stub.getPresentableText());
    writeUTFFast(dataStream, stub.getIndexedName());
  }

  private static void writeUTFFast(@NotNull StubOutputStream dataStream, String text) throws IOException {
    if (text == null) text = "";
    dataStream.writeUTFFast(text);
  }

  @NotNull
  @Override
  public MarkdownHeaderStubElement deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) {
    String locationString = null;
    String presentableText = null;
    String indexedName = null;
    try {
      locationString = dataStream.readUTFFast();
      presentableText = dataStream.readUTFFast();
      indexedName = dataStream.readUTFFast();
    }
    catch (IOException e) {
      LOG.error("Cannot read data stream; ", e.getMessage());
    }

    String finalPresentableString = StringUtil.isEmpty(presentableText) ? null : presentableText;
    String finalLocationString = StringUtil.isEmpty(locationString) ? null : locationString;
    String finalIndexedString = StringUtil.isEmpty(indexedName) ? null : indexedName;
    return new MarkdownHeaderStubElement(
      parentStub,
      this,
      finalIndexedString,
      new ItemPresentation() {
        @Nullable
        @Override
        public String getPresentableText() {
          return finalPresentableString;
        }

        @Nullable
        @Override
        public String getLocationString() {
          return finalLocationString;
        }

        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
          return null;
        }
      }
    );
  }

  @Override
  public void indexStub(@NotNull MarkdownHeaderStubElement stub, @NotNull IndexSink sink) {
    String indexedName = stub.getIndexedName();
    if (indexedName != null) sink.occurrence(MarkdownHeadersIndex.Companion.getKEY(), indexedName);
  }
}