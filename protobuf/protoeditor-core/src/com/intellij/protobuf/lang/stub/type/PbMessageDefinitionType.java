/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.stub.type;

import com.intellij.lang.Language;
import com.intellij.protobuf.lang.psi.PbMessageDefinition;
import com.intellij.protobuf.lang.psi.impl.PbMessageDefinitionImpl;
import com.intellij.protobuf.lang.stub.PbMessageDefinitionStub;
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex;
import com.intellij.protobuf.lang.stub.index.ShortNameIndex;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PbMessageDefinitionType
    extends IStubElementType<PbMessageDefinitionStub, PbMessageDefinition> {

  PbMessageDefinitionType(String debugName, Language language) {
    super(debugName, language);
  }

  @Override
  public PbMessageDefinition createPsi(@NotNull PbMessageDefinitionStub stub) {
    return new PbMessageDefinitionImpl(stub, this);
  }

  @Override
  public @NotNull PbMessageDefinitionStub createStub(
      @NotNull PbMessageDefinition psi, StubElement parentStub) {
    return new PbMessageDefinitionStub(parentStub, this, psi.getName());
  }

  @Override
  public @NotNull String getExternalId() {
    return "protobuf.MESSAGE_DEFINITION";
  }

  @Override
  public void serialize(@NotNull PbMessageDefinitionStub stub, @NotNull StubOutputStream dataStream)
      throws IOException {
    dataStream.writeName(stub.getName());
  }

  @Override
  public @NotNull PbMessageDefinitionStub deserialize(
      @NotNull StubInputStream dataStream, StubElement parentStub)
      throws IOException {
    String name = null;
    StringRef nameRef = dataStream.readName();
    if (nameRef != null) {
      name = nameRef.getString();
    }
    return new PbMessageDefinitionStub(parentStub, this, name);
  }

  @Override
  public void indexStub(@NotNull PbMessageDefinitionStub stub, @NotNull IndexSink sink) {
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
