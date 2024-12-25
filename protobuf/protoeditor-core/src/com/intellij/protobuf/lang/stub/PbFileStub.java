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
package com.intellij.protobuf.lang.stub;

import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.stub.type.PbStubElementTypes;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.Nullable;

/** Protobuf file stub. */
public class PbFileStub extends PsiFileStubImpl<PbFile> implements PbStatementOwnerStub<PbFile> {

  public PbFileStub(PbFile file) {
    super(file);
  }

  @Override
  public @Nullable QualifiedName getChildScope() {
    PbPackageStatementStub packageStatement = getPackageStatement();
    return packageStatement != null ? packageStatement.getPackageQualifiedName() : null;
  }

  private @Nullable PbPackageStatementStub getPackageStatement() {
    return findChildStubByType(PbStubElementTypes.PACKAGE_STATEMENT);
  }
}
