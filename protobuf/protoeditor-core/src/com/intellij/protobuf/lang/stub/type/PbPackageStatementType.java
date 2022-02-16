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
import com.intellij.protobuf.lang.psi.PbPackageName;
import com.intellij.protobuf.lang.psi.PbPackageStatement;
import com.intellij.protobuf.lang.psi.impl.PbPackageStatementImpl;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.protobuf.lang.stub.PbPackageStatementStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Stub element type for {@link PbPackageStatement}.
 */
public class PbPackageStatementType
    extends IStubElementType<PbPackageStatementStub, PbPackageStatement> {

  PbPackageStatementType(String debugName, Language language) {
    super(debugName, language);
  }

  @Override
  public PbPackageStatement createPsi(@NotNull PbPackageStatementStub stub) {
    return new PbPackageStatementImpl(stub, this);
  }

  @NotNull
  @Override
  public PbPackageStatementStub createStub(
      @NotNull PbPackageStatement psi, StubElement parentStub) {
    QualifiedName packageNameQualifiedName = PbPsiUtil.EMPTY_QUALIFIED_NAME;
    PbPackageName packageName = psi.getPackageName();
    if (packageName != null) {
      packageNameQualifiedName = packageName.getQualifiedName();
    }

    return new PbPackageStatementStub(parentStub, this, packageNameQualifiedName);
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "protobuf.package";
  }

  @Override
  public void serialize(@NotNull PbPackageStatementStub stub, @NotNull StubOutputStream dataStream)
      throws IOException {
    QualifiedName packageName = stub.getPackageQualifiedName();
    dataStream.writeName(packageName.toString());
  }

  @NotNull
  @Override
  public PbPackageStatementStub deserialize(
      @NotNull StubInputStream dataStream, StubElement parentStub)
      throws IOException {
    StringRef packageName = dataStream.readName();
    return new PbPackageStatementStub(
        parentStub,
        this,
        packageName != null
            ? QualifiedName.fromDottedString(packageName.getString())
            : PbPsiUtil.EMPTY_QUALIFIED_NAME);
  }

  @Override
  public void indexStub(@NotNull PbPackageStatementStub stub, @NotNull IndexSink sink) {}
}
