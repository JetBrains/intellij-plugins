// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.lang.Language;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class CfmlComponentElementType extends CfmlStubElementType<CfmlComponentStub, CfmlComponent> {
  public CfmlComponentElementType(final @NotNull @NonNls String debugName, final @Nullable Language language) {
    super(debugName, language);
  }

  @Override
  public @NotNull CfmlComponentStub createStub(@NotNull CfmlComponent psi, StubElement parentStub) {
    return new CfmlComponentStubImpl(parentStub, this, psi.getName(),
                                     psi.isInterface(), psi.getSuperName(), psi.getInterfaceNames());
  }

  @Override
  public void serialize(@NotNull CfmlComponentStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isInterface());
    dataStream.writeName(stub.getSuperclass());
    dataStream.writeByte(stub.getInterfaces().length);
    for (String name : stub.getInterfaces()) {
      dataStream.writeName(name);
    }
  }

  @Override
  public @NotNull CfmlComponentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    String name = dataStream.readNameString();
    boolean isInterface = dataStream.readBoolean();
    String superclass = dataStream.readNameString();

    byte supersNumber = dataStream.readByte();
    String[] interfaces = new String[supersNumber];
    for (int i = 0; i < supersNumber; i++) {
      interfaces[i] = dataStream.readNameString();
    }
    return new CfmlComponentStubImpl(parentStub, this, name, isInterface, superclass, interfaces);
  }

  @Override
  public void indexStub(@NotNull CfmlComponentStub stub, @NotNull IndexSink sink) {
    super.indexStub(stub, sink);
    String shortName = stub.getName();
    if (shortName != null) {
      if (stub.isInterface()) {
        sink.occurrence(CfmlInterfaceIndex.KEY, StringUtil.toLowerCase(shortName));
      }
      else {
        sink.occurrence(CfmlComponentIndex.KEY, StringUtil.toLowerCase(shortName));
      }
    }
    if (stub.getSuperclass() != null) {
      sink.occurrence(CfmlInheritanceIndex.KEY, StringUtil.toLowerCase(stub.getSuperclass()));
    }
    for (String superName : stub.getInterfaces()) {
      sink.occurrence(CfmlInheritanceIndex.KEY, StringUtil.toLowerCase(superName));
    }
  }
}
