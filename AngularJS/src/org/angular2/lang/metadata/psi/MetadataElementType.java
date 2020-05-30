// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.psi;

import com.intellij.lang.Language;
import com.intellij.psi.stubs.*;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MetadataElementType<Stub extends MetadataElementStub<?>> extends IStubElementType<Stub, MetadataElement<Stub>> {

  private final @NotNull MetadataStubConstructor<? extends Stub> myStubConstructor;
  private final @NotNull MetadataElementConstructor<Stub> myPsiConstructor;

  public MetadataElementType(@NotNull String debugName,
                             Language language,
                             @NotNull MetadataStubConstructor<? extends Stub> stubConstructor,
                             @NotNull MetadataElementConstructor<Stub> psiConstructor) {
    super(debugName, language);
    myStubConstructor = stubConstructor;
    myPsiConstructor = psiConstructor;
  }

  @Override
  public MetadataElement<Stub> createPsi(@NotNull Stub stub) {
    return myPsiConstructor.construct(stub);
  }

  @Override
  public @NotNull Stub createStub(@NotNull MetadataElement psi, StubElement parentStub) {
    throw new UnsupportedOperationException();
  }

  @NonNls
  @Override
  public String toString() {
    return "METADATA_JSON:" + super.toString();
  }

  @Override
  public @NotNull String getExternalId() {
    return toString();
  }

  @Override
  public void serialize(@NotNull Stub stub, @NotNull StubOutputStream dataStream) throws IOException {
    stub.serialize(dataStream);
  }

  @Override
  public @NotNull Stub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return myStubConstructor.construct(dataStream, parentStub);
  }

  @Override
  public void indexStub(@NotNull Stub stub, @NotNull IndexSink sink) {
    stub.index(sink);
  }

  public interface MetadataStubConstructor<Stub extends MetadataElementStub<?>> {
    Stub construct(StubInputStream stream, StubElement parent) throws IOException;
  }

  public interface MetadataElementConstructor<Stub extends MetadataElementStub<?>> {
    MetadataElement<Stub> construct(@NotNull Stub stub);
  }
}
