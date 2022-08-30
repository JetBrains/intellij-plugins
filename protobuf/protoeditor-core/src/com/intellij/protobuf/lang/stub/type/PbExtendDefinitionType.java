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
import com.intellij.protobuf.lang.psi.PbExtendDefinition;
import com.intellij.protobuf.lang.psi.PbTypeName;
import com.intellij.protobuf.lang.psi.impl.PbExtendDefinitionImpl;
import com.intellij.protobuf.lang.stub.PbExtendDefinitionStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PbExtendDefinitionType
    extends IStubElementType<PbExtendDefinitionStub, PbExtendDefinition> {

  PbExtendDefinitionType(String debugName, Language language) {
    super(debugName, language);
  }

  @Override
  public PbExtendDefinition createPsi(@NotNull PbExtendDefinitionStub stub) {
    return new PbExtendDefinitionImpl(stub, this);
  }

  @NotNull
  @Override
  public PbExtendDefinitionStub createStub(
      @NotNull PbExtendDefinition psi, StubElement parentStub) {
    // TODO(volkman): Handle extended type better
    String typeString = null;
    PbTypeName typeName = psi.getTypeName();
    if (typeName != null) {
      typeString = typeName.getReferenceString();
    }

    return new PbExtendDefinitionStub(parentStub, this, typeString == null ? "" : typeString);
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "protobuf.EXTEND_DEFINITION";
  }

  @Override
  public void serialize(@NotNull PbExtendDefinitionStub stub, @NotNull StubOutputStream dataStream)
      throws IOException {
    dataStream.writeUTF(stub.getExtendedType());
  }

  @NotNull
  @Override
  public PbExtendDefinitionStub deserialize(
      @NotNull StubInputStream dataStream, StubElement parentStub)
      throws IOException {
    String extendedType = dataStream.readUTF();
    return new PbExtendDefinitionStub(parentStub, this, extendedType);
  }

  @Override
  public void indexStub(@NotNull PbExtendDefinitionStub stub, @NotNull IndexSink sink) {}
}
