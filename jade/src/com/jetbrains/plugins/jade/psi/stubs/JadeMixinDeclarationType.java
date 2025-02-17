package com.jetbrains.plugins.jade.psi.stubs;

import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.jetbrains.plugins.jade.psi.impl.JadeMixinDeclarationImpl;
import com.jetbrains.plugins.jade.psi.stubs.impl.JadeMixinDeclarationStubImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JadeMixinDeclarationType extends JadeStubElementType<JadeMixinDeclarationStubImpl, JadeMixinDeclarationImpl> {
  public JadeMixinDeclarationType(@NotNull @NonNls String debugName) {
    super(debugName);
  }

  @Override
  public JadeMixinDeclarationImpl createPsi(@NotNull JadeMixinDeclarationStubImpl stub) {
    return new JadeMixinDeclarationImpl(stub, this);
  }

  @Override
  public @NotNull JadeMixinDeclarationStubImpl createStub(@NotNull JadeMixinDeclarationImpl psi, StubElement parentStub) {
    return new JadeMixinDeclarationStubImpl(parentStub, this, psi.getName());
  }

  @Override
  public void serialize(@NotNull JadeMixinDeclarationStubImpl stub, @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
  }

  @Override
  public @NotNull JadeMixinDeclarationStubImpl deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new JadeMixinDeclarationStubImpl(parentStub, this, dataStream.readNameString());
  }

  @Override
  public void indexStub(@NotNull JadeMixinDeclarationStubImpl stub, @NotNull IndexSink sink) {
    String name = stub.getName();
    if (name != null) {
      sink.occurrence(JadeMixinIndex.KEY, name);
    }
  }
}
