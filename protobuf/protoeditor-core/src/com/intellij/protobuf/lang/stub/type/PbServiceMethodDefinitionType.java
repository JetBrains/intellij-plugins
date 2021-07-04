package com.intellij.protobuf.lang.stub.type;

import com.intellij.lang.Language;
import com.intellij.protobuf.lang.psi.PbServiceMethod;
import com.intellij.protobuf.lang.psi.impl.PbServiceMethodImpl;
import com.intellij.protobuf.lang.stub.PbServiceMethodStub;
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex;
import com.intellij.protobuf.lang.stub.index.ShortNameIndex;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class PbServiceMethodDefinitionType
  extends IStubElementType<PbServiceMethodStub, PbServiceMethod> {

  public PbServiceMethodDefinitionType(@NotNull String debugName, @Nullable Language language) {
    super(debugName, language);
  }

  @Override
  public PbServiceMethod createPsi(@NotNull PbServiceMethodStub stub) {
    return new PbServiceMethodImpl(stub, this);
  }

  @Override
  public @NotNull PbServiceMethodStub createStub(@NotNull PbServiceMethod psi,
                                                 StubElement<? extends PsiElement> parentStub) {
    return new PbServiceMethodStub(parentStub, this, psi.getName());
  }

  @Override
  public @NotNull String getExternalId() {
    return "protobuf.serviceMethod";
  }

  @Override
  public void serialize(@NotNull PbServiceMethodStub stub,
                        @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
  }

  @Override
  public @NotNull PbServiceMethodStub deserialize(@NotNull StubInputStream dataStream,
                                                  StubElement parentStub) throws IOException {
    String name = null;
    StringRef nameRef = dataStream.readName();
    if (nameRef != null) {
      name = nameRef.getString();
    }
    return new PbServiceMethodStub(parentStub, this, name);
  }

  @Override
  public void indexStub(@NotNull PbServiceMethodStub stub, @NotNull IndexSink sink) {
    String name = stub.getName();
    if (name != null) {
      sink.occurrence(ShortNameIndex.KEY, name);
    }
    QualifiedName qualifiedName = stub.getQualifiedName();
    if (qualifiedName != null) {
      sink.occurrence(QualifiedNameIndex.KEY, qualifiedName.toString());
    }
  }
}
