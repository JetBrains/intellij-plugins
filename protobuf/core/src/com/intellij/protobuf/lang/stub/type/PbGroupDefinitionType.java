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
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.io.StringRef;
import com.intellij.protobuf.lang.psi.PbGroupDefinition;
import com.intellij.protobuf.lang.psi.impl.PbGroupDefinitionImpl;
import com.intellij.protobuf.lang.stub.PbGroupDefinitionStub;
import com.intellij.protobuf.lang.stub.index.QualifiedNameIndex;
import com.intellij.protobuf.lang.stub.index.ShortNameIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/** Stub type for {@link PbGroupDefinition}. */
public class PbGroupDefinitionType
    extends IStubElementType<PbGroupDefinitionStub, PbGroupDefinition> {

  PbGroupDefinitionType(String debugName, Language language) {
    super(debugName, language);
  }

  @Override
  public PbGroupDefinition createPsi(@NotNull PbGroupDefinitionStub stub) {
    return new PbGroupDefinitionImpl(stub, this);
  }

  @NotNull
  @Override
  public PbGroupDefinitionStub createStub(
      @NotNull PbGroupDefinition psi, StubElement parentStub) {
    return new PbGroupDefinitionStub(parentStub, this, psi.getName());
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "protobuf.group";
  }

  @Override
  public void serialize(@NotNull PbGroupDefinitionStub stub, @NotNull StubOutputStream dataStream)
      throws IOException {
    dataStream.writeName(stub.getName());
  }

  @NotNull
  @Override
  public PbGroupDefinitionStub deserialize(
      @NotNull StubInputStream dataStream, StubElement parentStub)
      throws IOException {
    String name = null;
    StringRef nameRef = dataStream.readName();
    if (nameRef != null) {
      name = nameRef.getString();
    }
    return new PbGroupDefinitionStub(parentStub, this, name);
  }

  @Override
  public void indexStub(@NotNull PbGroupDefinitionStub stub, @NotNull IndexSink sink) {
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
