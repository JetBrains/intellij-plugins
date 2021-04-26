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
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.protobuf.lang.psi.PbPackageStatement;
import com.intellij.protobuf.lang.stub.PbPackageStatementStub;

abstract class PbPackageStatementMixin extends PbStubbedElementBase<PbPackageStatementStub>
    implements PbPackageStatement {

  PbPackageStatementMixin(ASTNode node) {
    super(node);
  }

  PbPackageStatementMixin(
      PbPackageStatementStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }
}
