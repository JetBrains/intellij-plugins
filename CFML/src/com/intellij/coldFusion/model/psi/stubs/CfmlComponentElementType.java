/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.lang.Language;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author vnikolaenko
 */
public abstract class CfmlComponentElementType extends CfmlStubElementType<CfmlComponentStub, CfmlComponent> {
  public CfmlComponentElementType(@NotNull @NonNls final String debugName, @Nullable final Language language) {
    super(debugName, language);
  }

  @Override
  public CfmlComponentStub createStub(@NotNull CfmlComponent psi, StubElement parentStub) {
    return new CfmlComponentStubImpl(parentStub, this, psi.getName(),
                                     psi.isInterface(), psi.getSuperName(), psi.getInterfaceNames());
  }

  public void serialize(@NotNull CfmlComponentStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isInterface());
    dataStream.writeName(stub.getSuperclass());
    dataStream.writeByte(stub.getInterfaces().length);
    for (String name : stub.getInterfaces()) {
      dataStream.writeName(name);
    }
  }

  @NotNull
  public CfmlComponentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    String name = StringRef.toString(dataStream.readName());
    boolean isInterface = dataStream.readBoolean();
    String superclass = StringRef.toString(dataStream.readName());

    byte supersNumber = dataStream.readByte();
    String[] interfaces = new String[supersNumber];
    for (int i = 0; i < supersNumber; i++) {
      interfaces[i] = StringRef.toString(dataStream.readName());
    }
    return new CfmlComponentStubImpl(parentStub, this, name, isInterface, superclass, interfaces);
  }

  @Override
  public void indexStub(@NotNull CfmlComponentStub stub, @NotNull IndexSink sink) {
    super.indexStub(stub, sink);
    String shortName = stub.getName();
    if (shortName != null) {
      if (stub.isInterface()) {
        sink.occurrence(CfmlInterfaceIndex.KEY, shortName.toLowerCase());
      }
      else {
        sink.occurrence(CfmlComponentIndex.KEY, shortName.toLowerCase());
      }
    }
    if (stub.getSuperclass() != null) {
      sink.occurrence(CfmlInheritanceIndex.KEY, stub.getSuperclass().toLowerCase());
    }
    for (String superName : stub.getInterfaces()) {
      sink.occurrence(CfmlInheritanceIndex.KEY, superName.toLowerCase());
    }
  }
}
